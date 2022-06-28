package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapplication.Utility.REQUEST_CODE
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mPermissions:Permissions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
       mPermissions=Permissions()
        requestMultiplePermissions()
        binding.generate.setOnClickListener {
            val intent = Intent(this@MainActivity, GenerateType::class.java)
            startActivity(intent)
        }
        binding.scan.setOnClickListener {
         Utility.scan(this)
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK){
                val bundleResult = data!!.extras
                val intent = Intent(this, ResultActivity::class.java)
                if (bundleResult != null) {
                    intent.putExtras(bundleResult)
                }
                startActivity(intent)
            }
        }
    }
    fun requestMultiplePermissions(){
        var permissions: Array<String> = arrayOf( Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        val register = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it[Manifest.permission.WRITE_EXTERNAL_STORAGE]!!) {// 同意
            } else {
           mPermissions.showReasonDialogSTORAGE()
            }
            if (it[Manifest.permission.CAMERA]!!) {// 同意

            } else {
                mPermissions.showReasonDialogCAMERA()
            }
        }

        register.launch(permissions)
    }
}