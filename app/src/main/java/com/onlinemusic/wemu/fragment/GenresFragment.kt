package com.onlinemusic.wemu.fragment

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onlinemusic.wemu.MainActivity
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.adapter.GenreAdapter
import com.onlinemusic.wemu.databinding.FragmentGenresBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.genres.response.DataX
import com.onlinemusic.wemu.responseModel.genres.response.GenresDataResponseModel
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface

import com.onlinemusic.wemu.session.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit

class GenresFragment : Fragment() {

    lateinit var fragmentGenresBinding: FragmentGenresBinding
    var sessionManager: SessionManager? = null
    lateinit var mainActivity: MainActivity
    var mProgressDialog: ProgressDialog? = null
    // initialise loading state
    var mIsLoading = false
    var mIsLastPage = false
    var mCurrentPage = 0


    final var pageSize = 10

    var arrayList: ArrayList<DataX> = ArrayList()
    lateinit var genreAdapter: GenreAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getActivity()?.getWindow()?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentGenresBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_genres,container,false);
        val root = fragmentGenresBinding.root
        mainActivity=activity as MainActivity
        sessionManager = SessionManager(mainActivity)

        if (sessionManager?.getTheme().equals("night")){
           // fragmentGenresBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.black))
            fragmentGenresBinding.tvTitle.setTextColor(getResources().getColor(R.color.white))
            fragmentGenresBinding.tvSubtitle.setTextColor(getResources().getColor(R.color.white))
            fragmentGenresBinding.btnNotification.setColorFilter(ContextCompat.getColor(mainActivity, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY)
        }else{
           // fragmentGenresBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.white))
            fragmentGenresBinding.tvTitle.setTextColor(getResources().getColor(R.color.black))
            fragmentGenresBinding.tvSubtitle.setTextColor(getResources().getColor(R.color.black))
            fragmentGenresBinding.btnNotification.setColorFilter(ContextCompat.getColor(mainActivity, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY)

        }
        fragmentGenresBinding.btnNotification.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_notification)
        }
        genreAdapter = GenreAdapter(mainActivity)
        setupRecyclewrView()
        // getAlbumsData()
        loadMoreItems(true)


        return root
    }

    override fun onResume() {
        super.onResume()
        mCurrentPage = 0
        mIsLoading = false
        mIsLastPage = false
        loadMoreItems(true)
        mainActivity.mBottomNavigationView.visibility=View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        mCurrentPage = 0
        mIsLoading = false
        mIsLastPage = false
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

            if (shouldLoadMore) loadMoreItems(false);
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mIsLoading = false
         mIsLastPage = false
         mCurrentPage = 0
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
            val mcall: Call<GenresDataResponseModel?>? = uploadAPIs.getGenresData(
                "Bearer " + sessionManager?.getToken(),

                mCurrentPage.toString()
            )
            mcall?.enqueue(object : Callback<GenresDataResponseModel?> {
                override fun onResponse(
                    call: Call<GenresDataResponseModel?>,
                    response: retrofit2.Response<GenresDataResponseModel?>,
                ) {
                    try {
                        if (response.body() != null) {
                            val result = response.body()!!
                            //Log.v("responseannouncement-->", result)
                            /*  val mjonsresponse = JSONObject(result)
                              val data = mjonsresponse.getJSONObject("data")
                              val dataArray = data.getJSONArray("data")*/

                            result.apply {
                                if(data!!.data!!.isNotEmpty()) {
                                   // activityAlbumdetailsBinding.mainLl.visibility = View.VISIBLE
                                    if (isFirstPage) genreAdapter?.updateList(data?.data!!) else genreAdapter?.addToList(
                                        data?.data!!
                                    )
                                    mIsLoading = false
                                    mIsLastPage = mCurrentPage == result.data?.lastPage
                                }
                                else
                                {
                                   // activityAlbumdetailsBinding.mainLl.visibility = View.GONE
                                    Toast.makeText(requireContext(), "no data found", Toast.LENGTH_SHORT).show()
                                }

                            }


                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<GenresDataResponseModel?>, t: Throwable) {
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
    private fun setupRecyclewrView() {
        val mLayoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(mainActivity, 2)
        fragmentGenresBinding.genresRecycler.layoutManager = mLayoutManager
        fragmentGenresBinding.genresRecycler.addOnScrollListener(recyclerOnScroll)
        fragmentGenresBinding.genresRecycler.itemAnimator = DefaultItemAnimator()
        fragmentGenresBinding.genresRecycler.setAdapter(genreAdapter)
    }
}