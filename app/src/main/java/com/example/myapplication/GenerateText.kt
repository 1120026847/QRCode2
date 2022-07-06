package com.example.myapplication

import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContentProviderCompat.requireContext
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.example.myapplication.databinding.ActivityGenerateTextBinding
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import kotlinx.android.synthetic.main.dialog_web.*
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*
import java.util.regex.Pattern
const val TAG="moli"
class GenerateText : AppCompatActivity() {
    private lateinit var binding: ActivityGenerateTextBinding
    private var mTextInput: String? = null
    private var mQRBitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerateTextBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        binding.ivGenerateTextSubmit.setOnClickListener {
            mQRBitmap = Utility.generateQR(binding.GenerateTextEdittext.text.toString())
            if (mQRBitmap != null) {
                mTextInput = binding.GenerateTextEdittext.text.toString()
                binding.imgGenerateText.setImageBitmap(mQRBitmap)
                binding.ivGenerateTextSave.visibility = View.VISIBLE

            } else {
                binding.ivGenerateTextSave.visibility = View.INVISIBLE
                binding.imgGenerateText.setImageBitmap(null)
                mQRBitmap = null
            }
        }
        binding.imgGenerateText.setOnLongClickListener {
            recogQRcode(binding.imgGenerateText)
            true
        }
        binding.ivGenerateTextSave.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                    val strings = Array<String>(1) { "android.permission.WRITE_EXTERNAL_STORAGE" }
                    requestPermissions(strings, Utility.WRITE_PERMISSION)
                    return@setOnClickListener
                }
            }

