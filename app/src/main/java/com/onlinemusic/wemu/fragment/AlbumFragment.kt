package com.onlinemusic.wemu.fragment

import android.app.ProgressDialog

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onlinemusic.wemu.MainActivity

import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.adapter.AlbumsAdapter

import com.onlinemusic.wemu.databinding.FragmentAlbumBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.albumslist.response.AlbumListResponseMainModel
import com.onlinemusic.wemu.responseModel.dashboard.response.NewAlbum
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit

class AlbumFragment : Fragment() {

    lateinit var fragmentAlbumBinding: FragmentAlbumBinding
    lateinit var albumAdapter: AlbumsAdapter
    var sessionManager: SessionManager? = null
    var mProgressDialog: ProgressDialog? = null
    var arrayList : ArrayList<NewAlbum> = ArrayList()
    lateinit var mainActivity: MainActivity
    var mLayoutManager: RecyclerView.LayoutManager?=null
    // initialise loading state
    var mIsLoading = false;
    var mIsLastPage = false;
    var mCurrentPage = 0;

    // amount of items you want to load per page
    final var pageSize = 10;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentAlbumBinding =
            DataBindingUtil.inflate(inflater,R.layout.fragment_album,container,false)
        val root = fragmentAlbumBinding.root
        mainActivity=activity as MainActivity

        sessionManager = SessionManager(mainActivity)

        mainActivity.mBottomNavigationView?.visibility = View.GONE
        fragmentAlbumBinding.btnNotification.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_notification)
        }
        if (sessionManager?.getTheme().equals("night")) {

           // fragmentAlbumBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.black))
            fragmentAlbumBinding.tvTitle.setTextColor(getResources().getColor(R.color.white))
            fragmentAlbumBinding.tvSubtitle.setTextColor(getResources().getColor(R.color.white))

            fragmentAlbumBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentAlbumBinding.imgback.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        }
        else
        {


          //  fragmentAlbumBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.white))
            fragmentAlbumBinding.tvTitle.setTextColor(getResources().getColor(R.color.black))
            fragmentAlbumBinding.tvSubtitle.setTextColor(getResources().getColor(R.color.black))

            fragmentAlbumBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentAlbumBinding.imgback.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        }

        fragmentAlbumBinding.btnBack.setOnClickListener {

            mainActivity.onBackPressed()
        }
        mIsLoading = false;
        mIsLastPage = false;
        albumAdapter = AlbumsAdapter(requireContext())

        setupRecyclewrView()
        // getAlbumsData()
        loadMoreItems(true)
        //getAlbumsData()

        return root
    }

    private fun setupRecyclewrView() {
        val mLayoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(mainActivity, 3)
        fragmentAlbumBinding.recAlbums.layoutManager = mLayoutManager
        fragmentAlbumBinding.recAlbums.addOnScrollListener(recyclerOnScroll)
        fragmentAlbumBinding.recAlbums.itemAnimator = DefaultItemAnimator()
        fragmentAlbumBinding.recAlbums.setAdapter(albumAdapter)
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
        if (CheckConnectivity.getInstance(requireContext()).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<AlbumListResponseMainModel?>? = uploadAPIs.getAlbumListData("Bearer "+sessionManager?.getToken(),"0",mCurrentPage.toString())
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

                                if (isFirstPage) albumAdapter.updateList(data?.data!!) else albumAdapter.addToList(data?.data!!)
                                mIsLoading = false
                                mIsLastPage = mCurrentPage == result.data?.lastPage




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

  /*  private fun getAlbumsData() {
        Log.d("dynamic","hfdhgchg")
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {
            arrayList.clear()
            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<AlbumResponseModel?>? = uploadAPIs.getAlbumData("Bearer "+sessionManager?.getToken())
            mcall?.enqueue(object : Callback<AlbumResponseModel?> {
                override fun onResponse(
                    call: Call<AlbumResponseModel?>,
                    response: retrofit2.Response<AlbumResponseModel?>,
                ) {
                    try {
                        if (response.body() != null) {

                            val result = response.body()!!
                            *//* Log.v("responseannouncement-->", result)
                             val mjonsresponse = JSONObject(result)
                             val data = mjonsresponse.getJSONObject("data")*//*
                            result.apply {

                                val newAlbumSongsArr = data!!.newAlbum
                                for(i in  newAlbumSongsArr!!)
                                {

                                   // var song_id = i.song_id
                                    var title = i.title
                                    var thumbnail = i.thumbnail
                                    var newAlbum =NewAlbum(i.categoryId,i.description,i.palbumId,i.thumbnail,i.title,i.userId)
                                    arrayList.add(newAlbum)
                                }
                                if(arrayList.size > 0)
                                {
                                    fragmentAlbumBinding.tvSubtitle.text=arrayList.size.toString()+"  albums"
                                    albumAdapter = AlbumsAdapter(mainActivity,arrayList)


                                    albumAdapter.notifyDataSetChanged()
                                }
                                else
                                {
                                    Utilities.alertDialogUtil(requireContext(),"Login","no albums found",
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

                override fun onFailure(call: Call<AlbumResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
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
        if(!mainActivity.isFinishing)
        mProgressDialog!!.show()
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        mIsLoading = false
        mIsLastPage = false
        mCurrentPage = 0
    }

}