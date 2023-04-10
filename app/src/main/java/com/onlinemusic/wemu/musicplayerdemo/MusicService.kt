package com.onlinemusic.wemu.musicplayerdemo

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.onlinemusic.wemu.MainActivity
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.onlinemusic.wemu.utils.Coroutines
import okhttp3.internal.concurrent.formatDuration


class MusicService : Service(), AudioManager.OnAudioFocusChangeListener  {
    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    var modelList: ArrayList<CommonDataModel1>? = ArrayList()
    var notificationReceiver: NotificationReceiver? = null
    lateinit var audioManager: AudioManager
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable

    override fun onBind(intent: Intent?): IBinder? {
        return myBinder
    }

    override fun onCreate() {
        super.onCreate()
        notificationReceiver = NotificationReceiver()
    }

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            mediaSession = MediaSessionCompat(baseContext, "My Music")
            mediaSession.isActive = true
            //notificationReceiver!!.receiverListener(this@MusicService)
            return this@MusicService
        }
    }

    fun showNotification(
        playPauseBtn: Int,
        modelData: CommonDataModel1
    ) {

        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

     //   val contentIntent = PendingIntent.getActivity(this, 0, intent, flag)

        val prevIntent =
            Intent(baseContext, notificationReceiver?.javaClass ?: NotificationReceiver::class.java)
                .setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            prevIntent,
            flag
        )

        val nextIntent =
            Intent(baseContext, notificationReceiver?.javaClass ?: NotificationReceiver::class.java)
                .setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            nextIntent,
            flag
        )

        val endIntent =
            Intent(baseContext, notificationReceiver?.javaClass ?: NotificationReceiver::class.java)
                .setAction(ApplicationClass.EXIT)
        val endPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            endIntent,
            flag
        )

        val playIntent =
            Intent(baseContext, notificationReceiver?.javaClass ?: NotificationReceiver::class.java)
                .setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            playIntent,
            flag
        )

        var artist = ""
        modelData.artists_name.forEach { artist += "$it, " }

        val futureTarget = Glide.with(this)
            .asBitmap()
            .load(modelData.thumbnail)
            .timeout(6000)
            .error(R.drawable.logo)
            .placeholder(R.drawable.logo)
            .submit()

        futureTarget.apply {

            Coroutines.io {


                var bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo);
                try {
                    get().let {
                        bitmap = it
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val notification =
                    NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
                        .setContentTitle(modelData.title)
                        .setSmallIcon(R.drawable.logo)
                        .setSilent(true)
                        .setLargeIcon(bitmap)
                        .setStyle(
                            androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(1)
                                .setShowCancelButton(true)
                                .setMediaSession(mediaSession.sessionToken)
                        )
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setOnlyAlertOnce(true)
                        .addAction(R.drawable.ic_prev, "Previous", prevPendingIntent)
                        .addAction(playPauseBtn, "Play", playPendingIntent)
                        .addAction(R.drawable.ic_next, "Next", nextPendingIntent)
                        .addAction(R.drawable.ic_cross, "End", endPendingIntent)
                        .build()

                startForeground(12, notification)
            }
            runnable = Runnable {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val playbackSpeed = if (MainActivity.isPlaying) 1F else 0F
                    mediaSession.setMetadata(
                        MediaMetadataCompat.Builder()
                            .putLong(
                                MediaMetadataCompat.METADATA_KEY_DURATION,
                                mediaPlayer!!.duration.toLong()
                            )
                            .build()
                    )
                    val playBackState = PlaybackStateCompat.Builder()
                        .setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            mediaPlayer!!.currentPosition.toLong(),
                            playbackSpeed
                        )
                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                        .build()
                    mediaSession.setPlaybackState(playBackState)
                    mediaSession.setCallback(object : MediaSessionCompat.Callback() {

                        //called when headphones buttons are pressed
                        //currently only pause or play music on button click
                        override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                            if(MainActivity.isPlaying){
                                //pause music
                                MainActivity.playBtn.setImageResource(R.drawable.play)
                                MainActivity.playBtnShort.setImageResource( R.drawable.play_icon)
                                //NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
                                MainActivity.isPlaying = false
                                mediaPlayer!!.pause()
                                showNotification(R.drawable.play,modelData)
                            }else{
                                //play music

                                MainActivity.playBtn.setImageResource(R.drawable.ic_pause)
                                MainActivity.playBtnShort.setImageResource( R.drawable.pause_icon)
                                //  MainActivity.binding.playerLayout.ivPlayIcon.setImageResource(R.drawable.pause_icon)
                                //  NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
                                MainActivity.isPlaying = true
                                mediaPlayer!!.start()
                                showNotification(R.drawable.ic_pause,modelData)
                            }
                            return super.onMediaButtonEvent(mediaButtonEvent)
                        }

                        override fun onSeekTo(pos: Long) {
                            super.onSeekTo(pos)
                            mediaPlayer!!.seekTo(pos.toInt())
                            val playBackStateNew = PlaybackStateCompat.Builder()
                                .setState(
                                    PlaybackStateCompat.STATE_PLAYING,
                                    mediaPlayer!!.currentPosition.toLong(),
                                    playbackSpeed
                                )
                                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                                .build()
                            mediaSession.setPlaybackState(playBackStateNew)
                        }
                    })




                    Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
                }
            }
            Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
        }
    }

    fun seekBarSetup(){

            runnable = Runnable {
                try {
                    if (mediaPlayer!=null && mediaPlayer!!.isPlaying) {
                        MainActivity.binding.playerLayout.tvPrevTime.text =
                            formatDuration(mediaPlayer!!.currentPosition.toLong())
                        MainActivity.binding.playerLayout.seekbarPlay.progress =
                            mediaPlayer!!.currentPosition
                    }
            }catch (e:Exception){
                e.printStackTrace()
            }
                Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
            }
            Handler(Looper.getMainLooper()).postDelayed(runnable, 0)

    }
    fun playNext(isNext: Boolean) {

        MainActivity.nextSong(MainActivity.MainActivityInstance(), isNext)
    }

    fun closeNotification() {

        // MainActivity.musicService!!.mediaPlayer!!.reset()
        MainActivity.musicService!!.audioManager.abandonAudioFocus(MainActivity.musicService)
        MainActivity.musicService!!.stopForeground(true)
        // MainActivity.musicService!!.getSystemService<NotificationManagerCompat>()?.cancel(12)

    }

    fun stopPlayerService() {

        try {

            if (MainActivity.musicService!!.mediaPlayer!=null && MainActivity.musicService!!.mediaPlayer!!.isPlaying)
                MainActivity.musicService!!.mediaPlayer!!.stop()

        }catch (e:Exception){
            e.printStackTrace()
        }
        MainActivity.musicService!!.audioManager.abandonAudioFocus(MainActivity.musicService)
        MainActivity.musicService!!.stopForeground(true)
        MainActivity.musicService!!.stopSelf()
        MainActivity.musicService!!.mediaPlayer!!.release()
        MainActivity.songId=0
        // MainActivity.musicService!!.getSystemService<NotificationManagerCompat>()?.cancel(12)

    }

    fun addList(list: List<CommonDataModel1>) {
        modelList!!.clear()
        modelList!!.addAll(list)

    }

    fun getList(): ArrayList<CommonDataModel1> {
        return modelList!!
    }

    fun createPlayer() {
        MainActivity.musicService!!.mediaPlayer!!.reset()
        MainActivity.musicService!!.mediaPlayer = MediaPlayer()
        try {

            MainActivity.musicService!!.mediaPlayer!!.setDataSource(MainActivity.arrayList[MainActivity.itemPosition].audio_location)
            MainActivity.musicService!!.mediaPlayer!!.prepare()
            MainActivity.musicService!!.mediaPlayer!!.start()
        }catch (e:Exception){
            e.printStackTrace()
        }
        MainActivity.mModelData?.let {
            MainActivity.musicService!!.showNotification(
                R.drawable.ic_pause,
                it
            )
        }
        MainActivity.songName.text = MainActivity.arrayList[MainActivity.itemPosition].title
    }

    override fun onAudioFocusChange(focusChange: Int) {

        if(focusChange >0){
            //play music
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
        else{
            //pause music
          /*  PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
            NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
            PlayerActivity.isPlaying = true
            mediaPlayer!!.start()
*/
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
    }
}