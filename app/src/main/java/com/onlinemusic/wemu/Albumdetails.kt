package com.onlinemusic.wemu

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Insets
import android.media.MediaPlayer
import android.os.*
import android.text.format.DateUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.onlinemusic.wemu.adapter.RecentlyplayedAdapter
import com.onlinemusic.wemu.databinding.ActivityAlbumdetailsBinding
import com.onlinemusic.wemu.databinding.SongsBottomsheetBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.CommonDataModel
import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.onlinemusic.wemu.responseModel.albumdetails.response.AlbumDetailsResponseModel
import com.onlinemusic.wemu.responseModel.albumdetails.response.DataX
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.viewmodel.PlayerDataModel1
import com.onlinemusic.wemu.viewmodel.PlayerViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit

class Albumdetails : AppCompatActivity(), RecentlyplayedAdapter.OnItemClickListener {
    lateinit var activityAlbumBinding: ActivityAlbumdetailsBinding

    lateinit var songs_list: RecyclerView
    lateinit var recentlyplayedAdapter: RecentlyplayedAdapter

    var btn_back:ImageView?=null
    var album_id: String =""
    var album_title: String =""
    var album_image: String =""
    var sessionManager: SessionManager? = null
    var mProgressDialog: ProgressDialog? = null
    var mLayoutManager: RecyclerView.LayoutManager?=null

    private lateinit var playerViewModel: PlayerViewModel
    // initialise loading state
    var mIsLoading = false;
    var mIsLastPage = false;
    var mCurrentPage = 0;

    lateinit var dialog: BottomSheetDialog
    // amount of items you want to load per page
    final var pageSize = 10;

