package com.onlinemusic.wemu.fragment


import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.SearchView
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
import com.onlinemusic.wemu.adapter.PlayListCreatedAdapter
import com.onlinemusic.wemu.adapter.SearchAlbumsAdapter
import com.onlinemusic.wemu.adapter.SearchArtistAdapter
import com.onlinemusic.wemu.adapter.SearchSongsAdapter
import com.onlinemusic.wemu.databinding.DialogAddToPlaylistBinding
import com.onlinemusic.wemu.databinding.FragmentSearchBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.onlinemusic.wemu.responseModel.FavouritesResponseModel
import com.onlinemusic.wemu.responseModel.add_to_playlist.AddToPlaylistRequestModel
import com.onlinemusic.wemu.responseModel.playlistitems.response.DataX
import com.onlinemusic.wemu.responseModel.playlistitems.response.PlaylistCreatedResponseModel
import com.onlinemusic.wemu.responseModel.search.response.SearchResponseModel
import com.onlinemusic.wemu.responseModel.searchcategory.response.Album
import com.onlinemusic.wemu.responseModel.searchcategory.response.Artist
import com.onlinemusic.wemu.responseModel.searchcategory.response.SearchCategoryResponseModel
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.utils.Utilities
import com.onlinemusic.wemu.viewmodel.PlayerDataModel1
import com.onlinemusic.wemu.viewmodel.PlayerViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit


class SearchFragment : Fragment(), SearchSongsAdapter.OnItemClickListener {

    companion object {
        var search_string = ""
    }

    lateinit var fragmentSearchBinding: FragmentSearchBinding
    var mProgressDialog: ProgressDialog? = null
    var sessionManager: SessionManager? = null
    var mLayoutManager: RecyclerView.LayoutManager? = null

    //lateinit var searchAdapter: SearchResultAdapter
    lateinit var mainActivity: MainActivity
    private lateinit var playerViewModel: PlayerViewModel

    var searchArraylist: ArrayList<CommonDataModel1> = ArrayList()
    var albumArraylist: ArrayList<Album> = ArrayList()
    var artistArraylist: ArrayList<Artist> = ArrayList()
    var searchKey = ""

