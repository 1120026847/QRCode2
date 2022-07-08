package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.myapplication.databinding.ActivityGenerateTypeBinding
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import retrofit2.Response
import java.util.*
import java.util.regex.Pattern

class GenerateType : AppCompatActivity() {
    private lateinit var retService: WordService
    private lateinit var binding: ActivityGenerateTypeBinding
    private var mQRBitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("moli", "GenerateType")
        binding = ActivityGenerateTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        retService = RetrofitInstance
            .getRetrofitInstance()
            .create(WordService::class.java)
        //  val Generate_text_Edittext=findViewById<EditText>(R.id.Generate_text_Edittext)
        binding.ivGenerateText.setOnClickListener {
            val intent = Intent(this, GenerateText::class.java)
            startActivity(intent)
        }
        binding.ivGenerateWeb.setOnClickListener {
            val intent = Intent(this, WEB_Activity::class.java)
            startActivity(intent)
        }
        binding.ivGenerateRandom.setOnClickListener {
            getRamdomRequest()
        }
binding.ivRandomQrcode.setOnLongClickListener {
    recogQRcode(binding.ivRandomQrcode)
    true
}
    }

    private fun getRamdomRequest() {
        val responseLiveData: LiveData<Response<Words>> = liveData {
            val response = retService.getWords()

            emit(response)
        }
        responseLiveData.observe(this, androidx.lifecycle.Observer {
            val words = it.body()
            val random = Random()
            val JsonArrayLength: Int? = words?.data?.size
            val RandomIndex = JsonArrayLength?.let { it1 -> random.nextInt(it1) }
            val requestRandomResult = RandomIndex?.let { it1 -> words.data.get(it1).word }
           // Toast.makeText(this, ""+requestRandomResult, Toast.LENGTH_SHORT).show()
            // Log.e(TAG, , )
            Log.e(TAG, "getRamdomRequest: "+requestRandomResult, )
            mQRBitmap = requestRandomResult?.let { it1 -> Utility.generateQR(it1) }
            if (mQRBitmap != null) {
               // mTextInput = binding.GenerateWebEdittext.text.toString()
                binding.ivRandomQrcode
                    .setImageBitmap(mQRBitmap)
                //binding.ivGenerateWebSave.visibility = View.VISIBLE

            } else {
              //  binding.ivGenerateWebSave.visibility = View.INVISIBLE
                binding.ivRandomQrcode.setImageBitmap(null)
                mQRBitmap = null
            }
        })

        //        responseLiveData.observe(this, Observer {
//            val words = it.body()
//            val random= Random()
//            val JsonArrayLength: Int? =words?.data?.size
//            val RandomIndex= JsonArrayLength?.let { it1 -> random.nextInt(it1) }
//            /**
//            for (int i = 0; i < surveyListVO.getResult().size(); i++) {
//            System.out.print(surveyListVO.getResult().get(i)
//            .getSurveyId());  print: 12
//            System.out.print(surveyListVO.getResult().get(i)
//            .getSurveyName());   print: B///C
//            System.out.print(surveyListVO.getMessage());   print: success
//            }
//             */
////            Log.e("moli", ""+words )
////            Log.e("moli", ""+words?.data?.get(0) )
////            Log.e("moli", ""+words?.data?.get(0)?.id )
////            Log.e("moli", ""+words?.data?.get(0)?.newsId)
////            Log.e("moli", ""+words?.msg)
//            //   Log.e("moli", ""+words?.data?.get(RandomIndex)?.word )
//
//            // Log.e("moli", ""+randomtext )
//            textView.setText(""+ RandomIndex?.let { it1 -> words?.data?.get(it1)?.word })
////            if (words != null) {
////
////            }
//        })
//    }

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
//        val editor = getSharedPreferences("data", Context.MODE_PRIVATE).edit()
//        //向SharedPreferences.Editor对象中添加数据
//        editor.putString("QRCode_WEB",re!!.text)
//        //调用apply()方法将添加的数据提交，从而完成数据存储操作
//        editor.apply()
//        val intent=Intent(this,ResultActivity::class.java)
//        startActivity(intent)
//        //Toast出内容
//        //  Toast.makeText(this@MainActivity, re!!.text, Toast.LENGTH_SHORT).show()
//        Log.e(TAG, re!!.text, )
        //利用正则表达式判断内容是否是URL，是的话则打开网页
        val regex = ("(((https|http)?://)?([a-z0-9]+[.])|(www.))"
                + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)") //设置正则表达式
        val pat = Pattern.compile(regex.trim { it <= ' ' }) //比对
        val mat = pat.matcher(re!!.text.trim { it <= ' ' })
        if (mat.matches()) {
            val uri = Uri.parse(re?.text)
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
}