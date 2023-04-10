package com.onlinemusic.wemu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.LinearLayout

class Player : AppCompatActivity() {

    var btn_back:LinearLayout?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        btn_back = findViewById(R.id.btn_back)
        this.getWindow()?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        btn_back?.setOnClickListener {

            onBackPressed()
        }
    }
}