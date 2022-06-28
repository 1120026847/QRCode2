package com.example.myapplication

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.myapplication.Utility.KEY_CONTENT
import com.example.myapplication.databinding.ActivityGenerateTypeBinding
import com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE

class GenerateType : AppCompatActivity() {
    private lateinit var binding:ActivityGenerateTypeBinding
    private var mQRBitmap : Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("moli", "GenerateType", )
        binding= ActivityGenerateTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.e("moli", "type success", )
      //  val Generate_text_Edittext=findViewById<EditText>(R.id.Generate_text_Edittext)
        binding.generateText.setOnClickListener {
            val intent = Intent(this, GenerateText::class.java)
            startActivity(intent)
        }
        binding.generateWeb.setOnClickListener {
            val intent = Intent(this, WEB_Activity::class.java)
            startActivity(intent)
        }
        binding.generateRandom.setOnClickListener {

        }

    }
}