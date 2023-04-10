package com.onlinemusic.wemu.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onlinemusic.wemu.BuildConfig
import com.onlinemusic.wemu.MainActivity
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.adapter.ArtistSearchItemsAdapter
import com.onlinemusic.wemu.adapter.PlayListCreatedAdapter
import com.onlinemusic.wemu.adapter.SearchAlbumItemAdapter
import com.onlinemusic.wemu.adapter.SearchSongItemsAdapter
import com.onlinemusic.wemu.databinding.DialogAddToPlaylistBinding
import com.onlinemusic.wemu.databinding.FragmentSearchMoreElementBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.onlinemusic.wemu.responseModel.FavouritesResponseModel
import com.onlinemusic.wemu.responseModel.add_to_playlist.AddToPlaylistRequestModel
import com.onlinemusic.wemu.responseModel.playlistitems.response.DataX
import com.onlinemusic.wemu.responseModel.playlistitems.response.PlaylistCreatedResponseModel
import com.onlinemusic.wemu.responseModel.searchalbum.response.SearchAlbumsMainResponseModel
import com.onlinemusic.wemu.responseModel.searchartist.response.SearchSingerResponseMainModel
import com.onlinemusic.wemu.responseModel.songsmoresearch.response.SearchSongsMainResponseModel
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.viewmodel.PlayerDataModel1
import com.onlinemusic.wemu.viewmodel.PlayerViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit


class SearchMoreElementFragment : Fragment(), SearchSongItemsAdapter.OnItemSongClickListener {
    lateinit var fragmentSearchMoreElementBinding: FragmentSearchMoreElementBinding
    lateinit var searchAlbumItemAdapter: SearchAlbumItemAdapter
    lateinit var artistSearchItemsAdapter: ArtistSearchItemsAdapter
    lateinit var recentlyplayedAdapter: SearchSongItemsAdapter
    var mProgressDialog: ProgressDialog? = null
    private lateinit var playerViewModel: PlayerViewModel

    var mLayoutManager: RecyclerView.LayoutManager? = null

    // initialise loading state
    var mIsLoading = false;
    var mIsLastPage = false;
    var mCurrentPage = 0;

