package com.example.myapplication

import android.annotation.SuppressLint
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
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.example.myapplication.Utility.KEY_CONTENT
import com.example.myapplication.Utility.REQUEST_CODE
import com.example.myapplication.databinding.ActivityWebBinding
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import kotlinx.android.synthetic.main.dialog_web.*
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

class WEB_Activity : AppCompatActivity() {
    private lateinit var binding: ActivityWebBinding
    private var mTextInput : String? = null
    private var mQRBitmap : Bitmap? = null
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)
       val activity=WEB_Activity()
        //_______________________
    binding.btnGenerateWebGenerate.setOnClickListener {
        val strURL = generateURLString(binding.GenerateWebEdittext.text.toString())
        mQRBitmap = Utility.generateQR(strURL)
        if (mQRBitmap != null) {
            mTextInput = binding.GenerateWebEdittext.text.toString()
            binding.imgGenerateWeb.setImageBitmap(mQRBitmap)
            binding.btnGenerateWebSave.visibility = View.VISIBLE

        } else {
            binding.btnGenerateWebSave.visibility = View.INVISIBLE
            binding.imgGenerateWeb.setImageBitmap(null)
            mQRBitmap = null
        }
    }
        binding.imgGenerateWeb.setOnLongClickListener {
            recogQRcode(binding.imgGenerateWeb)
            true
        }



        binding.btnGenerateWebSave.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED){
                    val strings = Array<String>(1) { "android.permission.WRITE_EXTERNAL_STORAGE" }
                    requestPermissions(strings, Utility.WRITE_PERMISSION)
                    return@setOnClickListener
                }
            }

           CapturePhotoUtils.insertImage(contentResolver, mQRBitmap, Date().time.toString(), this.mTextInput!!)
        }
        //_______________________
    }
    private fun generateURLString(url: String): String {
        var strResult = url
        if (url.indexOf("http") != 0){
            strResult = "http://" + url
        }
        return strResult
    }
    object CapturePhotoUtils {

        fun insertImage(cr: ContentResolver,
                        source: Bitmap?,
                        title: String,
                        description: String): String? {

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
                    val miniThumb = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null)
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
            kind: Int) {

            val matrix = Matrix()

            val scaleX = 50f / source.width
            val scaleY = 50f / source.height

            matrix.setScale(scaleX, scaleY)

            val thumb = Bitmap.createBitmap(source, 0, 0,
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
    //识别二维码的函数
    fun recogQRcode(imageView: ImageView) {
        val QRbmp = (imageView.drawable as BitmapDrawable).bitmap //将图片bitmap化
        val width = QRbmp.width
        val height = QRbmp.height
        val data = IntArray(width * height)
        QRbmp.getPixels(data, 0, width, 0, 0, width, height) //得到像素
        val source: GenerateText.RGBLuminanceSource = GenerateText.RGBLuminanceSource(QRbmp)
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
        editor.putString("QRCode_WEB",re!!.text)
        //调用apply()方法将添加的数据提交，从而完成数据存储操作
        editor.apply()
        val intent=Intent(this,ResultActivity::class.java)
        startActivity(intent)
        //Toast出内容
        //  Toast.makeText(this@MainActivity, re!!.text, Toast.LENGTH_SHORT).show()
        Log.e(TAG, re!!.text, )
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
}