    var arrayList:ArrayList<DataX> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_albumdetails)
        activityAlbumBinding = DataBindingUtil.setContentView(this,R.layout.activity_albumdetails)
        album_id= intent.extras!!.getInt("album_id").toString()
        album_title= intent.extras!!.getString("album_title").toString()
        album_image= intent.extras!!.getString("album_image").toString()
        songs_list = findViewById(R.id.songs_list)
        btn_back = findViewById(R.id.btn_back)
        sessionManager = SessionManager(this@Albumdetails)
        Glide.with(this@Albumdetails)
            .load(album_image)
            .timeout(6000)
            .error(R.drawable.logo)
            .placeholder(R.drawable.logo)
            .into(activityAlbumBinding.imageBack)
        activityAlbumBinding.demoTxt.text = album_title
        dialog = BottomSheetDialog(this)


        btn_back?.setOnClickListener {

            onBackPressed()
        }
        mIsLoading = false;
        mIsLastPage = false;
        recentlyplayedAdapter = RecentlyplayedAdapter(this@Albumdetails,this)
        setupRecyclewrView()
       // getAlbumsData()
        loadMoreItems(true)


    }

    val recyclerOnScroll=object :RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            // number of visible items
            val visibleItemCount = recyclerView.layoutManager?.childCount;
            // number of items in layout
            val totalItemCount = recyclerView.layoutManager?.itemCount;
            // the position of first visible item
            val firstVisibleItemPosition =
                (recyclerView.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()

            val isNotLoadingAndNotLastPage = !mIsLoading && !mIsLastPage;
            // flag if number of visible items is at the last
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount!! >= totalItemCount!!;
            // validate non negative values
            val isValidFirstItem = firstVisibleItemPosition >= 0;
            // validate total items are more than possible visible items
            val totalIsMoreThanVisible = totalItemCount >= pageSize;
            // flag to know whether to load more
            val shouldLoadMore = isValidFirstItem && isAtLastItem && totalIsMoreThanVisible && isNotLoadingAndNotLastPage

                    if (shouldLoadMore) loadMoreItems(false);
        }
    }

    private fun loadMoreItems(isFirstPage: Boolean) {
        // change loading state
        mIsLoading = true
        mCurrentPage += 1

        // update recycler adapter with next page
        if (CheckConnectivity.getInstance(this@Albumdetails).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<AlbumDetailsResponseModel?>? = uploadAPIs.getAlbumsData("Bearer "+sessionManager?.getToken(),album_id,mCurrentPage.toString())
            mcall?.enqueue(object : Callback<AlbumDetailsResponseModel?> {
                override fun onResponse(
                    call: Call<AlbumDetailsResponseModel?>,
                    response: retrofit2.Response<AlbumDetailsResponseModel?>,
                ) {
                    try {
                        if (response.body() != null) {
                            val result = response.body()!!
                            //Log.v("responseannouncement-->", result)
                          /*  val mjonsresponse = JSONObject(result)
                            val data = mjonsresponse.getJSONObject("data")
                            val dataArray = data.getJSONArray("data")*/

                            result.apply {

                               if (isFirstPage) recentlyplayedAdapter.updateList(data?.data!!) else recentlyplayedAdapter.addToList(data?.data!!)
                                mIsLoading = false
                                mIsLastPage = mCurrentPage == result.data?.lastPage



                            }





                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<AlbumDetailsResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(this@Albumdetails, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
        /*apiEndpoint.getPagedList(mCurrentPage).enqueue(object : Callback<PagedList<Any?>?> {
            override fun onResponse(
                call: Call<PagedList<Any?>?>?,
                response: Response<PagedList<Any?>?>
            ) {
                val result: PagedList<Any> = response.body()
                if (result == null) return else if (!isFirstPage) mAdapter.addAll(result.getResults()) else mAdapter.setList(
                    result.getResults()
                )
                mIsLoading = false
                mIsLastPage = mCurrentPage == result.getTotalPages()
            }

            override fun onFailure(call: Call<PagedList<Any?>?>, t: Throwable) {
                Log.e("SomeActivity", t.message!!)
            }
        })*/
    }
    fun setupRecyclewrView(){
        mLayoutManager = GridLayoutManager(this@Albumdetails, 1)
        songs_list.layoutManager = mLayoutManager
        songs_list.addOnScrollListener(recyclerOnScroll)
        songs_list.itemAnimator = DefaultItemAnimator()
        songs_list.adapter = recentlyplayedAdapter
    }

    /*private fun getAlbumsData() {
        Log.d("dynamic","hfdhgchg")
        if (CheckConnectivity.getInstance(this@Albumdetails).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<AlbumDetailsResponseModel?>? = uploadAPIs.getAlbumsData("Bearer "+sessionManager?.getToken(),album_id,"1")
            mcall?.enqueue(object : Callback<AlbumDetailsResponseModel?> {
                override fun onResponse(
                    call: Call<AlbumDetailsResponseModel?>,
                    response: retrofit2.Response<AlbumDetailsResponseModel?>,
                ) {
                    try {
                        if (response.body() != null) {
                            val result = response.body()!!.string()
                            Log.v("responseannouncement-->", result)
                            val mjonsresponse = JSONObject(result)
                            val data = mjonsresponse.getJSONObject("data")
                            val dataArray = data.getJSONArray("data")

                            for(i in 0 until dataArray.length())
                            {
                                var j = i+1
                                var song_id = dataArray.getJSONObject(i).getInt("song_id")
                                var title = dataArray.getJSONObject(i).getString("title")
                                var description = dataArray.getJSONObject(i).getString("description")
                                var thumbnail = dataArray.getJSONObject(i).getString("thumbnail")
                                var albumDetailsResponceModel =AlbumDetailsResponceModel(j,song_id.toString().toInt(),title,description,thumbnail)
                                arrayList.add(albumDetailsResponceModel)
                            }
                            if(arrayList.size > 0)
                            {

                                recentlyplayedAdapter.updateList(arrayList)
                            }






                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<AlbumDetailsResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(this@Albumdetails, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }*/
    fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(this@Albumdetails)
            mProgressDialog!!.setMessage("Loading...")
            mProgressDialog!!.isIndeterminate = true
        }
        if(!this.isFinishing)
        mProgressDialog!!.show()
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }

    override fun onClick(modelData: List<CommonDataModel1>, position: Int,type:String) {
        //stopPlaying()

        playerViewModel.startSongModel1.value= PlayerDataModel1(1,position,modelData)
       /* if (!dialog.isShowing) {
            nextTrackCount = 0
            showSong(modelData[position], modelData, position, modelData.size)
        }*/

    }



    //OpenPopup

    var nextTrackCount = 0
    var isPlayLoop = false
    var isPlayRand = false


    private fun showSong(
        modelData: CommonDataModel,
        modelDataList: List<CommonDataModel>,
        mPosition: Int,
        totalLength: Int, trackChangeFromPlayer: Boolean = false
    ) {
        if (modelData.audioLocation.isNullOrEmpty()) {
            Toast.makeText(this, "No audio link provided", Toast.LENGTH_SHORT).show()
            return
        }
        val layoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layerBinding = SongsBottomsheetBinding.inflate(layoutInflater, null, false);

        var position = mPosition
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
        }

        var isFavourate = false

        with(layerBinding) {
            pbLoading.visibility = View.VISIBLE
            ivPlayIcon.visibility = View.GONE
            var x = 0
            var b = false;
            songsTitle.text = modelData.title
            tvTitle.text = modelData.title
            tvSongDesc.text = modelData.description
            tvDesc.text = modelData.description
            songsTitle.isSelected=true
            tvSongDesc.isSelected=true
            tvDesc.isSelected=true
            tvTitle.isSelected=true
            Glide.with(this@Albumdetails)
                .load(modelData.thumbnail)
                .timeout(6000)
                .error(R.drawable.logo)
                .placeholder(R.drawable.logo)
                .into(ivThumbnail)
            Glide.with(this@Albumdetails)
                .load(modelData.thumbnail)
                .timeout(6000)
                .error(R.drawable.logo)
                .placeholder(R.drawable.logo)
                .into(ivLargeThumbnail)

            isPLAYING = false
            stopPlaying()
            if (!trackChangeFromPlayer) {
                rlBottomActivity.visibility = View.VISIBLE
                rlFullScreenActivity.visibility = View.GONE
                (dialog).behavior.peekHeight = 200
            } else {
                rlBottomActivity.visibility = View.GONE
                rlFullScreenActivity.visibility = View.VISIBLE
                //  (dialog).behavior.peekHeight = getScreenWidth(mainActivity as Activity)
                (dialog).behavior.peekHeight = getScreenHeight()

            }
            dialog.setContentView(layerBinding.root)
            if (!dialog.isShowing)
                dialog.show()

            ivPlayIcon.background =
                ContextCompat.getDrawable(this@Albumdetails, R.drawable.ic_pause)
            dialog.behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        dialog.behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        rlBottomActivity.visibility = View.VISIBLE
                        rlFullScreenActivity.visibility = View.GONE
                        (dialog).behavior.peekHeight = 200
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }

            })

            dialog.setOnDismissListener(object : DialogInterface.OnDismissListener {
                override fun onDismiss(p0: DialogInterface?) {
                    p0.apply {

                        rlBottomActivity.visibility = View.VISIBLE
                        rlFullScreenActivity.visibility = View.GONE
                        (dialog).behavior.peekHeight = 200
                        stopPlaying()
                    }
                }

            })

            val mHandler = Handler(Looper.getMainLooper())
            //Seekbar on UI thread

            runOnUiThread(object : Runnable {
                override fun run() {
                    val mCurrentPosition: Int = PlayerInstance().currentPosition / 1000
                    val mCurrentPositionTxt =
                        DateUtils.formatElapsedTime((PlayerInstance().currentPosition / 1000).toLong())
                    seekbarPlay.progress = mCurrentPosition
                    seekbarPlay.max = PlayerInstance().duration / 1000

                    val durationText =
                        DateUtils.formatElapsedTime((PlayerInstance().duration / 1000).toLong()) // converting time in millis to minutes:second format eg 14:15 min


                    tvNextTime.text = durationText.toString()
                    tvPrevTime.text = mCurrentPositionTxt.toString()

                    try {
                        if (PlayerInstance().isPlaying) {
                            pbLoading.visibility = View.GONE
                            ivPlayIcon.visibility = View.VISIBLE
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    if (animationView.isAnimating)
                        animationView.visibility = View.VISIBLE
                    else
                        animationView.visibility = View.GONE
                    mHandler.postDelayed(this, 1000)
                }
            })

            ivPrevTrack.setOnClickListener {
                if (position == 0) {
                    Toast.makeText(
                        this@Albumdetails,
                        "No previous song available",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                } else {
                    if (isPlayRand)
                        position=(modelDataList.indices).random()
                    else
                        position--
                    showSong(modelDataList[position], modelDataList, position, totalLength, true)
                    return@setOnClickListener
                }
            }
            if (isPlayLoop) {
                ivLoopSong.setColorFilter(
                    ContextCompat.getColor(this@Albumdetails, R.color.textyellow),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                ivLoopSong.setColorFilter(
                    ContextCompat.getColor(this@Albumdetails, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            if (isPlayRand) {
                ivRandSong.setColorFilter(
                    ContextCompat.getColor(this@Albumdetails, R.color.textyellow),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                ivRandSong.setColorFilter(
                    ContextCompat.getColor(this@Albumdetails, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            ivNextTrack.setOnClickListener {
                if (position == totalLength) {
                    Toast.makeText(
                        this@Albumdetails,
                        "No next song available",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                } else {

                    if (nextTrackCount == 1) {
                        Toast.makeText(
                            this@Albumdetails,
                            "Pay for premium version to proceed further",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    } else
                        nextTrackCount++

                    if (isPlayRand)
                        position=(modelDataList.indices).random()
                    else
                        position++
                    showSong(modelDataList[position], modelDataList, position, totalLength, true)
                    return@setOnClickListener
                }
            }

            ivFavourate.setOnClickListener {
                if (isFavourate) {
                    ivFavourate.setImageResource(R.drawable.ic_heart)
                    isFavourate = false
                } else {
                    animationView.playAnimation()
                    animationView.visibility = View.VISIBLE
                    ivFavourate.setImageResource(R.drawable.ic_heart_red)
                    isFavourate = true
                }
                /*Handler(Looper.getMainLooper()).postDelayed({
                    animationView.pauseAnimation()
                    animationView.
                }, 2000)*/
            }
            seekbarPlay.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        PlayerInstance().seekTo(progress * 1000);
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            })

            rlBottomActivity.setOnClickListener {
                //Toast.makeText(context, "PRESSED", Toast.LENGTH_SHORT).show()
                rlBottomActivity.visibility = View.GONE
                rlFullScreenActivity.visibility = View.VISIBLE
                //  (dialog).behavior.peekHeight = getScreenWidth(mainActivity as Activity)
                (dialog).behavior.peekHeight = getScreenHeight()
            }


            btnBack.setOnClickListener {
                // Toast.makeText(context, "PRESSED", Toast.LENGTH_SHORT).show()
                rlBottomActivity.visibility = View.VISIBLE
                rlFullScreenActivity.visibility = View.GONE
                (dialog).behavior.peekHeight = 200
            }

            ivPlayPause.setOnClickListener {
                println("SEEK BAR   ${mp!!.currentPosition}")
                PlayerInstance().let { itMP ->

                    vibrate(vib)
                    if (b) {
                        itMP.seekTo(x)
                        itMP.start()
                        ivPlayIcon.background =
                            ContextCompat.getDrawable(this@Albumdetails, R.drawable.ic_pause)
                        ivPlayPause.background =
                            ContextCompat.getDrawable(this@Albumdetails, R.drawable.pause_icon)
                        b = false;
                    } else {
                        x = itMP.currentPosition;
                        itMP.pause();
                        Log.v("log", "" + x);
                        ivPlayIcon.background =
                            ContextCompat.getDrawable(this@Albumdetails, R.drawable.play)
                        ivPlayPause.background =
                            ContextCompat.getDrawable(this@Albumdetails, R.drawable.play_icon)
                        b = true;
                    }

                }
            }




            btnPlayPause.setOnClickListener {
                println("SEEK BAR   ${mp!!.currentPosition}")
                PlayerInstance().let { itMP ->

                    vibrate(vib)
                    if (b) {
                        itMP.seekTo(x)
                        itMP.start()
                        ivPlayIcon.background =
                            ContextCompat.getDrawable(this@Albumdetails, R.drawable.ic_pause)
                        ivPlayPause.background =
                            ContextCompat.getDrawable(this@Albumdetails, R.drawable.pause_icon)
                        b = false;
                    } else {
                        x = itMP.currentPosition;
                        itMP.pause();
                        Log.v("log", "" + x);
                        ivPlayIcon.background =
                            ContextCompat.getDrawable(this@Albumdetails, R.drawable.play)
                        ivPlayPause.background =
                            ContextCompat.getDrawable(this@Albumdetails, R.drawable.play_icon)
                        b = true;
                    }

                }
            }

            ivLoopSong.setOnClickListener {
                if (isPlayLoop) {
                    isPlayLoop = false
                    ivLoopSong.setColorFilter(
                        ContextCompat.getColor(this@Albumdetails, R.color.white),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    );

                } else {
                    isPlayLoop = true
                    ivLoopSong.setColorFilter(
                        ContextCompat.getColor(
                            this@Albumdetails,
                            R.color.textyellow
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    );
                }
            }

            ivRandSong.setOnClickListener {

                if (isPlayRand){
                    isPlayRand=false
                    ivRandSong.setColorFilter(
                        ContextCompat.getColor(this@Albumdetails, R.color.white),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    );

                }else{
                    isPlayRand=true
                    ivRandSong.setColorFilter(
                        ContextCompat.getColor(this@Albumdetails, R.color.textyellow),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    );
                }
            }

            ivPlayPause.background =
                ContextCompat.getDrawable(this@Albumdetails, R.drawable.pause_icon)

            ivPlayIcon.background =
                ContextCompat.getDrawable(this@Albumdetails, R.drawable.ic_pause)

            println(modelData.audioLocation)
            playSongs(modelData.audioLocation)

            PlayerInstance().setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
                override fun onCompletion(p0: MediaPlayer?) {

                    if (position < totalLength) {

                        if (!isPlayLoop) {
                            if (isPlayRand)
                                position=(modelDataList.indices).random()
                            else
                                position++
                        }

                        showSong(
                            modelDataList[position],
                            modelDataList,
                            position,
                            totalLength,
                            true
                        )

                    }
                }

            })

        }
        // RelativeLayout headerLay = (RelativeLayout) dialog.findViewById(R.id.addTask_dialog_header);

    }

    fun getScreenHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }

    fun getScreenWidth(activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.height() - insets.top - insets.bottom
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }

    fun vibrate(vib: Vibrator) {

// Vibrate for 500 milliseconds
// Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            vib!!.vibrate(200)
        }
    }

    var isPLAYING = false

    var mp: MediaPlayer? = null

    fun PlayerInstance(): MediaPlayer {
        if (mp == null)
            mp = MediaPlayer()
        return mp!!
    }

    fun playSongs(audioUrl: String) {


        if (!PlayerInstance().isPlaying) {
            isPLAYING = true
            try {
                mp?.apply {
                    mp?.reset()
                    setDataSource(audioUrl)
                    prepare()
                    start()
                    /* setOnPreparedListener(object :MediaPlayer.OnPreparedListener{
                         override fun onPrepared(p0: MediaPlayer?) {

                         }

                     })*/

                }
            } catch (e: Exception) {
                Log.e("LOG_TAG", e.message.toString())
            }
        } else {
            isPLAYING = false
            stopPlaying()
        }

    }

    private fun stopPlaying() {
        if (mp != null && mp!!.isPlaying) {
            mp?.reset()
            mp = null
        }
    }
}