    // amount of items you want to load per page
    final var pageSize = 10;
    lateinit var mainActivity: MainActivity
    var sessionManager: SessionManager? = null
    var searchKey = ""
    var searchType = ""
    var pageType = ""
    var mIsPlayListLastPage = false;
    var mCurrentPlayListPage = 0;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentSearchMoreElementBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_search_more_element,
                container,
                false
            );
        val root = fragmentSearchMoreElementBinding.root
        mainActivity = activity as MainActivity
        sessionManager = SessionManager(mainActivity)
        playerViewModel = ViewModelProvider(activity as MainActivity)[PlayerViewModel::class.java]
        fragmentSearchMoreElementBinding.btnBack.setOnClickListener {
            mainActivity.onBackPressed()
        }
        val bundle = this.arguments
        if (bundle != null) {
            searchKey = bundle.getString("search_key").toString()
            searchType = bundle.getString("key").toString()
            pageType = bundle.getString("type").toString()
        } else {

        }
        if (sessionManager?.getTheme().equals("night")) {
            //fragmentFavouriteBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.black))
            fragmentSearchMoreElementBinding.tvTitle.setTextColor(getResources().getColor(R.color.white))


            fragmentSearchMoreElementBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentSearchMoreElementBinding.imgback.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        } else {

            //fragmentFavouriteBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.white))
            fragmentSearchMoreElementBinding.tvTitle.setTextColor(getResources().getColor(R.color.black))

            fragmentSearchMoreElementBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentSearchMoreElementBinding.imgback.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        }
        searchAlbumItemAdapter = SearchAlbumItemAdapter(requireContext())
        artistSearchItemsAdapter = ArtistSearchItemsAdapter(requireContext())
        recentlyplayedAdapter = SearchSongItemsAdapter(requireContext(), this)
        fragmentSearchMoreElementBinding.btnNotification.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_notification)
        }
        mIsLoading = false;
        mIsLastPage = false;
        if (pageType == "more") {

        } else {
            fragmentSearchMoreElementBinding.tvTitle.text = "New Updates"
        }
        if (searchType == "album") {

            setupRecyclewrView()
            // getAlbumsData()
            loadMoreItems(true)
        } else if (searchType == "singer") {

            setupRecyclewrViewSinger()
            loadMoreItemsSinger(true)
        } else {
            setupRecyclewrViewSongs()
            loadMoreSongsItems(true)
        }

        Log.d("searchKey", searchKey)
        Log.d("searchType", searchType)
        return root
    }

    private fun setupRecyclewrViewSongs() {
        val mLayoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(mainActivity, 1)
        fragmentSearchMoreElementBinding.recList.layoutManager = mLayoutManager
        fragmentSearchMoreElementBinding.recList.addOnScrollListener(recyclerOnScroll)
        fragmentSearchMoreElementBinding.recList.itemAnimator = DefaultItemAnimator()
        fragmentSearchMoreElementBinding.recList.setAdapter(recentlyplayedAdapter)
    }

    private fun setupRecyclewrViewSinger() {
        val mLayoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(mainActivity, 1)
        fragmentSearchMoreElementBinding.recList.layoutManager = mLayoutManager
        fragmentSearchMoreElementBinding.recList.addOnScrollListener(recyclerOnScroll)
        fragmentSearchMoreElementBinding.recList.itemAnimator = DefaultItemAnimator()
        fragmentSearchMoreElementBinding.recList.setAdapter(artistSearchItemsAdapter)
    }

    private fun loadMoreSongsItems(isFirstPage: Boolean) {
        // change loading state
        mIsLoading = true
        mCurrentPage += 1

        // update recycler adapter with next page
        if (CheckConnectivity.getInstance(requireContext()).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<SearchSongsMainResponseModel?>? = uploadAPIs.getSearchedSongsList(
                "Bearer " + sessionManager?.getToken(),
                searchKey,
                mCurrentPage.toString()
            )
            mcall?.enqueue(object : Callback<SearchSongsMainResponseModel?> {
                override fun onResponse(
                    call: Call<SearchSongsMainResponseModel?>,
                    response: retrofit2.Response<SearchSongsMainResponseModel?>,
                ) {
                    try {
                        if (response.body() != null) {
                            val result = response.body()!!
                            //Log.v("responseannouncement-->", result)
                            /*  val mjonsresponse = JSONObject(result)
                              val data = mjonsresponse.getJSONObject("data")
                              val dataArray = data.getJSONArray("data")*/

                            result.apply {
                                if (pageType == "more") {
                                    fragmentSearchMoreElementBinding.tvTitle.text =
                                        "More Songs Result"
                                } else {
                                    fragmentSearchMoreElementBinding.tvTitle.text = "New Updates"
                                }


                                if (isFirstPage) recentlyplayedAdapter.updateList(data?.data!!) else recentlyplayedAdapter.addToList(
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

                override fun onFailure(call: Call<SearchSongsMainResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(requireContext(), "OOPS! No Internet Connection", Toast.LENGTH_SHORT)
                .show()
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

    private fun loadMoreItemsSinger(isFirstPage: Boolean) {
        // change loading state
        mIsLoading = true
        mCurrentPage += 1

        // update recycler adapter with next page
        if (CheckConnectivity.getInstance(requireContext()).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<SearchSingerResponseMainModel?>? = uploadAPIs.getSearchedArtist(
                "Bearer " + sessionManager?.getToken(), searchKey, mCurrentPage.toString()
            )
            mcall?.enqueue(object : Callback<SearchSingerResponseMainModel?> {
                override fun onResponse(
                    call: Call<SearchSingerResponseMainModel?>,
                    response: retrofit2.Response<SearchSingerResponseMainModel?>,
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
                                fragmentSearchMoreElementBinding.tvTitle.text = "More Singers"
                                if (isFirstPage) artistSearchItemsAdapter.updateList(data?.data!!) else artistSearchItemsAdapter.addToList(
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

                override fun onFailure(call: Call<SearchSingerResponseMainModel?>, t: Throwable) {
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
        fragmentSearchMoreElementBinding.recList.layoutManager = mLayoutManager
        fragmentSearchMoreElementBinding.recList.addOnScrollListener(recyclerOnScroll)
        fragmentSearchMoreElementBinding.recList.itemAnimator = DefaultItemAnimator()
        fragmentSearchMoreElementBinding.recList.setAdapter(searchAlbumItemAdapter)
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
                if (searchType == "album") {
                    loadMoreItems(false);
                } else if (searchType == "singer") {
                    loadMoreItemsSinger(false)

                } else {
                    loadMoreSongsItems(false)
                }


            }


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
            val mcall: Call<SearchAlbumsMainResponseModel?>? = uploadAPIs.getSearchedAlbum(
                "Bearer " + sessionManager?.getToken(),
                searchKey,
                mCurrentPage.toString()
            )
            mcall?.enqueue(object : Callback<SearchAlbumsMainResponseModel?> {
                override fun onResponse(
                    call: Call<SearchAlbumsMainResponseModel?>,
                    response: retrofit2.Response<SearchAlbumsMainResponseModel?>,
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
                                if (pageType == "more") {
                                    fragmentSearchMoreElementBinding.tvTitle.text = "More Albums"
                                } else {
                                    fragmentSearchMoreElementBinding.tvTitle.text = "New Updates"
                                }

                                if (isFirstPage) searchAlbumItemAdapter.updateList(data?.data!!) else searchAlbumItemAdapter.addToList(
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

                override fun onFailure(call: Call<SearchAlbumsMainResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(requireContext(), "OOPS! No Internet Connection", Toast.LENGTH_SHORT)
                .show()
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        mIsLoading = false
        mIsLastPage = false
        mCurrentPage = 0
        mIsPlayListLastPage = false
        mCurrentPlayListPage = 0
        searchKey = searchKey
        searchType = searchType
    }

    @SuppressLint("SuspiciousIndentation")
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
            //addtoPlaylist(modelData,position)
        } else if (type == "Like_Song") {
            likeSong(modelData, position)
        } else {
            shareAppLink(modelData[position].title, modelData[position].audio_location)
        }
    }

    private fun likeSong(modelData: List<CommonDataModel1>, position: Int) {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            //showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<FavouritesResponseModel?>? = uploadAPIs.addtoLikeSongs(
                "Bearer " + sessionManager?.getToken(),
                modelData[position].song_id.toString()
            )
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
                                        "Song has been liked",
                                        Toast.LENGTH_SHORT
                                    ).show()


                                } else {
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


    lateinit var playlistAdapter: PlayListCreatedAdapter

    var dismissDialog: Dialog? = null
    private fun showAddToPlayListDialog(song_id: String) {
        val dialog = Dialog(mainActivity, R.style.DialogSlideAnim)

        dialog.window?.setBackgroundDrawableResource(R.color.greywhite)
        dialog.window
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
        dismissDialog = dialog
        dialog.show()
        with(binding) {

            playlistAdapter =
                PlayListCreatedAdapter(context = mainActivity, onItemClickListener = object :
                    PlayListCreatedAdapter.OnPlaylistItemClickListener {
                    override fun onClick(modelData: List<DataX>, position: Int) {
                        addtoPlaylist(modelData, position, song_id)
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
                                        mIsPlayListLastPage =
                                            mCurrentPlayListPage == result.data?.lastPage
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
                                        if (dismissDialog != null) {
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


    private fun addtoPlaylist(modelData: List<DataX>, position: Int, song_id: String) {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            //showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<FavouritesResponseModel?>? = uploadAPIs.addtoPlaylist(
                "Bearer " + sessionManager?.getToken(),
                AddToPlaylistRequestModel(song_id.trim().toInt(), modelData[position].id)
            )
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
}