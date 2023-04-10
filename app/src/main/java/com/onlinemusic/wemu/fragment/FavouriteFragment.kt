package com.onlinemusic.wemu.fragment

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.onlinemusic.wemu.MainActivity
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.adapter.FavouritesItemAdapter
import com.onlinemusic.wemu.databinding.FragmentFavouriteBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.CommonDataModel
import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.onlinemusic.wemu.responseModel.FavouritesResponseModel
import com.onlinemusic.wemu.responseModel.favourites.response.FavouritesAddResponseModel
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.viewmodel.PlayerDataModel1
import com.onlinemusic.wemu.viewmodel.PlayerViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit

class FavouriteFragment : Fragment(), FavouritesItemAdapter.DeleteItem {

    lateinit var fragmentFavouriteBinding: FragmentFavouriteBinding
    var keyItem = ""
    var key_fetch = ""

    var arrayList: ArrayList<CommonDataModel> = ArrayList()
    lateinit var dialog: BottomSheetDialog
    lateinit var mainActivity: MainActivity

    var sessionManager: SessionManager? = null
    var mProgressDialog: ProgressDialog? = null

    lateinit var favouritesItemAdapter: FavouritesItemAdapter

    var mLayoutManager: RecyclerView.LayoutManager? = null

    // initialise loading state
    var mIsLoading = false;
    var mIsLastPage = false;
    var mCurrentPage = 0;

    // amount of items you want to load per page
    final var pageSize = 10;

    private lateinit var playerViewModel: PlayerViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentFavouriteBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_favourite, container, false);
        val root = fragmentFavouriteBinding.root
        // playerViewModel = ViewModelProvider(activity as MainActivity)[PlayerViewModel::class.java]
        playerViewModel = ViewModelProvider(activity as MainActivity)[PlayerViewModel::class.java]

        mainActivity = activity as MainActivity
        sessionManager = SessionManager(mainActivity)
        dialog = BottomSheetDialog(mainActivity)
        fragmentFavouriteBinding.btnNotification.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_notification)
        }
        if (sessionManager?.getTheme().equals("night")) {
            //fragmentFavouriteBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.black))
            fragmentFavouriteBinding.tvTitle.setTextColor(getResources().getColor(R.color.white))
            fragmentFavouriteBinding.tvSubtitle.setTextColor(getResources().getColor(R.color.white))

            fragmentFavouriteBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentFavouriteBinding.imgback.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        } else {

            //fragmentFavouriteBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.white))
            fragmentFavouriteBinding.tvTitle.setTextColor(getResources().getColor(R.color.black))
            fragmentFavouriteBinding.tvSubtitle.setTextColor(getResources().getColor(R.color.black))

            fragmentFavouriteBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentFavouriteBinding.imgback.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        }
        fragmentFavouriteBinding.btnBack.setOnClickListener {

            mainActivity.onBackPressed()
        }

        mIsLoading = false;
        mIsLastPage = false;
        favouritesItemAdapter = FavouritesItemAdapter(requireContext(),this)

        setupRecyclewrView()
        // getAlbumsData()
        loadMoreItems(true)


        playerViewModel.getCurrentSongId.observe(mainActivity, Observer {
            favouritesItemAdapter.notifyItemRangeChanged(0,favouritesItemAdapter.itemCount)
        })
        return root

    }



    override fun onDestroyView() {
        super.onDestroyView()
        mIsLoading = false
        mIsLastPage = false
        mCurrentPage = 0
    }


    fun setupRecyclewrView() {
        mLayoutManager = GridLayoutManager(requireContext(), 1)
        fragmentFavouriteBinding.recFavourate.layoutManager = mLayoutManager
        fragmentFavouriteBinding.recFavourate.addOnScrollListener(recyclerOnScroll)
        fragmentFavouriteBinding.recFavourate.itemAnimator = DefaultItemAnimator()
        fragmentFavouriteBinding.recFavourate.adapter = favouritesItemAdapter
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
        if (CheckConnectivity.getInstance(requireContext()).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<FavouritesAddResponseModel?>? = uploadAPIs.getfavouritesData(
                "Bearer " + sessionManager?.getToken(),
                mCurrentPage.toString()
            )
            mcall?.enqueue(object : Callback<FavouritesAddResponseModel?> {
                override fun onResponse(
                    call: Call<FavouritesAddResponseModel?>,
                    response: retrofit2.Response<FavouritesAddResponseModel?>,
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

                                if (isFirstPage) favouritesItemAdapter.updateList(data?.data!!) else favouritesItemAdapter.addToList(
                                    data?.data!!
                                )
                                mIsLoading = false
                                mIsLastPage = mCurrentPage == result.data?.lastPage
                                if (data.data.isNotEmpty()) {
                                    fragmentFavouriteBinding.tvSubtitle.text =
                                        data.data.size.toString() + " + songs"

                                } else {
                                    fragmentFavouriteBinding.tvSubtitle.text = ""

                                }


                            }


                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<FavouritesAddResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(requireContext(), "OOPS! No Internet Connection", Toast.LENGTH_SHORT)
                .show()
        }


    }


    fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(mainActivity)
            mProgressDialog!!.setMessage("Loading...")
            mProgressDialog!!.isIndeterminate = true
        }
        if (!mainActivity.isFinishing) {
            mProgressDialog!!.show()
        }
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }

    override fun deleteSelectedItem(songId: String) {
        removeFromList(songId)
    }

    private fun removeFromList(requestId: String) {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<FavouritesResponseModel?>? = uploadAPIs.addtoLikeSongs(
                "Bearer " + sessionManager?.getToken(),
                requestId
            )
            mcall?.enqueue(object : Callback<FavouritesResponseModel?> {
                override fun onResponse(
                    call: Call<FavouritesResponseModel?>,
                    response: retrofit2.Response<FavouritesResponseModel?>,
                ) {
                    try {
                        if (response.body() != null) {

                            val result = response.body()!!
                            // Log.v("responseannouncement-->", result)
                            /* val mjonsresponse = JSONObject(result)
                             val data = mjonsresponse.getJSONObject("data")*/
                            result.apply {

                                var status = result.status
                                if (status == true) {
                                    Toast.makeText(
                                        mainActivity,
                                        "Removed from favourites",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    mIsLoading = false
                                    mIsLastPage = false
                                    mCurrentPage = 0
                                    loadMoreItems(true)
                                    //loadMoreItems(true)

                                } else {
                                    Toast.makeText(
                                        mainActivity,
                                        "Error Removing from favourites",
                                        Toast.LENGTH_SHORT
                                    ).show()
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
            Toast.makeText(mainActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onClickSelectedItem(modelList: List<CommonDataModel1>, position: Int) {

        playerViewModel.startSongModel1.value = PlayerDataModel1(1, position, modelList)
    }


}