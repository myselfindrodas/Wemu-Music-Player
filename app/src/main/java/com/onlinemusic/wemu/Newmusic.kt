package com.onlinemusic.wemu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout

class Newmusic : AppCompatActivity() {

    var btnMusicPlayer:LinearLayout?=null
    var btn_back: LinearLayout?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newmusic)
        btnMusicPlayer = findViewById(R.id.btnMusicPlayer)
        btn_back = findViewById(R.id.btn_back)
        btn_back?.setOnClickListener {

            onBackPressed()
        }
        btnMusicPlayer?.setOnClickListener {

            val intent = Intent(this, Player::class.java)
            startActivity(intent)
        }
    }
}