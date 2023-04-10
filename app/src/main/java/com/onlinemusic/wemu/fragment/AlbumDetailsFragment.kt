package com.onlinemusic.wemu.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.graphics.Insets
import android.media.MediaPlayer
import android.os.*
import android.text.format.DateUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.onlinemusic.wemu.BuildConfig
import com.onlinemusic.wemu.MainActivity
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.adapter.AlbumsAdapter
import com.onlinemusic.wemu.adapter.MoreAlbumsAdapter
import com.onlinemusic.wemu.adapter.PlayListCreatedAdapter
import com.onlinemusic.wemu.adapter.RecentlyplayedAdapter
import com.onlinemusic.wemu.databinding.ActivityAlbumdetailsBinding
import com.onlinemusic.wemu.databinding.DialogAddToPlaylistBinding
import com.onlinemusic.wemu.databinding.SongsBottomsheetBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.CommonDataModel
import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.onlinemusic.wemu.responseModel.FavouritesResponseModel
import com.onlinemusic.wemu.responseModel.add_to_playlist.AddToPlaylistRequestModel
import com.onlinemusic.wemu.responseModel.albumdetails.response.AlbumDetailsResponseModel
import com.onlinemusic.wemu.responseModel.albumdetails.response.DataX
import com.onlinemusic.wemu.responseModel.albumslist.response.AlbumListResponseMainModel
import com.onlinemusic.wemu.responseModel.genredetails.response.GenreDetailsResponseModel
import com.onlinemusic.wemu.responseModel.playlistitems.response.PlaylistCreatedResponseModel
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.utils.Utilities
import com.onlinemusic.wemu.viewmodel.PlayerDataModel1
import com.onlinemusic.wemu.viewmodel.PlayerViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit

class AlbumDetailsFragment : Fragment(), RecentlyplayedAdapter.OnItemClickListener {

    lateinit var activityAlbumdetailsBinding: ActivityAlbumdetailsBinding
    var sessionManager: SessionManager? = null
    lateinit var mainActivity: MainActivity

    var songs_list: RecyclerView?=null
    var recentlyplayedAdapter: RecentlyplayedAdapter?=null

    var btn_back: ImageView? = null
    var album_id: String = ""
    var album_title: String = ""
    var album_category: String = ""
    var album_image: String = ""
    var key: String = ""
    var mProgressDialog: ProgressDialog? = null
    var mLayoutManager: RecyclerView.LayoutManager? = null
    private lateinit var playerViewModel: PlayerViewModel

    // initialise loading state
    var mIsLoading = false
    var mIsLoading1 = false
    var mIsLastPage = false
    var mIsPlayListLastPage = false;
    var mCurrentPlayListPage = 0;
    var mCurrentPage = 0
    lateinit var albumAdapter: AlbumsAdapter

    lateinit var dialog: BottomSheetDialog
    // amount of items you want to load per page
    final var pageSize = 10

    var arrayList: ArrayList<DataX> = ArrayList()

