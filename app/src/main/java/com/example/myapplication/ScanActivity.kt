package com.example.myapplication

import android.app.Activity
import android.app.Service
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import com.example.myapplication.Utility.KEY_RESULT
import com.example.myapplication.databinding.ActivityScanBinding
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import kotlinx.android.synthetic.main.activity_scan.*

class ScanActivity : AppCompatActivity() {
    private var isTorch = false
    private lateinit var binding: ActivityScanBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        barcode_view.decodeContinuous(callback)

        img_flash.setOnClickListener {
            if (isTorch){
                isTorch = false
                img_flash.setImageResource(R.drawable.flash_off)
            }else{
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
            if (result!!.text != null){
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
}