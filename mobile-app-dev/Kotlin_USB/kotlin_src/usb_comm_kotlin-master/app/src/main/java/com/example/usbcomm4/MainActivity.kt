package com.example.usbcomm4

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.example.usbcomm4.databinding.ActivityMainBinding
import com.example.usbcommunicator.IUsbCallback
import com.example.usbcommunicator.UsbEngine
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {
    private val mCallback: IUsbCallback = object : IUsbCallback {
        override fun onConnectionEstablished() {
            val tv = findViewById<TextView>(R.id.textView)
            tv.text = usbEngine!!.connectionStatus()
            tv.setTextColor(Color.GREEN)
        }

        override fun onDeviceDisconnected() {
            val tv = findViewById<TextView>(R.id.textView)
            tv.text = usbEngine!!.connectionStatus()
            tv.setTextColor(Color.RED)
        }

        override fun onDataReceived(data: ByteArray?, num: Int) {
            val tv = findViewById<TextView>(R.id.textView2)
            if (data == null) {
                Log.d("App", "Received empty data!")
                return
            }
            var text = data.toString(StandardCharsets.UTF_8)
            Log.d("App", "Received: $text")
            if (text.length > 10) {
                text = text.substring(0, 10) + "..."
            }
            tv.text = text
        }
    }
    private var usbEngine: UsbEngine? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        usbEngine = UsbEngine(applicationContext, mCallback)
        onNewIntent(this.intent)
        findViewById<View>(R.id.button0).setOnClickListener { it: View? -> usbEngine!!.write("0".toByteArray()) }
        findViewById<View>(R.id.button1).setOnClickListener { it: View? -> usbEngine!!.write("1".toByteArray()) }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        usbEngine!!.onNewIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        val intent = this.intent
        onNewIntent(intent)
    }
}