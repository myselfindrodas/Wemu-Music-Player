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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onlinemusic.wemu.MainActivity
import com.onlinemusic.wemu.R

import com.onlinemusic.wemu.adapter.NotificationAdapter
import com.onlinemusic.wemu.databinding.FragmentNotificationBinding
import com.onlinemusic.wemu.internet.CheckConnectivity

import com.onlinemusic.wemu.responseModel.notification.response.DataX
import com.onlinemusic.wemu.responseModel.notification.response.NotificationResponseModel
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface

import com.onlinemusic.wemu.session.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit


class NotificationFragment : Fragment() {
    lateinit var fragmentNotificationBinding: FragmentNotificationBinding
    var sessionManager: SessionManager? = null
    lateinit var mainActivity: MainActivity
    lateinit var notificationAdapter: NotificationAdapter
    var mLayoutManager: RecyclerView.LayoutManager?=null
    // initialise loading state
    var arrayList : ArrayList<DataX> = ArrayList()

    var mIsLoading = false;
    var mIsLastPage = false;
    var mCurrentPage = 0;
    var mProgressDialog: ProgressDialog? = null

    // amount of items you want to load per page
    final var pageSize = 10;
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentNotificationBinding =
            DataBindingUtil.inflate(inflater,R.layout.fragment_notification,container,false)
        val root = fragmentNotificationBinding.root
        mainActivity=activity as MainActivity
        mainActivity.mBottomNavigationView?.visibility = View.GONE
        sessionManager = SessionManager(mainActivity)
        if (sessionManager?.getTheme().equals("night")) {

            // fragmentAlbumBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.black))
            fragmentNotificationBinding.tvTitle.setTextColor(getResources().getColor(R.color.white))
            fragmentNotificationBinding.tvSubtitle.setTextColor(getResources().getColor(R.color.white))

            fragmentNotificationBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentNotificationBinding.imgback.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        }
        else
        {


            //  fragmentAlbumBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.white))
            fragmentNotificationBinding.tvTitle.setTextColor(getResources().getColor(R.color.black))
            fragmentNotificationBinding.tvSubtitle.setTextColor(getResources().getColor(R.color.black))

            fragmentNotificationBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentNotificationBinding.imgback.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        }
        fragmentNotificationBinding.btnBack.setOnClickListener {

            mainActivity.onBackPressed()
        }
        mIsLoading = false;
        mIsLastPage = false;
        notificationAdapter = NotificationAdapter(requireContext())

        setupRecyclewrView()
        // getAlbumsData()
        loadMoreItems(true)

        mLayoutManager = GridLayoutManager(requireContext(), 1)


        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        mIsLoading = false
        mIsLastPage = false
        mCurrentPage = 0
    }


    fun setupRecyclewrView(){
        mLayoutManager = GridLayoutManager(requireContext(), 1)
        fragmentNotificationBinding.recNotification.layoutManager = mLayoutManager
        fragmentNotificationBinding.recNotification.addOnScrollListener(recyclerOnScroll)
        fragmentNotificationBinding.recNotification.itemAnimator = DefaultItemAnimator()
        fragmentNotificationBinding.recNotification.adapter = notificationAdapter
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
            val mcall: Call<NotificationResponseModel?>? = uploadAPIs.getNotificationData("Bearer "+sessionManager?.getToken(),mCurrentPage.toString())
            mcall?.enqueue(object : Callback<NotificationResponseModel?> {
                override fun onResponse(
                    call: Call<NotificationResponseModel?>,
                    response: retrofit2.Response<NotificationResponseModel?>,
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

                                if (isFirstPage) notificationAdapter.updateList(data?.data!!) else notificationAdapter.addToList(data?.data!!)
                                mIsLoading = false
                                mIsLastPage = mCurrentPage == result.data?.lastPage




                            }





                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<NotificationResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(requireContext(), "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }


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

}