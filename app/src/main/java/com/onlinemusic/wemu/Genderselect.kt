package com.onlinemusic.wemu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout

class Genderselect : AppCompatActivity() {
    var btn_back: LinearLayout?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genderselect)
        btn_back = findViewById(R.id.btn_back)

        btn_back?.setOnClickListener {

            onBackPressed()
        }

    }
}