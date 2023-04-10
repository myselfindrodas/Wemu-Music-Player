package com.onlinemusic.wemu.fragment

import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onlinemusic.wemu.MainActivity
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.adapter.ArtistAdapter
import com.onlinemusic.wemu.databinding.FragmentArtistBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.artist.response.ArtistMainResponseModel
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.utils.Utilities
import com.onlinemusic.wemu.viewmodel.PlayerViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit


class ArtistFragment : Fragment() {
    lateinit var fragmentArtistBinding: FragmentArtistBinding
    lateinit var mainActivity: MainActivity

    var sessionManager: SessionManager? = null
    lateinit var dialog: BottomSheetDialog

    lateinit var artistAdapter: ArtistAdapter
    var mProgressDialog: ProgressDialog? = null
    var mLayoutManager: RecyclerView.LayoutManager? = null
    private lateinit var playerViewModel: PlayerViewModel

    // initialise loading state
    var mIsLoading = false
    var mIsLastPage = false
    var mCurrentPage = 0


    // amount of items you want to load per page
    final var pageSize = 10

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentArtistBinding =
            DataBindingUtil.inflate(inflater,R.layout.fragment_artist,container,false);
        val root = fragmentArtistBinding.root
        mainActivity=activity as MainActivity
        playerViewModel = ViewModelProvider(activity as MainActivity)[PlayerViewModel::class.java]
        mainActivity.mBottomNavigationView?.visibility = View.GONE
        sessionManager = SessionManager(mainActivity)
        dialog = BottomSheetDialog(mainActivity)
        //playedAdapter=TopmusicplayedAdapter(mainActivity)
        fragmentArtistBinding.btnBack.setOnClickListener {
            mainActivity.onBackPressed()
        }
        fragmentArtistBinding.btnNotification.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_notification)
        }
        if (sessionManager?.getTheme().equals("night")) {
            //fragmentFavouriteBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.black))
            fragmentArtistBinding.tvTitle.setTextColor(getResources().getColor(R.color.white))
            fragmentArtistBinding.tvSubtitle.setTextColor(getResources().getColor(R.color.white))
            fragmentArtistBinding.artistSize.setTextColor(getResources().getColor(R.color.white))

            fragmentArtistBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentArtistBinding.imgback.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        } else {

            //fragmentFavouriteBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.white))
            fragmentArtistBinding.tvTitle.setTextColor(getResources().getColor(R.color.black))
            fragmentArtistBinding.tvSubtitle.setTextColor(getResources().getColor(R.color.black))
            fragmentArtistBinding.artistSize.setTextColor(getResources().getColor(R.color.black))

            fragmentArtistBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentArtistBinding.imgback.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        }
        mIsLoading = false;
        mIsLastPage = false;
        artistAdapter = ArtistAdapter(mainActivity)
        setupRecyclewrView()

        loadMoreItems(true)
        return root
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

    private fun loadMoreItems(isFirstPage: Boolean) {
        // change loading state
        mIsLoading = true
        mCurrentPage += 1

        // update recycler adapter with next page
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<ArtistMainResponseModel?>? = uploadAPIs.getArtistData(
                "Bearer " + sessionManager?.getToken(),
                mCurrentPage.toString()
            )
            mcall?.enqueue(object : Callback<ArtistMainResponseModel?> {
                override fun onResponse(
                    call: Call<ArtistMainResponseModel?>,
                    response: retrofit2.Response<ArtistMainResponseModel?>,
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
                                    //fragmentArtistBinding.artistSize.text= data?.data!!.size.toString() + " artists"
                                    if (isFirstPage) artistAdapter?.updateList(data?.data!!) else artistAdapter?.addToList(
                                        data?.data!!
                                    )
                                    mIsLoading = false
                                    mIsLastPage = mCurrentPage == result.data?.lastPage
                                }
                                else
                                {

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


                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<ArtistMainResponseModel?>, t: Throwable) {
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
    fun setupRecyclewrView() {
        mLayoutManager = GridLayoutManager(mainActivity, 2)
        fragmentArtistBinding.artistList.layoutManager = mLayoutManager
        fragmentArtistBinding.artistList.addOnScrollListener(recyclerOnScroll)
        fragmentArtistBinding.artistList.itemAnimator = DefaultItemAnimator()
        fragmentArtistBinding.artistList.adapter = artistAdapter


    }
    override fun onDestroyView() {
        super.onDestroyView()
        mIsLoading = false
        mIsLastPage = false
        mCurrentPage = 0
    }
}