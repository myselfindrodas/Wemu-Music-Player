package com.onlinemusic.wemu

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.databinding.DataBindingUtil
import com.onlinemusic.wemu.databinding.ActivityPlaylistBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class Playlist : AppCompatActivity() {
    lateinit var activityPlayListBinding: ActivityPlaylistBinding

    private var mBottomSheetBehavior1: BottomSheetBehavior<*>? = null
    var btnAddplaylist:CardView?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_playlist)
        activityPlayListBinding = DataBindingUtil.setContentView(this, R.layout.activity_playlist)
        val bottomSheet = findViewById<View>(R.id.bottom_sheet)
        btnAddplaylist = findViewById(R.id.btnAddplaylist)
        mBottomSheetBehavior1 = BottomSheetBehavior.from(bottomSheet)

        activityPlayListBinding.btnAddplaylist?.setOnClickListener {

            if (mBottomSheetBehavior1?.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                mBottomSheetBehavior1?.setState(BottomSheetBehavior.STATE_EXPANDED)
                mBottomSheetBehavior1?.setDraggable(true)
            } else {
                mBottomSheetBehavior1?.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }
        activityPlayListBinding.btnBack.setOnClickListener {
            onBackPressed()
        }


    }
}