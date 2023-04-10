package com.onlinemusic.wemu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.widget.DatePicker
import android.widget.LinearLayout
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.onlinemusic.wemu.R
import android.widget.DatePicker.OnDateChangedListener
import android.widget.Toast
import java.util.*

class DOB : AppCompatActivity() {
    private var datePicker: DatePicker? = null
    private var buttonDate: LinearLayout? = null
    private var btn_back: LinearLayout?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_dob)
        buttonDate = findViewById(R.id.button_date)
        datePicker = findViewById(R.id.datePicker)
        btn_back = findViewById(R.id.btn_back)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DAY_OF_MONTH]
        datePicker?.init(year, month, day,
            OnDateChangedListener { datePicker, year, month, dayOfMonth ->
                datePickerChange(datePicker, year, month, dayOfMonth
                )
            })
        buttonDate?.setOnClickListener(View.OnClickListener {
//            showDate()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        })

        btn_back?.setOnClickListener {

            onBackPressed()
        }
    }

    private fun datePickerChange(datePicker: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        Log.d("Date", "Year=" + year + " Month=" + (month + 1) + " day=" + dayOfMonth)
    }

    private fun showDate() {
        val year = datePicker!!.year
        val month = datePicker!!.month // 0 - 11
        val day = datePicker!!.dayOfMonth
        Toast.makeText(this, "Date: " + day + "-" + (month + 1) + "-" + year, Toast.LENGTH_LONG).show()
    }
}