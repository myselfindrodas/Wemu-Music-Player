package com.onlinemusic.wemu.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.onlinemusic.wemu.MainActivity
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.adapter.ArtistSongsAdapter
import com.onlinemusic.wemu.databinding.FragmentArtistSongsBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.onlinemusic.wemu.responseModel.artistsongs.response.ArtistSongsResponseMainModel
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager

import com.onlinemusic.wemu.viewmodel.PlayerDataModel1
import com.onlinemusic.wemu.viewmodel.PlayerViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.onlinemusic.wemu.BuildConfig
import com.onlinemusic.wemu.adapter.AlbumSingerAdapter
import com.onlinemusic.wemu.adapter.PlayListCreatedAdapter
import com.onlinemusic.wemu.databinding.DialogAddToPlaylistBinding

import com.onlinemusic.wemu.responseModel.FavouritesResponseModel
import com.onlinemusic.wemu.responseModel.add_to_playlist.AddToPlaylistRequestModel
import com.onlinemusic.wemu.responseModel.artist.songsdetails.response.ArtistAlbumResponseModel
import com.onlinemusic.wemu.responseModel.artist.songsdetails.response.DataX
import com.onlinemusic.wemu.responseModel.playlistitems.response.PlaylistCreatedResponseModel

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class ArtistSongsFragment : Fragment(),ArtistSongsAdapter.OnItemClickListener {

    lateinit var fragmentArtistSongsBinding: FragmentArtistSongsBinding
    var sessionManager: SessionManager? = null
    var mIsLoading = false
    var mIsPlaylistLoading = false
    var mIsLastPage = false
    var mCurrentPage = 0
    var album_id: String = ""
    var album_title: String = ""
    var album_image: String = ""
    lateinit var dialog: BottomSheetDialog
    // amount of items you want to load per page
    final var pageSize = 15
    private lateinit var playerViewModel: PlayerViewModel
    lateinit var mainActivity: MainActivity
    lateinit var artistSongsAdapter: ArtistSongsAdapter
    var mProgressDialog: ProgressDialog? = null
    var mLayoutManager: RecyclerView.LayoutManager? = null
    lateinit var albumSingerAdapter: AlbumSingerAdapter
    var arrayList:ArrayList<DataX> = ArrayList()
    var mIsPlayListLastPage = false;
    var mCurrentPlayListPage = 0;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentArtistSongsBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_artist_songs, container, false)
        val root = fragmentArtistSongsBinding.root
        mainActivity = activity as MainActivity
        sessionManager = SessionManager(mainActivity)
        mIsLoading = false
        mIsLastPage = false
        playerViewModel = ViewModelProvider(activity as MainActivity)[PlayerViewModel::class.java]
        val bundle = this.arguments
        if (bundle != null) {
            album_id = bundle.getInt("album_id").toString()
        } else {
            album_id = ""
        }

        if (bundle != null) {
            album_title = bundle.getString("album_title").toString()
        } else {
            album_title = ""
        }

        if (bundle != null) {
            album_image = bundle.getString("album_image").toString()
        } else {
            album_image = ""
        }
        Glide.with(mainActivity)
            .load(album_image)
            .timeout(6000)
            .error(R.drawable.logo)
            .placeholder(R.drawable.logo)
            .into(fragmentArtistSongsBinding.imageBack)
        fragmentArtistSongsBinding.demoTxt.text = album_title

        artistSongsAdapter = ArtistSongsAdapter(mainActivity, this)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclewrView()
        getAlbums(album_id)
        loadMoreItems(true)

        playerViewModel.getCurrentSongId.observe(mainActivity, Observer {
            artistSongsAdapter.notifyItemRangeChanged(0,artistSongsAdapter.itemCount)
        })
        fragmentArtistSongsBinding.btnNotification.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_notification)
        }
        fragmentArtistSongsBinding.btnBack.setOnClickListener {
            mainActivity.onBackPressed()
        }
    }
    private fun getAlbums(albumId: String) {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {
           // arrayList.clear()
            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<ArtistAlbumResponseModel?>? =
                uploadAPIs.getArtistAlbumsList("Bearer " + sessionManager?.getToken(), albumId,"1")
            mcall?.enqueue(object :Callback<ArtistAlbumResponseModel?>
            {
                override fun onResponse(
                    call: Call<ArtistAlbumResponseModel?>,
                    response: Response<ArtistAlbumResponseModel?>
                ) {
                if(response.isSuccessful && response.body()!!.status== true)
                {
                    fragmentArtistSongsBinding.albumsTxt.visibility = View.VISIBLE
                    fragmentArtistSongsBinding.recAlbums.visibility = View.VISIBLE
                    arrayList = response.body()!!.data?.data as ArrayList<DataX>
                    if (arrayList.isEmpty()){
                        fragmentArtistSongsBinding.albumsTxt.visibility = View.GONE
                        fragmentArtistSongsBinding.recAlbums.visibility = View.GONE
                    }
                   /* if(arrayList.size > 5)
                    {
                        fragmentArtistSongsBinding.albumsSeeMoreTxt.visibility = View.VISIBLE
                    }
                    else
                    {
                        fragmentArtistSongsBinding.albumsSeeMoreTxt.visibility = View.GONE

                    }*/
                    var albumSingerAdapter = AlbumSingerAdapter(mainActivity,arrayList)
                    val horizontaLayoutManagaer =
                        LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                    fragmentArtistSongsBinding.recAlbums.layoutManager =
                        horizontaLayoutManagaer
                    fragmentArtistSongsBinding.recAlbums.adapter = albumSingerAdapter
                    albumSingerAdapter.notifyDataSetChanged()

                }
                    else
                {
                    fragmentArtistSongsBinding.albumsTxt.visibility = View.GONE
                    fragmentArtistSongsBinding.recAlbums.visibility = View.GONE


                }

                }

                override fun onFailure(call: Call<ArtistAlbumResponseModel?>, t: Throwable) {

                }

            })

        } else {
            Toast.makeText(mainActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }

    }

    fun setupRecyclewrView() {
        mLayoutManager = GridLayoutManager(mainActivity, 1)
        fragmentArtistSongsBinding.songsList.addOnScrollListener(recyclerOnScroll)
        fragmentArtistSongsBinding.songsList.layoutManager = mLayoutManager
        fragmentArtistSongsBinding.songsList.itemAnimator = DefaultItemAnimator()
        fragmentArtistSongsBinding.songsList.adapter = artistSongsAdapter


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
            if (!recyclerView.canScrollVertically(1)) {
                if (shouldLoadMore)
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
            val mcall: Call<ArtistSongsResponseMainModel?>? = uploadAPIs.getArtistSongData(
                "Bearer " + sessionManager?.getToken(),
                album_id,
                mCurrentPage.toString()
            )
            mcall?.enqueue(object : Callback<ArtistSongsResponseMainModel?> {
                override fun onResponse(
                    call: Call<ArtistSongsResponseMainModel?>,
                    response: retrofit2.Response<ArtistSongsResponseMainModel?>,
                ) {
                    try {
                        if (response.isSuccessful) {

                            if (response.body() != null) {
                                val result = response.body()!!
                                //Log.v("responseannouncement-->", result)
                                /*  val mjonsresponse = JSONObject(result)
                                  val data = mjonsresponse.getJSONObject("data")
                                  val dataArray = data.getJSONArray("data")*/

                                result.apply {
                                    /*if (data == null || data.data.isNullOrEmpty()){

                                      //  fragmentArtistSongsBinding.noData.root.visibility=View.VISIBLE
                                        fragmentArtistSongsBinding.playAllLl.visibility=View.GONE
                                        hideProgressDialog()
                                        return@apply
                                    }*/
                                    if(status==true && data!!.data!!.isNotEmpty()) {
                                        fragmentArtistSongsBinding.mainLl.visibility = View.VISIBLE
                                        if (isFirstPage) artistSongsAdapter?.updateList(data?.data!!) else artistSongsAdapter?.addToList(
                                            data?.data!!
                                        )
                                        pageSize=result.data?.perPage!!.toInt()
                                        mIsLastPage = mCurrentPage == result.data?.lastPage
                                        mIsLoading = false
                                        fragmentArtistSongsBinding.playAllLl.setOnClickListener {
                                            playerViewModel.startSongModel1.value =
                                                PlayerDataModel1(
                                                    1,
                                                    0,
                                                    artistSongsAdapter.getAllList()
                                                )
                                            artistSongsAdapter!!.notifyItemRangeChanged(0,artistSongsAdapter!!.getAllList().size)

                                        }

                                        fragmentArtistSongsBinding.noData.root.visibility=View.GONE
                                    }
                                    else
                                    {
                                        fragmentArtistSongsBinding.songsList.visibility = View.GONE
                                        fragmentArtistSongsBinding.songsTxt.visibility = View.GONE
                                        fragmentArtistSongsBinding.playAllLl.visibility=View.GONE
                                        fragmentArtistSongsBinding.noData.root.visibility=View.VISIBLE
                                        Toast.makeText(mainActivity, "no songs found", Toast.LENGTH_SHORT).show()
                                        /* Utilities.alertDialogUtil(requireContext(),"Login","no musiclist found",
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
                                                         mainActivity.onBackPressed()

                                                     }
                                                 }

                                             })*/
                                        //Toast.makeText(requireContext(), "no data found", Toast.LENGTH_SHORT).show()
                                    }

                                }


                            }
                        }else{
                            fragmentArtistSongsBinding.songsList.visibility = View.GONE
                            fragmentArtistSongsBinding.songsTxt.visibility = View.GONE
                            fragmentArtistSongsBinding.playAllLl.visibility=View.GONE
                            fragmentArtistSongsBinding.noData.root.visibility=View.VISIBLE
                            Toast.makeText(mainActivity, response.errorBody().toString(), Toast.LENGTH_SHORT).show()

                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<ArtistSongsResponseMainModel?>, t: Throwable) {
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
    override fun onDestroyView() {
        super.onDestroyView()
        mIsLoading = false
        mIsLastPage = false
        mIsPlayListLastPage = false
        mCurrentPlayListPage = 0
        mCurrentPage = 0
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
        if(type=="Play_Song") {
            playerViewModel.startSongModel1.value = PlayerDataModel1(1, position, modelData)
        }
        else if(type=="Add_To_Playlist")
        {

            showAddToPlayListDialog(modelData[position].song_id.toString())
           // addtoPlaylist(modelData,position)
        }
        else if(type == "Like_Song")
        {
            likeSong(modelData,position)
        }
        else if (type == "Share_Song")
        {
            shareAppLink(modelData[position].title, modelData[position].audio_location)
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

    private fun shareAppLink(subject:String,message:String){
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            var shareMessage = "\nLet me recommend you this application\n\n"
            shareMessage = "$shareMessage ${message}\n\n"
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

            val isNotLoadingAndNotLastPage = !mIsPlaylistLoading && !mIsPlayListLastPage;
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
        mIsPlaylistLoading = true
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
                                        mIsPlaylistLoading = false
                                        mIsPlayListLastPage = mCurrentPlayListPage == result.data?.lastPage
                                    } else if (message == "No songs available") {
                                        Log.d("mbvmbv", "hfvjhvj")
                                        hideProgressDialog()
                                        mIsPlaylistLoading = false
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
                                mIsPlaylistLoading = false
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


    private fun addtoPlaylist(modelData: List<com.onlinemusic.wemu.responseModel.playlistitems.response.DataX>, position: Int, song_id: String) {
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

}