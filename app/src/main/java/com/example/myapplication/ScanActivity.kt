package com.example.myapplication

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.example.myapplication.Utility.KEY_RESULT
import com.example.myapplication.databinding.ActivityScanBinding
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import kotlinx.android.synthetic.main.activity_scan.*
import java.util.regex.Pattern

const val SELECT_IMAGE_REQUEST_CODE=1
const val  SCAN_REQUEST_CODE=200;
class ScanActivity : AppCompatActivity() {
    private var isTorch = false
    private lateinit var binding: ActivityScanBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        barcode_view.decodeContinuous(callback)
        binding.xiangce.setOnClickListener {
            val innerIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val wrapperIntent = Intent.createChooser(innerIntent, "选择二维码图片")
            startActivityForResult(wrapperIntent, SELECT_IMAGE_REQUEST_CODE);
        }
        img_flash.setOnClickListener {
            if (isTorch) {
                isTorch = false
                img_flash.setImageResource(R.drawable.flash_off)
            } else {
                isTorch = true
                img_flash.setImageResource(R.drawable.flash_on)
            }
            barcode_view.setTorch(isTorch)
        }

    }

    override fun onResume() {
        barcode_view.resume()
        super.onResume()
    }

    override fun onPause() {
        barcode_view.pause()
        super.onPause()
    }

    private val callback = object : BarcodeCallback {
        override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {

        }

        override fun barcodeResult(result: BarcodeResult?) {
            if (result!!.text != null) {
                val intent = Intent()
                val bundle = Bundle()
                bundle.putString(KEY_RESULT, result.text.toString())
                intent.putExtras(bundle)
                setResult(Activity.RESULT_OK, intent)
                val vibrator = application.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(100)
                finish()
            }
        }

    }
/*
           var cursor: Cursor? =
                intent.getData()
                    ?.let { data?.data?.let { it1 -> this.getContentResolver().query(it1, proj, null, null, null) } }
 */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_IMAGE_REQUEST_CODE) {
            var proj = arrayOf(MediaStore.Images.Media.DATA)
          //  val cursor=this.contentResolver.query(intent.data,proj,null,null,null)
//Cursor cursor = this.getContentResolver().query(intent.getData(),proj, null, null, null);
            val cursor= data?.data?.let { this.contentResolver.query(it,proj,null,null,null) }
            if (cursor?.moveToNext() == true) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val photoPath = cursor.getString(columnIndex)
                /*
                  Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath);
        ImageView iv = new ImageView(this);
        iv.setImageBitmap(bitmap);
                 */
                val bitmap=BitmapFactory.decodeFile(photoPath)
                val iv=ImageView(this)
                iv.setImageBitmap(bitmap)
                recogQRcode(iv)
            }
            cursor?.close()
        }
        /*
else if (requestCode == SCAN_REQUEST_CODE && resultCode == RESULT_OK) {
            String input = intent.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);
            showToast("扫描结果:"+input);
        }
         */
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
        editor.putString("QRCode_text",re!!.text)
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
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==android.R.id.home){
//            val intent=Intent(this,MainActivity::class.java)
//            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}