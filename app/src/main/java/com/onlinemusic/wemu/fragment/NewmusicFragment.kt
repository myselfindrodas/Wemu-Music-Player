package com.onlinemusic.wemu.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Insets
import android.media.MediaPlayer
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.onlinemusic.wemu.BuildConfig
import com.onlinemusic.wemu.MainActivity
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.adapter.NewMusicAdapter
import com.onlinemusic.wemu.adapter.PlayListCreatedAdapter
import com.onlinemusic.wemu.databinding.DialogAddToPlaylistBinding
import com.onlinemusic.wemu.databinding.FragmentNewmusicBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.onlinemusic.wemu.responseModel.FavouritesResponseModel
import com.onlinemusic.wemu.responseModel.add_to_playlist.AddToPlaylistRequestModel
import com.onlinemusic.wemu.responseModel.commonsection.response.CommonDataResponseMainModel
import com.onlinemusic.wemu.responseModel.playlistitems.response.DataX
import com.onlinemusic.wemu.responseModel.playlistitems.response.PlaylistCreatedResponseModel
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.viewmodel.PlayerDataModel1
import com.onlinemusic.wemu.viewmodel.PlayerViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit


class NewmusicFragment : Fragment(),NewMusicAdapter.OnItemClickListener {

    lateinit var fragmentNewmusicBinding: FragmentNewmusicBinding
    lateinit var mainActivity: MainActivity
    var arrayList: ArrayList<CommonDataModel1> = ArrayList()
    var mProgressDialog: ProgressDialog? = null
    var sessionManager: SessionManager? = null
    lateinit var dialog: BottomSheetDialog
    var mLayoutManager: RecyclerView.LayoutManager?=null
    private lateinit var playerViewModel: PlayerViewModel
    // initialise loading state
    var mIsLoading = false;
    var mIsLastPage = false;
    var mIsPlayListLastPage = false;
    var mCurrentPage = 0;
    var mCurrentPlayListPage = 0;