    var mIsPlayListLastPage = false;
    var mCurrentPlayListPage = 0;
    var mIsLoading = false
    final var pageSize = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getActivity()?.getWindow()?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentSearchBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);
        val root = fragmentSearchBinding.root
        playerViewModel = ViewModelProvider(activity as MainActivity)[PlayerViewModel::class.java]
        mainActivity = activity as MainActivity
        sessionManager = SessionManager(mainActivity)
        if (sessionManager?.getTheme().equals("night")) {
            // fragmentSearchBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.black))
            fragmentSearchBinding.tvTitle.setTextColor(getResources().getColor(R.color.white))
            fragmentSearchBinding.tvSubtitle.setTextColor(getResources().getColor(R.color.white))
            fragmentSearchBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentSearchBinding.imgback.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentSearchBinding.btnViewallAlbums.setTextColor(getResources().getColor(R.color.white))
            fragmentSearchBinding.btnViewallArtist.setTextColor(getResources().getColor(R.color.white))
            fragmentSearchBinding.btnViewallSongs.setTextColor(getResources().getColor(R.color.white))
            fragmentSearchBinding.tvArtist.setTextColor(getResources().getColor(R.color.white))
            fragmentSearchBinding.tvAlbums.setTextColor(getResources().getColor(R.color.white))
            fragmentSearchBinding.tvSongs.setTextColor(getResources().getColor(R.color.white))

            val searchEditText: EditText =
                fragmentSearchBinding.svSearch.findViewById(androidx.appcompat.R.id.search_src_text)
            searchEditText.setTextColor(resources.getColor(R.color.white))
            searchEditText.setHintTextColor(resources.getColor(R.color.white))


        } else {
            // fragmentSearchBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.white))
            fragmentSearchBinding.tvTitle.setTextColor(getResources().getColor(R.color.black))
            fragmentSearchBinding.tvSubtitle.setTextColor(getResources().getColor(R.color.black))
            fragmentSearchBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentSearchBinding.imgback.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentSearchBinding.btnViewallAlbums.setTextColor(getResources().getColor(R.color.black))
            fragmentSearchBinding.btnViewallArtist.setTextColor(getResources().getColor(R.color.black))
            fragmentSearchBinding.btnViewallSongs.setTextColor(getResources().getColor(R.color.black))
            fragmentSearchBinding.tvArtist.setTextColor(getResources().getColor(R.color.black))
            fragmentSearchBinding.tvAlbums.setTextColor(getResources().getColor(R.color.black))
            fragmentSearchBinding.tvSongs.setTextColor(getResources().getColor(R.color.black))
            val searchEditText: EditText =
                fragmentSearchBinding.svSearch.findViewById(androidx.appcompat.R.id.search_src_text)
            searchEditText.setTextColor(resources.getColor(R.color.white))
            searchEditText.setHintTextColor(resources.getColor(R.color.white))
        }
        // fragmentSearchBinding.svSearch.requestFocus()

        //searchAdapter = SearchResultAdapter(mainActivity,this)

        mainActivity.mBottomNavigationView.visibility = View.GONE
        fragmentSearchBinding.svSearch.setQueryHint("Looking for...");
        //fragmentSearchBinding.svSearch.requestFocus()


        //fragmentSearchBinding.edtFocus.requestFocus()
        fragmentSearchBinding.btnViewallAlbums.setOnClickListener {
            if (searchKey == "") {

            } else {
                val bundle = Bundle()
                bundle.putString("search_key", searchKey)
                bundle.putString("key", "album")
                bundle.putString("type", "more")

                val navController = Navigation.findNavController(it)
                navController.navigate(R.id.nav_search_more, bundle)
            }
        }
        fragmentSearchBinding.btnViewallArtist.setOnClickListener {
            if (searchKey == "") {

            } else {
                val bundle = Bundle()
                bundle.putString("search_key", searchKey)
                bundle.putString("key", "singer")
                bundle.putString("type", "more")
                val navController = Navigation.findNavController(it)
                navController.navigate(R.id.nav_search_more, bundle)
            }
        }

        fragmentSearchBinding.btnViewallSongs.setOnClickListener {
            if (searchKey == "") {

            } else {
                val bundle = Bundle()
                bundle.putString("search_key", searchKey)
                bundle.putString("key", "songs")
                bundle.putString("type", "more")

                val navController = Navigation.findNavController(it)
                navController.navigate(R.id.nav_search_more, bundle)
            }
        }

        fragmentSearchBinding.svSearch.requestFocus()
        setupRecyclewrView()
        fragmentSearchBinding.btnNotification.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_notification)
        }
        fragmentSearchBinding.btnBack.setOnClickListener {
            mainActivity.onBackPressed()
        }
        fragmentSearchBinding.svSearch.setOnQueryTextFocusChangeListener(OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                val imm =
                    mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            }
        })
        fragmentSearchBinding.svSearch.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                // getSearchData(newText.toString())

                if (newText!!.length > 2) {
                    getUpdatedSearchData(newText.toString())


                } else {
                    fragmentSearchBinding.mainLl.visibility = View.GONE
                }

                return false
            }

        })
        return root


    }

    private fun getUpdatedSearchData(keyword: String) {
        searchArraylist.clear()
        albumArraylist.clear()
        artistArraylist.clear()
        if (CheckConnectivity.getInstance(requireContext()).isOnline) {
            fragmentSearchBinding.mainLl.visibility = View.GONE

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<SearchCategoryResponseModel?>? =
                uploadAPIs.getUpdatedSearchData("Bearer " + sessionManager?.getToken(), keyword)
            mcall?.enqueue(object : Callback<SearchCategoryResponseModel?> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<SearchCategoryResponseModel?>,
                    response: retrofit2.Response<SearchCategoryResponseModel?>,
                ) {
                    try {
                        if (response.isSuccessful && response.body() != null) {

                            //hideProgressDialog()
                            val result = response.body()!!
                            //Log.v("responseannouncement-->", result)
                            /*  val mjonsresponse = JSONObject(result)
                              val data = mjonsresponse.getJSONObject("data")
                              val dataArray = data.getJSONArray("data")*/
                            if (result.status == true) {


                                fragmentSearchBinding.mainLl.visibility = View.VISIBLE
                                searchKey = keyword
                                search_string = keyword
                                try {

                                    // if (result.data!!.songs.toString()!="{}" && result.data!!.songs!!.data!!.isNotEmpty())
                                    searchArraylist =
                                        result.data!!.songs!!.data as ArrayList<CommonDataModel1>
                                    val songCount =
                                        result.data!!.songs!!.total
                                    if (searchArraylist.isNotEmpty()) {
                                        fragmentSearchBinding.tvSongs.visibility = View.VISIBLE
                                        fragmentSearchBinding.btnViewallSongs.visibility =
                                            View.VISIBLE
                                        fragmentSearchBinding.tvSongs.text =
                                            "Songs ($songCount results)"
                                        val searchSongsAdapter = SearchSongsAdapter(
                                            mainActivity,
                                            searchArraylist,
                                            this@SearchFragment
                                        )
                                        mLayoutManager = GridLayoutManager(mainActivity, 1)
                                        fragmentSearchBinding.recSongs.layoutManager =
                                            mLayoutManager
                                        // recSearch.addOnScrollListener(recyclerOnScroll)
                                        fragmentSearchBinding.recSongs.itemAnimator =
                                            DefaultItemAnimator()
                                        fragmentSearchBinding.recSongs.adapter = searchSongsAdapter
                                        searchSongsAdapter.notifyDataSetChanged()
                                    } else {
                                        fragmentSearchBinding.tvSongs.visibility = View.GONE
                                        fragmentSearchBinding.btnViewallSongs.visibility = View.GONE

                                    }

                                } catch (e: Exception) {
                                    e.printStackTrace()

                                    fragmentSearchBinding.tvSongs.visibility = View.GONE
                                    fragmentSearchBinding.btnViewallSongs.visibility = View.GONE

                                }


                                try {

                                    albumArraylist = result.data?.albums as ArrayList<Album>
                                    if (albumArraylist.isNotEmpty()) {
                                        fragmentSearchBinding.tvAlbums.visibility = View.VISIBLE
                                        fragmentSearchBinding.btnViewallAlbums.visibility =
                                            View.VISIBLE
                                        fragmentSearchBinding.recAlbums.visibility = View.VISIBLE
                                        fragmentSearchBinding.tvAlbums.text =
                                            "Albums (" + albumArraylist.size + (" results)")

                                        var searchAlbumsAdapter =
                                            SearchAlbumsAdapter(mainActivity, albumArraylist)
                                        mLayoutManager = GridLayoutManager(mainActivity, 1)

                                        fragmentSearchBinding.recAlbums.layoutManager =
                                            mLayoutManager
                                        fragmentSearchBinding.recAlbums.itemAnimator =
                                            DefaultItemAnimator()
                                        fragmentSearchBinding.recAlbums.adapter =
                                            searchAlbumsAdapter
                                        searchAlbumsAdapter.notifyDataSetChanged()
                                    } else {
                                        fragmentSearchBinding.tvAlbums.visibility = View.GONE
                                        fragmentSearchBinding.btnViewallAlbums.visibility =
                                            View.GONE
                                        fragmentSearchBinding.recAlbums.visibility = View.GONE
                                    }

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    fragmentSearchBinding.tvAlbums.visibility = View.GONE
                                    fragmentSearchBinding.btnViewallAlbums.visibility = View.GONE
                                    fragmentSearchBinding.recAlbums.visibility = View.GONE
                                }


                                try {

                                    artistArraylist = result.data?.artists as ArrayList<Artist>
                                    if (artistArraylist.isNotEmpty()) {
                                        fragmentSearchBinding.tvArtist.visibility = View.VISIBLE
                                        fragmentSearchBinding.btnViewallArtist.visibility =
                                            View.VISIBLE

                                        fragmentSearchBinding.tvArtist.text =
                                            "Artist (" + artistArraylist.size + (" results)")


                                        val searchArtistAdapter =
                                            SearchArtistAdapter(mainActivity, artistArraylist)
                                        mLayoutManager = GridLayoutManager(mainActivity, 1)

                                        fragmentSearchBinding.recSearch.layoutManager =
                                            mLayoutManager
                                        fragmentSearchBinding.recSearch.itemAnimator =
                                            DefaultItemAnimator()
                                        fragmentSearchBinding.recSearch.adapter =
                                            searchArtistAdapter
                                        searchArtistAdapter.notifyDataSetChanged()

                                    } else {
                                        fragmentSearchBinding.tvArtist.visibility = View.GONE
                                        fragmentSearchBinding.btnViewallArtist.visibility =
                                            View.GONE
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    fragmentSearchBinding.tvArtist.visibility = View.VISIBLE
                                    fragmentSearchBinding.btnViewallArtist.visibility =
                                        View.VISIBLE
                                }


                            } else {
                                Toast.makeText(mainActivity, "no data found", Toast.LENGTH_SHORT)
                                    .show()
                            }


                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }

                    hideProgressDialog()
                }

                override fun onFailure(call: Call<SearchCategoryResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(requireContext(), "OOPS! No Internet Connection", Toast.LENGTH_SHORT)
                .show()
        }


    }


    fun setupRecyclewrView() {
        with(fragmentSearchBinding) {

            mLayoutManager = GridLayoutManager(mainActivity, 1)
            recSearch.layoutManager = mLayoutManager
            // recSearch.addOnScrollListener(recyclerOnScroll)
            recSearch.itemAnimator = DefaultItemAnimator()
            // recSearch.adapter = searchAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val imm = mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun getSearchData(keyword: String) {
        Log.d("dynamic", "hfdhgchg")
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            // showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<SearchResponseModel?>? =
                uploadAPIs.getSearchData("Bearer " + sessionManager?.getToken(), keyword)
            mcall?.enqueue(object : Callback<SearchResponseModel?> {
                override fun onResponse(
                    call: Call<SearchResponseModel?>,
                    response: retrofit2.Response<SearchResponseModel?>,
                ) {
                    try {
                        if (response.body() != null) {
                            val result = response.body()!!

                            result.apply {
                                if (data?.data!!.size > 0) {
                                    fragmentSearchBinding.tvSubtitle.text =
                                        data?.data!!.size.toString() + " songs"
                                    //searchAdapter.updateList(data?.data!!)
                                } else {
                                    Utilities.alertDialogUtil(requireContext(),
                                        "Login",
                                        "no musiclist found",
                                        isCancelable = false,
                                        isPositive = false,
                                        isNegetive = true,
                                        isNeutral = false,
                                        "Ok",
                                        "",
                                        "",
                                        object : Utilities.OnItemClickListener {
                                            override fun onItemClickAction(
                                                type: Int,
                                                dialogInterface: DialogInterface
                                            ) {
                                                if (type == 2) {
                                                    dialogInterface.dismiss()
                                                }
                                            }

                                        })
                                }
                            }
                            // Log.v("responseannouncement-->", result)
                            /* val mjonsresponse = JSONObject(result)
                             val data = mjonsresponse.getJSONObject("data")
                             val dataArray = data.getJSONArray("data")*/

                            /*   for(i in 0 until dataArray.length())
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
                               }*/


                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    // hideProgressDialog()
                }

                override fun onFailure(call: Call<SearchResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(mainActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
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

    /*override fun onClick(modelData: List<CommonDataModel1>, position: Int,type:String) {
        if(type == "Play_Song") {
            playerViewModel.startSongModel1.value = PlayerDataModel1(1, position, modelData)
        }
        else if(type == "Add_To_Playlist")
        {
            addtoPlaylist(modelData,position)
        }
        else
        {
            shareAppLink(modelData[position].title, modelData[position].audio_location)
        }
    }
*/


    lateinit var playlistAdapter: PlayListCreatedAdapter

    var dismissDialog: Dialog? = null
    private fun showAddToPlayListDialog(song_id: String) {
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
                                // hideProgressDialog()
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
            Toast.makeText(mainActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT)
                .show()
        }

    }

    private fun shareAppLink(subject: String, message: String) {
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

    override fun onClick(modelData: List<CommonDataModel1>, position: Int, type: String) {
        Log.d("artist_name", modelData[position].artists_name.toString())
        if (type == "Play_Song") {
            playerViewModel.startSongModel1.value = PlayerDataModel1(1, position, modelData)
        } else if (type == "Add_To_Playlist") {
            showAddToPlayListDialog(modelData[position].song_id.toString())
            // addtoPlaylist(modelData,position)
        } else if (type == "Like_Song") {

            likeSong(modelData, position)
            // addtoPlaylist(modelData,position)
        } else {
            shareAppLink(modelData[position].title, modelData[position].audio_location)
        }
    }

}