package com.onlinemusic.wemu.utils

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.databinding.ActivityWebviewBinding


class Webview : AppCompatActivity() {

    lateinit var activityWebviewBinding: ActivityWebviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityWebviewBinding = DataBindingUtil.setContentView(this, R.layout.activity_webview)
        activityWebviewBinding.webview.loadUrl("https://www.termsandcondiitionssample.com/live.php?token=BMdX8FaXjTq9qDA3SofoJEeVqUdQPRmA")

    }

}