    // amount of items you want to load per page
    final var pageSize = 10;
    lateinit var newMusicAdapter: NewMusicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentNewmusicBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_newmusic,container,false);
        val root = fragmentNewmusicBinding.root
        mainActivity=activity as MainActivity
        playerViewModel = ViewModelProvider(activity as MainActivity)[PlayerViewModel::class.java]

        mainActivity.mBottomNavigationView?.visibility = View.GONE
        sessionManager = SessionManager(mainActivity)
        dialog = BottomSheetDialog(mainActivity)
        fragmentNewmusicBinding.btnNotification.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_notification)
        }
        if (sessionManager?.getTheme().equals("night")) {
            //fragmentFavouriteBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.black))
            fragmentNewmusicBinding.tvTitle.setTextColor(getResources().getColor(R.color.white))
            fragmentNewmusicBinding.tvSubtitle.setTextColor(getResources().getColor(R.color.white))
            fragmentNewmusicBinding.latestSong.setTextColor(getResources().getColor(R.color.white))

            fragmentNewmusicBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentNewmusicBinding.imgback.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        } else {

            //fragmentFavouriteBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.white))
            fragmentNewmusicBinding.tvTitle.setTextColor(getResources().getColor(R.color.black))
            fragmentNewmusicBinding.tvSubtitle.setTextColor(getResources().getColor(R.color.black))
            fragmentNewmusicBinding.latestSong.setTextColor(getResources().getColor(R.color.black))

            fragmentNewmusicBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentNewmusicBinding.imgback.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        }
        fragmentNewmusicBinding.btnBack.setOnClickListener {

            mainActivity.onBackPressed()
        }
        var key_fetch="new"
       // getData(key_fetch)
        mIsLoading = false;
        mIsLastPage = false;
        mIsPlayListLastPage = false;
        newMusicAdapter = NewMusicAdapter(requireContext(),this)

        setupRecyclewrView()
        // getAlbumsData()
        loadMoreItems(true)

        playerViewModel.getCurrentSongId.observe(mainActivity, Observer {
            newMusicAdapter.notifyItemRangeChanged(0,newMusicAdapter.itemCount)
        })
        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        mIsLoading = false
        mIsLastPage = false
        mIsPlayListLastPage = false
        mCurrentPlayListPage = 0
        mCurrentPage = 0
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

            if (shouldLoadMore)
                loadMoreItems(false);
        }
    }
    private fun loadMoreItems(isFirstPage: Boolean) {
        // change loading state
        mIsLoading = true
        mCurrentPage += 1

        // update recycler adapter with next page
        if (CheckConnectivity.getInstance(requireContext()).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<CommonDataResponseMainModel?>? = uploadAPIs.getNewData("Bearer "+sessionManager?.getToken(),"new",mCurrentPage.toString())
            mcall?.enqueue(object : Callback<CommonDataResponseMainModel?> {
                override fun onResponse(
                    call: Call<CommonDataResponseMainModel?>,
                    response: retrofit2.Response<CommonDataResponseMainModel?>,
                ) {
                    try {
                        if (response.body() != null) {
                            hideProgressDialog()
                            val result = response.body()!!
                            //Log.v("responseannouncement-->", result)
                            /*  val mjonsresponse = JSONObject(result)
                              val data = mjonsresponse.getJSONObject("data")
                              val dataArray = data.getJSONArray("data")*/

                            result.apply {
                                fragmentNewmusicBinding.latestSong.text = data!!.data?.size.toString() + " songs"
                                if (isFirstPage) newMusicAdapter.updateList(data?.data!!) else newMusicAdapter.addToList(data?.data!!)
                                mIsLoading = false
                                mIsLastPage = mCurrentPage == result.data?.lastPage




                            }





                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<CommonDataResponseMainModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(requireContext(), "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }


    }

    fun setupRecyclewrView(){
        with(fragmentNewmusicBinding){
            mLayoutManager = GridLayoutManager(mainActivity, 1)
            songsList.addOnScrollListener(recyclerOnScroll)
            songsList.layoutManager = mLayoutManager
            songsList.itemAnimator = DefaultItemAnimator()
            songsList.adapter = newMusicAdapter
        }
    }
    @SuppressLint("SuspiciousIndentation")
    fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(mainActivity)
            mProgressDialog!!.setMessage("Loading...")
            mProgressDialog!!.isIndeterminate = true
        }
        if(!mainActivity.isFinishing)
        mProgressDialog!!.show()
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }

    override fun onClick(modelData: List<CommonDataModel1>, position: Int,type:String) {
        if(type == "Play_Song") {
            playerViewModel.startSongModel1.value = PlayerDataModel1(1, position, modelData)
        }
        else if(type == "Add_To_Playlist")
        {
            showAddToPlayListDialog(modelData[position].song_id.toString())
           // addtoPlaylist(modelData,position)
        }
        else if(type == "Like_Song")
        {
            likeSong(modelData,position)
        }

        else
        {
            shareAppLink(modelData[position].title, modelData[position].audio_location)
        }
/*
        if (!dialog.isShowing) {
            nextTrackCount = 0
            showSong(modelData[position], modelData, position, modelData.size)
        }*/
    }
    private fun likeSong(modelData: List<CommonDataModel1>, position: Int) {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            //showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<FavouritesResponseModel?>? = uploadAPIs.addtoLikeSongs("Bearer "+sessionManager?.getToken(),
                modelData[position].song_id.toString())
            mcall?.enqueue(object : Callback<FavouritesResponseModel?> {
                override fun onResponse(
                    call: Call<FavouritesResponseModel?>,
                    response: retrofit2.Response<FavouritesResponseModel?>,
                ) {
                    try {
                        if (response.body() != null) {

                            val result = response.body()!!
                            /* Log.v("responseannouncement-->", result)
                             val mjonsresponse = JSONObject(result)
                             val data = mjonsresponse.getJSONObject("data")*/
                            result.apply {

                                var status = result.status
                                if(status == true)
                                {
                                    Toast.makeText(mainActivity,"Song has been liked",Toast.LENGTH_SHORT).show()


                                }
                                else
                                {
                                    //Toast.makeText(mainActivity,"Removed from playlist",Toast.LENGTH_SHORT).show()
                                }

                            }






                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    //  hideProgressDialog()
                }

                override fun onFailure(call: Call<FavouritesResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(mainActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }

    }

    lateinit var playlistAdapter: PlayListCreatedAdapter

    var dismissDialog:Dialog?=null
    private fun showAddToPlayListDialog(song_id: String){
        val dialog = Dialog(mainActivity, R.style.DialogSlideAnim)

        dialog.getWindow()?.setBackgroundDrawableResource(R.color.greywhite)
        dialog.getWindow()
            ?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        val binding: DialogAddToPlaylistBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_add_to_playlist, null, false
        )
        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(true)
        dismissDialog=dialog
        dialog.setCancelable(true)
        dialog.show()
        with(binding){

            playlistAdapter = PlayListCreatedAdapter(context = mainActivity, onItemClickListener = object :
                PlayListCreatedAdapter.OnPlaylistItemClickListener{
                override fun onClick(modelData: List<DataX>, position: Int) {
                    addtoPlaylist(modelData,position,song_id)
                    dialog.dismiss()
                }

            })
            var mLayoutManager = GridLayoutManager(mainActivity, 2)

            imgback.setOnClickListener {
                dialog.dismiss()
            }
            mCurrentPlayListPage = 0
            loadMorePlayListItems(true)
            recPlaylist.layoutManager = mLayoutManager
            recPlaylist.addOnScrollListener(recyclerOnScrollPlaylist)
            recPlaylist.itemAnimator = DefaultItemAnimator()
            recPlaylist.adapter = playlistAdapter
        }
        /* val rvTest = dialog.findViewById(R.id.rvTest)
         rvTest.setHasFixedSize(true)
         rvTest.layoutManager = LinearLayoutManager(context)
         rvTest.addItemDecoration(SimpleDividerItemDecoration(context, R.drawable.divider))

         val rvAdapter = DataDialogAdapter(context, rvTestList)
         rvTest.adapter = rvAdapter*/
    }


    val recyclerOnScrollPlaylist = object : RecyclerView.OnScrollListener() {
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

            val isNotLoadingAndNotLastPage = !mIsLoading && !mIsPlayListLastPage;
            // flag if number of visible items is at the last
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount!! >= totalItemCount!!;
            // validate non negative values
            val isValidFirstItem = firstVisibleItemPosition >= 0;
            // validate total items are more than possible visible items
            val totalIsMoreThanVisible = totalItemCount >= pageSize;
            // flag to know whether to load more
            val shouldLoadMore =
                isValidFirstItem && isAtLastItem && totalIsMoreThanVisible && isNotLoadingAndNotLastPage

            if (shouldLoadMore) loadMorePlayListItems(false);
        }
    }


    private fun loadMorePlayListItems(isFirstPage: Boolean) {
        // change loading state
        mIsLoading = true
        mCurrentPlayListPage += 1

        // update recycler adapter with next page
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<PlaylistCreatedResponseModel?>? = uploadAPIs.getCreatedPlaylistData(
                "Bearer " + sessionManager?.getToken(),

                mCurrentPlayListPage.toString()
            )
            mcall?.enqueue(object : Callback<PlaylistCreatedResponseModel?> {
                override fun onResponse(
                    call: Call<PlaylistCreatedResponseModel?>,
                    response: retrofit2.Response<PlaylistCreatedResponseModel?>,
                ) {
                    try {
                        hideProgressDialog()
                        if (response.body() != null) {

                            val result = response.body()!!
                            //Log.v("responseannouncement-->", result)
                            /*  val mjonsresponse = JSONObject(result)
                              val data = mjonsresponse.getJSONObject("data")
                              val dataArray = data.getJSONArray("data")*/
                            Log.d("result", response.body()!!.status.toString())
                            if (result.status == true) {

                                result.apply {

                                    if (data!!.data!!.isNotEmpty()) {

                                        // activityAlbumdetailsBinding.mainLl.visibility = View.VISIBLE
                                        if (isFirstPage) playlistAdapter?.updateList(data?.data!!) else playlistAdapter?.addToList(
                                            data?.data!!
                                        )
                                        mIsLoading = false
                                        mIsPlayListLastPage = mCurrentPlayListPage == result.data?.lastPage
                                    } else if (message == "No songs available") {
                                        Log.d("mbvmbv", "hfvjhvj")
                                        hideProgressDialog()
                                        mIsLoading = false
                                        // activityAlbumdetailsBinding.mainLl.visibility = View.GONE
                                        Toast.makeText(
                                            requireContext(),
                                            "no data found",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        hideProgressDialog()
                                        Toast.makeText(
                                            requireContext(),
                                            "no data found",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                }

                            } else {
                                mIsLoading = false
                                 hideProgressDialog()
                                if (dismissDialog!=null){
                                    dismissDialog?.dismiss()
                                }
                                Toast.makeText(
                                    requireContext(),
                                    "Please create a playlist first!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<PlaylistCreatedResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(mainActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
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
    private fun addtoPlaylist(modelData: List<DataX>, position: Int,song_id: String) {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            //showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<FavouritesResponseModel?>? = uploadAPIs.addtoPlaylist("Bearer "+sessionManager?.getToken(),
                AddToPlaylistRequestModel( song_id.trim().toInt(),modelData[position].id))
            mcall?.enqueue(object : Callback<FavouritesResponseModel?> {
                override fun onResponse(
                    call: Call<FavouritesResponseModel?>,
                    response: retrofit2.Response<FavouritesResponseModel?>,
                ) {
                    try {
                        if (response.body() != null) {

                            val result = response.body()!!
                            /* Log.v("responseannouncement-->", result)
                             val mjonsresponse = JSONObject(result)
                             val data = mjonsresponse.getJSONObject("data")*/
                            result.apply {

                                var status = result.status
                                if(status == true)
                                {
                                    Toast.makeText(mainActivity,"Successfully added into playlist",Toast.LENGTH_SHORT).show()


                                }
                                else
                                {
                                    Toast.makeText(mainActivity,"Removed from playlist",Toast.LENGTH_SHORT).show()
                                }

                            }






                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    //  hideProgressDialog()
                }

                override fun onFailure(call: Call<FavouritesResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(mainActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }



    private fun shareAppLink(subject:String,message:String){
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            var shareMessage = "\nLet me recommend you this application\n\n"
           // shareMessage = "$shareMessage ${message}\n\n"
            shareMessage =
                """
                ${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
                
                """.trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "Choose app"))
        } catch (e: java.lang.Exception) {
            //e.toString();
        }
    }

    //OpenPopup

    var nextTrackCount = 0
    var isPlayLoop = false
    var isPlayRand = false


   /* private fun showSong(
        modelData: CommonDataModel1,
        modelDataList: List<CommonDataModel1>,
        mPosition: Int,
        totalLength: Int, trackChangeFromPlayer: Boolean = false
    ) {
        if (modelData.audio_location.isNullOrEmpty()) {
            Toast.makeText(mainActivity, "No audio link provided", Toast.LENGTH_SHORT).show()
            return
        }
        val layoutInflater =
            mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layerBinding = SongsBottomsheetBinding.inflate(layoutInflater, null, false);

        var position = mPosition
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                mainActivity.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            mainActivity.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
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
            Glide.with(mainActivity)
                .load(modelData.thumbnail)
                .into(ivThumbnail)
            Glide.with(mainActivity)
                .load(modelData.thumbnail)
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
                ContextCompat.getDrawable(mainActivity, R.drawable.ic_pause)
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

            mainActivity.runOnUiThread(object : Runnable {
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
                        mainActivity,
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
                    ContextCompat.getColor(mainActivity, R.color.textyellow),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            else {
                ivLoopSong.setColorFilter(
                    ContextCompat.getColor(mainActivity, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            if (isPlayRand) {
                ivRandSong.setColorFilter(
                    ContextCompat.getColor(mainActivity, R.color.textyellow),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                ivRandSong.setColorFilter(
                    ContextCompat.getColor(mainActivity, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            ivNextTrack.setOnClickListener {
                if (position == totalLength) {
                    Toast.makeText(
                        mainActivity,
                        "No next song available",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                } else {

                    if (nextTrackCount == 1) {
                        Toast.makeText(
                            mainActivity,
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


                addToFavourites(ivFavourate,animationView,modelData.id.toString())
                *//*   if (isFavourate) {
                       ivFavourate.setImageResource(R.drawable.ic_heart)
                       isFavourate = false
                   } else {
                       animationView.playAnimation()
                       animationView.visibility = View.VISIBLE
                       ivFavourate.setImageResource(R.drawable.ic_heart_red)
                       isFavourate = true
                   }*//*
                *//*Handler(Looper.getMainLooper()).postDelayed({
                    animationView.pauseAnimation()
                    animationView.
                }, 2000)*//*
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
                            ContextCompat.getDrawable(mainActivity, R.drawable.ic_pause)
                        ivPlayPause.background =
                            ContextCompat.getDrawable(mainActivity, R.drawable.pause_icon)
                        b = false;
                    } else {
                        x = itMP.currentPosition;
                        itMP.pause();
                        Log.v("log", "" + x);
                        ivPlayIcon.background =
                            ContextCompat.getDrawable(mainActivity, R.drawable.play)
                        ivPlayPause.background =
                            ContextCompat.getDrawable(mainActivity, R.drawable.play_icon)
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
                            ContextCompat.getDrawable(mainActivity, R.drawable.ic_pause)
                        ivPlayPause.background =
                            ContextCompat.getDrawable(mainActivity, R.drawable.pause_icon)
                        b = false;
                    } else {
                        x = itMP.currentPosition;
                        itMP.pause();
                        Log.v("log", "" + x);
                        ivPlayIcon.background =
                            ContextCompat.getDrawable(mainActivity, R.drawable.play)
                        ivPlayPause.background =
                            ContextCompat.getDrawable(mainActivity, R.drawable.play_icon)
                        b = true;
                    }

                }
            }

            ivLoopSong.setOnClickListener {
                if (isPlayLoop) {
                    isPlayLoop = false
                    ivLoopSong.setColorFilter(
                        ContextCompat.getColor(mainActivity, R.color.white),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    );

                } else {
                    isPlayLoop = true
                    ivLoopSong.setColorFilter(
                        ContextCompat.getColor(
                            mainActivity,
                            R.color.textyellow
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    );
                }
            }

            ivRandSong.setOnClickListener {

                if (isPlayRand){
                    isPlayRand=false
                    ivRandSong.setColorFilter(
                        ContextCompat.getColor(mainActivity, R.color.white),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    );

                }else{
                    isPlayRand=true
                    ivRandSong.setColorFilter(
                        ContextCompat.getColor(mainActivity, R.color.textyellow),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    );
                }
            }

            ivPlayPause.background =
                ContextCompat.getDrawable(mainActivity, R.drawable.pause_icon)

            ivPlayIcon.background =
                ContextCompat.getDrawable(mainActivity, R.drawable.ic_pause)

            println(modelData.audio_location)
            playSongs(modelData.audio_location)

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

    }*/

    private fun addToFavourites(
        ivFavourate: ImageView,
        animationView: LottieAnimationView,
        song_id: String
    ) {
        if (CheckConnectivity.getInstance(requireContext()).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<FavouritesResponseModel?>? = uploadAPIs.addtoFavourites("Bearer "+sessionManager?.getToken(),song_id)
            mcall?.enqueue(object : Callback<FavouritesResponseModel?> {
                override fun onResponse(
                    call: Call<FavouritesResponseModel?>,
                    response: retrofit2.Response<FavouritesResponseModel?>,
                ) {
                    try {
                        if (response.body() != null) {

                            val result = response.body()!!
                            /* Log.v("responseannouncement-->", result)
                             val mjonsresponse = JSONObject(result)
                             val data = mjonsresponse.getJSONObject("data")*/
                            result.apply {

                                var status = result.status
                                if(status == true)
                                {
                                    Toast.makeText(requireContext(),"Song has been added from favourites",Toast.LENGTH_SHORT).show()
                                    animationView.playAnimation()
                                    animationView.visibility = View.VISIBLE
                                    ivFavourate.setImageResource(R.drawable.ic_heart_red)
                                    //isFavourate = true

                                }
                                else
                                {
                                    Toast.makeText(requireContext(),"Song has been removed from favourites",Toast.LENGTH_SHORT).show()
                                    ivFavourate.setImageResource(R.drawable.ic_heart)
                                }


                            }






                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<FavouritesResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(requireContext(), "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
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