   /* override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_FULLSCREEN
    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityAlbumdetailsBinding =
            DataBindingUtil.inflate(inflater, R.layout.activity_albumdetails, container, false)
        val root = activityAlbumdetailsBinding.root

        playerViewModel = ViewModelProvider(activity as MainActivity)[PlayerViewModel::class.java]

        mainActivity = activity as MainActivity
        dialog = BottomSheetDialog(mainActivity)
        mainActivity.mBottomNavigationView?.visibility = View.GONE
        sessionManager = SessionManager(mainActivity)

        songs_list = activityAlbumdetailsBinding.songsList

        activityAlbumdetailsBinding.btnNotification.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_notification)
        }
        val bundle = this.arguments
        if(bundle != null) {
            album_id = bundle.getInt("album_id").toString()
        } else {
            album_id = ""
        }

        if(bundle != null) {
            album_title = bundle.getString("album_title").toString()
        } else {
            album_title = ""
        }
        if(bundle != null) {
            album_category = bundle.getString("album_category").toString()
        } else {
            album_category = ""
        }

        if (bundle != null) {
            album_image = bundle.getString("album_image").toString()
        } else {
            album_image = ""
        }
         if (bundle != null) {
             key = bundle.getString("key").toString()
        } else {
             key = ""
        }
        if (sessionManager?.getTheme().equals("night")) {
            //fragmentFavouriteBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.black))
            activityAlbumdetailsBinding.demoTxt.setTextColor(getResources().getColor(R.color.white))
            activityAlbumdetailsBinding.wemuPlaylistTxt.setTextColor(getResources().getColor(R.color.white))

            activityAlbumdetailsBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            activityAlbumdetailsBinding.btnBack.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        }
        else
        {

            //fragmentFavouriteBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.white))
            activityAlbumdetailsBinding.demoTxt.setTextColor(getResources().getColor(R.color.white))
            activityAlbumdetailsBinding.wemuPlaylistTxt.setTextColor(getResources().getColor(R.color.white))

            activityAlbumdetailsBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            activityAlbumdetailsBinding.btnBack.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        }

        Glide.with(mainActivity)
            .load(album_image)
            .timeout(6000)
            .error(R.drawable.logo)
            .placeholder(R.drawable.logo)
            .into(activityAlbumdetailsBinding.imageBack)
        activityAlbumdetailsBinding.demoTxt.text = album_title
       // activityAlbumdetailsBinding.wemuPlaylistTxt.text = album_title

        activityAlbumdetailsBinding.btnBack.setOnClickListener {

            mainActivity.onBackPressed()
        }


        if(key.equals("album")) {
            mIsLoading = false;
            mIsLastPage = false;

            recentlyplayedAdapter = RecentlyplayedAdapter(mainActivity, this)
            setupRecyclewrView()
            loadMoreItems(true)
            getAlbumsData()


        }
        else
        {
            activityAlbumdetailsBinding.moreItemsLayout.visibility = View.GONE
            recentlyplayedAdapter = RecentlyplayedAdapter(mainActivity, this)
            setupRecyclewrViewGenre()
            songs_list!!.layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
            getAllGenreData()
        }

        playerViewModel.getCurrentSongId.observe(mainActivity, Observer {
            recentlyplayedAdapter!!.notifyItemRangeChanged(0, recentlyplayedAdapter!!.itemCount)
        })
        return root
    }

    private fun getAlbumsData() {
        if (CheckConnectivity.getInstance(requireContext()).isOnline) {

            showProgressDialog()
            var moreAlbumsAdapter =
                MoreAlbumsAdapter(requireContext())
            val horizontaLayoutManagaer =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            activityAlbumdetailsBinding.recMoreAlbums.layoutManager =
                horizontaLayoutManagaer
            activityAlbumdetailsBinding.recMoreAlbums.adapter =
                moreAlbumsAdapter
            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<AlbumListResponseMainModel?>? = uploadAPIs.getYouMayLikeListData("Bearer "+sessionManager?.getToken(),album_category,"1")
            mcall?.enqueue(object : Callback<AlbumListResponseMainModel?> {
                override fun onResponse(
                    call: Call<AlbumListResponseMainModel?>,
                    response: retrofit2.Response<AlbumListResponseMainModel?>,
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
                                if(result.data!!.data!!.isNotEmpty()) {
                                    activityAlbumdetailsBinding.moreItemsLayout.visibility = View.VISIBLE

                                    var albumArr =
                                        result.data!!.data as ArrayList<com.onlinemusic.wemu.responseModel.albumslist.response.DataX>

                                    moreAlbumsAdapter.updateData(albumArr)
                                   // moreAlbumsAdapter.notifyDataSetChanged()
                                }
                                else
                                {

                                    songs_list!!.layoutParams = RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.MATCH_PARENT,
                                        RelativeLayout.LayoutParams.MATCH_PARENT
                                    )
                                    activityAlbumdetailsBinding.moreItemsLayout.visibility = View.GONE
                                }


                            }





                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<AlbumListResponseMainModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(requireContext(), "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupRecyclewrViewGenre() {
        mLayoutManager = GridLayoutManager(mainActivity, 1)
        songs_list?.layoutManager = mLayoutManager
        songs_list?.itemAnimator = DefaultItemAnimator()
        songs_list?.adapter = recentlyplayedAdapter
    }

    private fun getAllGenreData() {
        arrayList.clear()
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<GenreDetailsResponseModel?>? = uploadAPIs.getGenreDetails(
                "Bearer " + sessionManager?.getToken(),
                album_id

            )
            mcall?.enqueue(object : Callback<GenreDetailsResponseModel?> {
                override fun onResponse(
                    call: Call<GenreDetailsResponseModel?>,
                    response: retrofit2.Response<GenreDetailsResponseModel?>,
                ) {
                    try {
                        if (response.body() != null) {
                            val result = response.body()!!
                            //Log.v("responseannouncement-->", result)
                            /*  val mjonsresponse = JSONObject(result)
                              val data = mjonsresponse.getJSONObject("data")
                              val dataArray = data.getJSONArray("data")*/