//            CapturePhotoUtils.insertImage(
//                contentResolver,
//                mQRBitmap,
//                Date().time.toString(),
//                this.mTextInput!!
//            )
            GlobalScope.launch {
                mQRBitmap?.let { it1 -> savePhoto(it1) }
            }
            //Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
        }

    }

    object CapturePhotoUtils {

        fun insertImage(
            cr: ContentResolver,
            source: Bitmap?,
            title: String,
            description: String
        ): String? {

            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, title)
            values.put(MediaStore.Images.Media.DISPLAY_NAME, title)
            values.put(MediaStore.Images.Media.DESCRIPTION, description)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            // Add the date meta data to ensure the image is added at the front of the gallery
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())

            var url: Uri? = null
            var stringUrl: String? = null    /* value to be returned */

            try {
                url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                if (source != null) {
                    assert(url != null)
                    val imageOut = cr.openOutputStream(url!!)
                    try {
                        source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut)
                    } finally {
                        assert(imageOut != null)
                        imageOut!!.close()
                    }

                    val id = ContentUris.parseId(url)
                    // Wait until MINI_KIND thumbnail is generated.
                    val miniThumb = MediaStore.Images.Thumbnails.getThumbnail(
                        cr,
                        id,
                        MediaStore.Images.Thumbnails.MINI_KIND,
                        null
                    )
                    // This is for backward compatibility.
                    storeThumbnail(cr, miniThumb, id, MediaStore.Images.Thumbnails.MICRO_KIND)
                } else {
                    assert(url != null)
                    cr.delete(url!!, null, null)
                    url = null
                }
            } catch (e: Exception) {
                if (url != null) {
                    cr.delete(url, null, null)
                    url = null
                }
            }

            if (url != null) {
                stringUrl = url.toString()
            }

            return stringUrl
        }

        private fun storeThumbnail(
            cr: ContentResolver,
            source: Bitmap,
            id: Long,
            kind: Int
        ) {

            val matrix = Matrix()

            val scaleX = 50f / source.width
            val scaleY = 50f / source.height

            matrix.setScale(scaleX, scaleY)

            val thumb = Bitmap.createBitmap(
                source, 0, 0,
                source.width,
                source.height, matrix,
                true
            )

            val values = ContentValues(4)
            values.put(MediaStore.Images.Thumbnails.KIND, kind)
            values.put(MediaStore.Images.Thumbnails.IMAGE_ID, id.toInt())
            values.put(MediaStore.Images.Thumbnails.HEIGHT, thumb.height)
            values.put(MediaStore.Images.Thumbnails.WIDTH, thumb.width)

            val url = cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values)

            try {
                assert(url != null)
                val thumbOut = cr.openOutputStream(url!!)
                thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut)
                assert(thumbOut != null)
                thumbOut!!.close()
            } catch (ignored: IOException) {
            }

        }
    }

    //识别图片所需要的RGBLuminanceSource类
    class RGBLuminanceSource(bitmap: Bitmap) :
        LuminanceSource(bitmap.width, bitmap.height) {
        private val bitmapPixels: ByteArray
        override fun getMatrix(): ByteArray {
            // 返回我们生成好的像素数据
            return bitmapPixels
        }

        override fun getRow(y: Int, row: ByteArray): ByteArray {
            // 这里要得到指定行的像素数据
            System.arraycopy(bitmapPixels, y * width, row, 0, width)
            return row
        }

        init {

            // 首先，要取得该图片的像素数组内容
            val data = IntArray(bitmap.width * bitmap.height)
            bitmapPixels = ByteArray(bitmap.width * bitmap.height)
            bitmap.getPixels(data, 0, width, 0, 0, width, height)

            // 将int数组转换为byte数组，也就是取像素值中蓝色值部分作为辨析内容
            for (i in data.indices) {
                bitmapPixels[i] = data[i].toByte()
            }
        }
    }

    //识别二维码的函数
    fun recogQRcode(imageView: ImageView) {
        val QRbmp = (imageView.drawable as BitmapDrawable).bitmap //将图片bitmap化
        val width = QRbmp.width
        val height = QRbmp.height
        val data = IntArray(width * height)
        QRbmp.getPixels(data, 0, width, 0, 0, width, height) //得到像素
        val source: RGBLuminanceSource = RGBLuminanceSource(QRbmp)
        // val source:com.google.zxing.RGBLuminanceSource=com.google.zxing.RGBLuminanceSource(QRbmp)
        //RGBLuminanceSource source = new RGBLuminanceSource(QRbmp);

//        val source: com.moli.qrcodetest7.MainActivity.RGBLuminanceSource =
//            com.moli.qrcodetest7.MainActivity.RGBLuminanceSource(QRbmp) //RGBLuminanceSource对象
        val bitmap1 = BinaryBitmap(HybridBinarizer(source))
        val reader = QRCodeReader()
        var re: Result? = null
        try {
            //得到结果
            re = reader.decode(bitmap1)
        } catch (e: NotFoundException) {
            e.printStackTrace()
        } catch (e: ChecksumException) {
            e.printStackTrace()
        } catch (e: FormatException) {
            e.printStackTrace()
        }
        val editor = getSharedPreferences("data", Context.MODE_PRIVATE).edit()
        //向SharedPreferences.Editor对象中添加数据
        editor.putString("QRCode_text", re!!.text)
        //调用apply()方法将添加的数据提交，从而完成数据存储操作
        editor.apply()
        val intent = Intent(this, ResultActivity::class.java)
        startActivity(intent)
        //Toast出内容
        //  Toast.makeText(this@MainActivity, re!!.text, Toast.LENGTH_SHORT).show()
        Log.e(TAG, re!!.text,)
        //利用正则表达式判断内容是否是URL，是的话则打开网页
        val regex = ("(((https|http)?://)?([a-z0-9]+[.])|(www.))"
                + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)") //设置正则表达式
        val pat = Pattern.compile(regex.trim { it <= ' ' }) //比对
        val mat = pat.matcher(re.text.trim { it <= ' ' })
        if (mat.matches()) {
            val uri = Uri.parse(re.text)
            val intent = Intent(Intent.ACTION_VIEW, uri) //打开浏览器
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
//            val intent=Intent(this,MainActivity::class.java)
//            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    //存储图片的方法，suspend表示允许挂起，在另外一个线程上运行
    private suspend fun savePhoto(bitmap: Bitmap) {
        //声明运行的范围
        withContext(Dispatchers.IO) {

            //MediaStore是管理媒体的，bitmap就是要存储的对象，api为29之前可用
//        if (MediaStore.Images.Media.insertImage(requireContext().contentResolver,bitmap,"","")==null){
//            Toast.makeText(requireContext(),"存储失败",Toast.LENGTH_SHORT).show()
//        }else{
//            Toast.makeText(requireContext(),"存储成功",Toast.LENGTH_SHORT).show()
//        }

//设置保存的路径,第一个参数是设置存储的路径，第二个参数是设置存储时的参数，比如标题等；如果为url为空，就执行run方法
            val saveUri = this@GenerateText.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues()
            ) ?: kotlin.run {
                MainScope().launch {
                    Toast.makeText(this@GenerateText, "存储失败", Toast.LENGTH_SHORT).show()
                }
                return@withContext//程序停止向下运行
            }
            //第一个参数是写入流的目的地uri，use函数能够自动关闭io流
            this@GenerateText.contentResolver.openOutputStream(saveUri).use {
                //设置为jpg格式的图片，90的压缩率，it指io流
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)) {//如果布尔值为ture
                    //toast只在主线程运行，不允许到父线程运行，MainScope().launch{代码}，就能解决这个问题
                    MainScope().launch {
                        Toast.makeText(
                           this@GenerateText,
                            "存储成功",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    MainScope().launch {
                        Toast.makeText(
                            this@GenerateText,
                            "存储失败",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
        }
    }
}
