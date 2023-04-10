package com.onlinemusic.wemu.fragment

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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.onlinemusic.wemu.BuildConfig
import com.onlinemusic.wemu.MainActivity
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.adapter.CommonAdapter
import com.onlinemusic.wemu.adapter.PlayListCreatedAdapter
import com.onlinemusic.wemu.databinding.DialogAddToPlaylistBinding
import com.onlinemusic.wemu.databinding.FragmentRecentlyplayedBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.onlinemusic.wemu.responseModel.FavouritesResponseModel
import com.onlinemusic.wemu.responseModel.add_to_playlist.AddToPlaylistRequestModel
import com.onlinemusic.wemu.responseModel.commonsection.response.CommonDataResponseMainModel
import com.onlinemusic.wemu.responseModel.playlistitems.response.DataX
import com.onlinemusic.wemu.responseModel.playlistitems.response.PlaylistCreatedResponseModel
import com.onlinemusic.wemu.responseModel.recent_play.RecentPlayDataResponseMainModel
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.viewmodel.PlayerDataModel1
import com.onlinemusic.wemu.viewmodel.PlayerViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit

class RecentlyplayedFragment : Fragment(), CommonAdapter.OnItemClickListener {

    lateinit var fragmentRecentlyplayedBinding: FragmentRecentlyplayedBinding
    var keyItem = ""
    var key_fetch = ""
    var sessionManager: SessionManager? = null
    var mProgressDialog: ProgressDialog? = null
    lateinit var commonAdapter: CommonAdapter
    var arrayList: ArrayList<CommonDataModel1> = ArrayList()
    lateinit var dialog: BottomSheetDialog
    lateinit var mainActivity: MainActivity
    private lateinit var playerViewModel: PlayerViewModel
    var mIsPlayListLastPage = false;
    var mCurrentPlayListPage = 0;

    // initialise loading state
    var mIsLoading = false;
    var mIsLastPage = false;
    var mCurrentPage = 0;