                            result.apply {
                                if(data!!.songs!!.isNotEmpty()) {
                                    activityAlbumdetailsBinding.mainLl.visibility = View.VISIBLE
                                    recentlyplayedAdapter?.addToList(
                                        data?.songs!! as List<CommonDataModel1>
                                    )
                                    activityAlbumdetailsBinding.playAllLl.setOnClickListener {
                                        playerViewModel.startSongModel1.value= PlayerDataModel1(1,0, recentlyplayedAdapter!!.getFullList())
                                        recentlyplayedAdapter!!.notifyItemRangeChanged(0,recentlyplayedAdapter!!.getFullList().size)

                                    }
                                }
                                else
                                {
                                    activityAlbumdetailsBinding.songsList.visibility = View.GONE
                                    //Toast.makeText(requireContext(), "no data found", Toast.LENGTH_SHORT).show()
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


                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<GenreDetailsResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(mainActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
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

            if (shouldLoadMore) {
                loadMoreItems(false);
            }
        }
    }

    private fun loadMoreItems(isFirstPage: Boolean) {
        // change loading state
        mIsLoading = true
        mCurrentPage += 1

        // update recycler adapter with next page
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<AlbumDetailsResponseModel?>? = uploadAPIs.getAlbumsData(
                "Bearer " + sessionManager?.getToken(),
                album_id,
                mCurrentPage.toString()
            )
            mcall?.enqueue(object : Callback<AlbumDetailsResponseModel?> {
                override fun onResponse(
                    call: Call<AlbumDetailsResponseModel?>,
                    response: retrofit2.Response<AlbumDetailsResponseModel?>,
                ) {
                    try {
                        if (response.isSuccessful && response.body() != null) {
                            val result = response.body()!!
                            //Log.v("responseannouncement-->", result)
                            /*  val mjonsresponse = JSONObject(result)
                              val data = mjonsresponse.getJSONObject("data")
                              val dataArray = data.getJSONArray("data")*/

                            result.apply {
                                if(data!!.data!!.isNotEmpty()) {
                                    activityAlbumdetailsBinding.mainLl.visibility = View.VISIBLE
                                    if (isFirstPage) recentlyplayedAdapter?.updateList(data?.data!!) else recentlyplayedAdapter?.addToList(
                                        data?.data!!
                                    )
                                    mIsLoading = false
                                    mIsLastPage = mCurrentPage == result.data?.lastPage
                                    activityAlbumdetailsBinding.playAllLl.setOnClickListener {
                                        playerViewModel.startSongModel1.value= PlayerDataModel1(1,0,  recentlyplayedAdapter!!.getFullList())
                                        recentlyplayedAdapter!!.notifyItemRangeChanged(0,recentlyplayedAdapter!!.getFullList().size)

                                    }

                                    activityAlbumdetailsBinding.noData.root.visibility = View.GONE
                                }
                                else
                                {
                                    activityAlbumdetailsBinding.songsList.visibility = View.GONE
                                    activityAlbumdetailsBinding.playAllLl.visibility = View.GONE
                                    activityAlbumdetailsBinding.noData.root.visibility = View.VISIBLE
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
                                    //Toast.makeText(requireContext(), "no data found", Toast.LENGTH_SHORT).show()
                                }

                            }


                        }else{

                            activityAlbumdetailsBinding.songsList.visibility = View.GONE
                            activityAlbumdetailsBinding.playAllLl.visibility = View.GONE
                            activityAlbumdetailsBinding.noData.root.visibility = View.VISIBLE
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


    fun setupRecyclewrView() {
        mLayoutManager = GridLayoutManager(mainActivity, 1)
        songs_list?.layoutManager = mLayoutManager
        songs_list?.addOnScrollListener(recyclerOnScroll)
        songs_list?.itemAnimator = DefaultItemAnimator()
        songs_list?.adapter = recentlyplayedAdapter


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
        //stopPlaying()
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
        /*if (!dialog.isShowing) {
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
        dialog.setCancelable(true)
        dismissDialog=dialog
        dialog.show()
        with(binding){

            playlistAdapter = PlayListCreatedAdapter(context = mainActivity, onItemClickListener = object :
                PlayListCreatedAdapter.OnPlaylistItemClickListener{
                override fun onClick(modelData: List<com.onlinemusic.wemu.responseModel.playlistitems.response.DataX>, position: Int) {
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
    private fun addtoPlaylist(modelData: List<com.onlinemusic.wemu.responseModel.playlistitems.response.DataX>, position: Int, song_id: String)
    {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            //showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<FavouritesResponseModel?>? = uploadAPIs.addtoPlaylist("Bearer "+sessionManager?.getToken(),
                AddToPlaylistRequestModel(song_id.trim().toInt(),modelData[position].id))
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
                .timeout(6000)
                .error(R.drawable.logo)
                .placeholder(R.drawable.logo)
                .into(ivThumbnail)
            Glide.with(mainActivity)
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
            } else {
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

}