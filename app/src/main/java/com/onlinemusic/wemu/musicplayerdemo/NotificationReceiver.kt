package com.onlinemusic.wemu.musicplayerdemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.onlinemusic.wemu.MainActivity
import com.onlinemusic.wemu.R


class NotificationReceiver : BroadcastReceiver() {


    fun prevNext(myMessage:String,context: Context?){
        val intent = Intent(MainActivity.RECEIVER_INTENT)
        intent.putExtra(MainActivity.RECEIVER_MESSAGE, myMessage)
        if (context != null) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ApplicationClass.PREVIOUS -> {
                playNext(false)
               // prevNextSong(false)
               // prevNext("prev",context)
              //  mNotificationNextPrev!!.onPrevClick()
            }

            ApplicationClass.PLAY -> {
                if (!MainActivity.isPlaying) {
                    playMusic()
                } else {
                    pauseMusic()
                }
            }

            ApplicationClass.NEXT -> {
               // cancelNotification(context)
                playNext(true)
               // prevNextSong(true)
              //  prevNext("next",context)
              //  mNotificationNextPrev!!.onNextClick()
            }

            ApplicationClass.EXIT -> {
                cancelNotification(context)
              //  playNext(true)
               // prevNextSong(true)
              //  prevNext("next",context)
              //  mNotificationNextPrev!!.onNextClick()
            }


        }
    }

    fun playMusic() {
        MainActivity.isPlaying = true
        MainActivity.musicService!!.mediaPlayer!!.start()
        MainActivity.mModelData?.let {
            MainActivity.musicService!!.showNotification(
                R.drawable.ic_pause,
                it
            )
        }
        // MainActivity.playBtn.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_pause))
        MainActivity.playBtn.setImageResource(R.drawable.ic_pause)
        MainActivity.playBtnShort.setImageResource(R.drawable.pause_icon)

    }

    fun playNext(isNext:Boolean) {
        MainActivity.musicService!!.playNext(isNext)
    }
    fun pauseMusic() {
        MainActivity.isPlaying = false
        MainActivity.musicService!!.mediaPlayer!!.pause()
        MainActivity.mModelData?.let {
            MainActivity.musicService!!.showNotification(
                R.drawable.play,
                it
            )
        }
        //  MainActivity.playBtn.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.play));
        MainActivity.playBtn.setImageResource(R.drawable.play)
           // ContextCompat.getDrawable(context!!, R.drawable.play)
        MainActivity.playBtnShort.setImageResource(R.drawable.play_icon)

    }

    private fun prevNextSong(increment: Boolean) {
        setSongPosition(increment)
        MainActivity.musicService!!.createPlayer()
    }

    private fun cancelNotification(context: Context?) {

        MainActivity.isPlaying = false
        MainActivity.musicService!!.mediaPlayer!!.pause()
        MainActivity.playBtn.setImageResource(R.drawable.play)
        MainActivity.playBtnShort.setImageResource( R.drawable.play_icon)

        MainActivity.musicService!!.closeNotification()
    }

}