    // amount of items you want to load per page
    final var pageSize = 10;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getActivity()?.getWindow()?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentRecentlyplayedBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_recentlyplayed, container, false);
        val root = fragmentRecentlyplayedBinding.root

        playerViewModel = ViewModelProvider(activity as MainActivity)[PlayerViewModel::class.java]
        mainActivity = activity as MainActivity
        dialog = BottomSheetDialog(mainActivity)
        sessionManager = SessionManager(mainActivity)
        val bundle = this.arguments
        if (bundle != null) {
            keyItem = bundle.getString("key").toString()
        } else {
            keyItem = ""
        }
        if (keyItem.equals("popular")) {
            key_fetch = "popular"
            fragmentRecentlyplayedBinding.tvTitle.text = "Popular Songs"
        } else if (keyItem.equals("recent")) {
            key_fetch = "recent"
            fragmentRecentlyplayedBinding.tvTitle.text = "Recent Songs"


        } else if (keyItem.equals("new_release")) {
            key_fetch = "new"
            fragmentRecentlyplayedBinding.tvTitle.text = "New Release Songs"

        } else if (keyItem.equals("trending")) {
            key_fetch = "trending"
            fragmentRecentlyplayedBinding.tvTitle.text = "Trending Songs"

        } else if (keyItem.equals("recommended")) {
            key_fetch = "recomended"
            fragmentRecentlyplayedBinding.tvTitle.text = "Recommended Songs"


        } /*else if (keyItem.equals("new_albums")) {
            key_fetch = "recomended"
        } else if (keyItem.equals("new_music")) {
            key_fetch = "new_music"
            fragmentRecentlyplayedBinding.tvTitle.text = "New Music"

        }*/ else {

        }
        fragmentRecentlyplayedBinding.btnNotification.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_notification)
        }
        if (sessionManager?.getTheme().equals("night")) {
            //fragmentFavouriteBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.black))
            fragmentRecentlyplayedBinding.tvTitle.setTextColor(getResources().getColor(R.color.white))


            fragmentRecentlyplayedBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentRecentlyplayedBinding.imgback.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        } else {

            //fragmentFavouriteBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.white))
            fragmentRecentlyplayedBinding.tvTitle.setTextColor(getResources().getColor(R.color.black))

            fragmentRecentlyplayedBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentRecentlyplayedBinding.imgback.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        }

        fragmentRecentlyplayedBinding.btnBack.setOnClickListener {

            mainActivity.onBackPressed()
        }

        //getData(key_fetch)


        mIsLoading = false;
        mIsLastPage = false;
        commonAdapter = CommonAdapter(requireContext(), this)

        setupRecyclewrView()
        // getAlbumsData()

        if (key_fetch=="recent")
            getRecentPlay()
        else
            loadMoreItems(true)

        playerViewModel.getCurrentSongId.observe(mainActivity, Observer {
            commonAdapter.notifyItemRangeChanged(0,commonAdapter.itemCount)
        })

        return root

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
            val mcall: Call<CommonDataResponseMainModel?>? = uploadAPIs.getNewData(
                "Bearer " + sessionManager?.getToken(),
                key_fetch,
                mCurrentPage.toString()
            )
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

                                if (isFirstPage) commonAdapter.updateList(data?.data!!) else commonAdapter.addToList(
                                    data?.data!!
                                )
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
            Toast.makeText(requireContext(), "OOPS! No Internet Connection", Toast.LENGTH_SHORT)
                .show()
        }


    }
    private fun getRecentPlay () {

        // update recycler adapter with next page
        if (CheckConnectivity.getInstance(requireContext()).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<RecentPlayDataResponseMainModel?>? = uploadAPIs.getRecentPlayData(
                "Bearer " + sessionManager?.getToken()
            )
            mcall?.enqueue(object : Callback<RecentPlayDataResponseMainModel?> {
                override fun onResponse(
                    call: Call<RecentPlayDataResponseMainModel?>,
                    response: retrofit2.Response<RecentPlayDataResponseMainModel?>,
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

                              commonAdapter.updateList(data!!)

                            }


                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<RecentPlayDataResponseMainModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(requireContext(), "OOPS! No Internet Connection", Toast.LENGTH_SHORT)
                .show()
        }


    }

    private fun setupRecyclewrView() {
        val mLayoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(mainActivity, 1)
        fragmentRecentlyplayedBinding.recRecent.layoutManager = mLayoutManager
        if ( key_fetch == "trending")
            fragmentRecentlyplayedBinding.recRecent.addOnScrollListener(recyclerOnScroll)
        fragmentRecentlyplayedBinding.recRecent.itemAnimator =
            DefaultItemAnimator()
        fragmentRecentlyplayedBinding.recRecent.adapter = commonAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mIsLoading = false
        mIsLastPage = false
        mIsPlayListLastPage = false
        mCurrentPlayListPage = 0
        mCurrentPage = 0
    }

    val recyclerOnScroll = object : RecyclerView.OnScrollListener() {
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
            val shouldLoadMore =
                isValidFirstItem && isAtLastItem && totalIsMoreThanVisible && isNotLoadingAndNotLastPage

            if (shouldLoadMore)
                loadMoreItems(false);
        }
    }


/*    private fun getData(key_fetch: String) {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {
            arrayList.clear()
            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<ResponseBody?>? =
                uploadAPIs.getData("Bearer " + sessionManager?.getToken(), key_fetch)
            mcall?.enqueue(object : Callback<ResponseBody?>, CommonAdapter.OnItemClickListener {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: retrofit2.Response<ResponseBody?>,
                ) {
                    try {
                        if (response.body() != null) {
                            val result = response.body()!!.string()
                            Log.v("responseannouncement-->", result)
                            val mjonsresponse = JSONObject(result)
                            val status = mjonsresponse.getBoolean("status")
                            val message = mjonsresponse.getString("message")
                            val data = mjonsresponse.getJSONObject("data")
                            val dataArray = data.getJSONArray("data")
                        *//*    val audioLocation =
                                dataArray.getJSONObject(6).getString("audio_location")*//*
                          //  println(audioLocation + dataArray.length())
                            for (i in 0 until dataArray.length()) {

                                val j = i + 1

                                val song_id = dataArray.getJSONObject(i).getInt("song_id")
                                val title = dataArray.getJSONObject(i).getString("title")
                                val description =
                                    dataArray.getJSONObject(i).getString("description")
                                val thumbnail = dataArray.getJSONObject(i).getString("thumbnail")
                                val audioLocation =
                                    dataArray.getJSONObject(i).getString("audio_location")
                               // println(audioLocation)
                                val views_count =   dataArray.getJSONObject(i).getInt("views_count")
                                val like_count =dataArray.getJSONObject(i).getInt("like_count")
                                val play_count =dataArray.getJSONObject(i).getInt("play_count")
                                var arraySingers : ArrayList<String> = ArrayList()
                                var artist_name = dataArray.getJSONObject(i).getJSONArray("artists_name")

                                for (i in 0 until artist_name.length())
                                {
                                    arraySingers.add(artist_name.get(i).toString())
                                }
                                val commonDataModel = CommonDataModel1(
                                    j,
                                    song_id.toString().toInt(),
                                    title,
                                    description,
                                    thumbnail,
                                    audioLocation,
                                    views_count,
                                    like_count,
                                    play_count,
                                    arraySingers
                                )
                                if (audioLocation.isNotEmpty())
                                    arrayList.add(commonDataModel)


                            }

                            if (arrayList.size > 0) {
                                commonAdapter = CommonAdapter(
                                    mainActivity,
                                    arrayList,
                                    this@RecentlyplayedFragment
                                )
                                val mLayoutManager: RecyclerView.LayoutManager =
                                    GridLayoutManager(mainActivity, 1)
                                fragmentRecentlyplayedBinding.recRecent.layoutManager = mLayoutManager
                                fragmentRecentlyplayedBinding.recRecent.itemAnimator =
                                    DefaultItemAnimator()
                                fragmentRecentlyplayedBinding.recRecent.adapter = commonAdapter
                                commonAdapter.notifyDataSetChanged()
                            } else {
                                Utilities.alertDialogUtil(requireContext(),"Login","no musiclist found",
                                    isCancelable = false,
                                    isPositive = false,
                                    isNegetive = true,
                                    isNeutral = false,"Ok","","",object : Utilities.OnItemClickListener{
                                        override fun onItemClickAction(
                                            type: Int,
                                            dialogInterface: DialogInterface
                                        ) {
                                            if (type==2){
                                                dialogInterface.dismiss()
                                            }
                                        }

                                    })
                            }


                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }

                override fun onClick(modelData: List<CommonDataModel1>, position: Int,type:String) {


                }
            })
        } else {
            Toast.makeText(mainActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }*/

    fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(mainActivity)
            mProgressDialog!!.setMessage("Loading...")
            mProgressDialog!!.isIndeterminate = true
        }
        if (!mainActivity.isFinishing)
            mProgressDialog!!.show()
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }

    override fun onClick(modelData: List<CommonDataModel1>, position: Int, type: String) {
        if (type == "Play_Song") {
            playerViewModel.startSongModel1.value = PlayerDataModel1(1, position, modelData)
        } else if (type == "Add_To_Playlist") {

            showAddToPlayListDialog(modelData[position].song_id.toString())
           // addtoPlaylist(modelData, position)
        }else if (type == "Like_Song") {

            likeSong(modelData,position)
           // addtoPlaylist(modelData, position)
        } else {

            shareAppLink(modelData[position].title, modelData[position].audio_location)
        }
        /* stopPlaying()
         if (!dialog.isShowing) {
             nextTrackCount = 0
             showSong(modelData[position], modelData, position, modelData.size-1)
         }*/

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
        dialog.setCancelable(true)
        dismissDialog=dialog
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

                            } else {
                                mIsLoading = false
                                 hideProgressDialog()
                                Toast.makeText(
                                    requireContext(),
                                    "no data found",
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
            val mcall: Call<FavouritesResponseModel?>? = uploadAPIs.addtoPlaylist(
                "Bearer " + sessionManager?.getToken(),
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
                                if (status == true) {
                                    Toast.makeText(
                                        mainActivity,
                                        "Successfully added into playlist",
                                        Toast.LENGTH_SHORT
                                    ).show()


                                } else {
                                    Toast.makeText(
                                        mainActivity,
                                        "Removed from playlist",
                                        Toast.LENGTH_SHORT
                                    ).show()
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

    private fun shareAppLink(subject: String, message: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            var shareMessage = "\nLet me recommend you this application\n\n"
          //  shareMessage = "$shareMessage ${message}\n\n"
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


    var nextTrackCount = 0
    var isPlayLoop = false
    var isPlayRand = false


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