package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.myapplication.Utility.KEY_RESULT
import com.example.myapplication.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.textTextString.text=""
        binding.textUrlString.text=""
        val prefs = getSharedPreferences("data", Context.MODE_PRIVATE)
        val QRCode_text2 = prefs.getString("QRCode_text", "")
        val QRCode_WEB = prefs.getString("QRCode_WEB", "")
        binding.textTextString.text = QRCode_text2
        binding.textUrlString.text=QRCode_WEB
        //binding.text_url_string.text = QRCode_WEB
        val bundle = intent.extras
        bundle?.getString(KEY_RESULT)?.let { pareInfo(it) }
binding.btnUrl.setOnClickListener {
    if (!binding.textUrlString.text.equals("")){
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse(binding.textUrlString.text.toString())
        startActivity(intent)
    }
}
    }
    private fun pareInfo(resultString: String){
        when {
            resultString.indexOf("http") == 0 -> // Website
                getWebsite(resultString)

            else -> //TEXT
                getTextType(resultString)
        }
    }
    private fun getWebsite(resultString: String) {
      binding.textUrlString.text=resultString
        binding.btnUrl.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(resultString)
            startActivity(intent)
        }
    }
    private fun getTextType(resultString: String) {
        binding.textTextString.text = resultString
    }

}