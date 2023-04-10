package com.onlinemusic.wemu.fragment


import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.onlinemusic.wemu.MainActivity
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.adapter.PlaylistAdapter
import com.onlinemusic.wemu.databinding.FragmentPlaylistDetailsBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.onlinemusic.wemu.responseModel.FavouritesResponseModel
import com.onlinemusic.wemu.responseModel.playlist.responce.DataX
import com.onlinemusic.wemu.responseModel.playlist_details_response.PlayListDetailsResponse
import com.onlinemusic.wemu.responseModel.remove_from_playlist.RemoveFromPlayListRequestModel
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.viewmodel.PlayerDataModel1
import com.onlinemusic.wemu.viewmodel.PlayerViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit

class PlaylistDetailsFragment : Fragment(), PlaylistAdapter.DeleteItem {

    lateinit var fragmentPlaylistBinding: FragmentPlaylistDetailsBinding
    private var mBottomSheetBehavior1: BottomSheetBehavior<*>? = null
    lateinit var mainActivity: MainActivity
    var sessionManager: SessionManager? = null
    var playlistAdapter: PlaylistAdapter? = null

    private lateinit var playerViewModel: PlayerViewModel
    var permissionsArr: ArrayList<String> = ArrayList()

    var requestCodes = 100
    private val CAMERA_REQUEST = 1888

    var camera = false
    var gallery = false
    var isKitKat = false
    var mimeTypes = arrayOf(
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",  // .doc & .docx
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",  // .ppt & .pptx
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",  // .xls & .xlsx
        "text/plain",
        "application/pdf",
        "application/zip",
        "image/*"
    )
    var file_body: RequestBody? = null
    private val PICK_IMAGE_REQUEST = 1
    var filePath: String? = null
    var playlistId: Int? = 0
    var playlistName: String? = ""
    var playlistImageUrl: String? = ""
    var progress: ProgressDialog? = null
    var bitmap: Bitmap? = null
    lateinit var file_multi: MultipartBody.Part

    var mIsLoading = false
    var mIsLastPage = false
    var mCurrentPage = 0


    final var pageSize = 10

    var arrayList: ArrayList<DataX> = ArrayList()
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

        fragmentPlaylistBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_playlist_details, container, false)
        val root = fragmentPlaylistBinding.root
        mainActivity = activity as MainActivity
        sessionManager = SessionManager(mainActivity)

        playerViewModel = ViewModelProvider(activity as MainActivity)[PlayerViewModel::class.java]
        /* permissionsArr.add(Manifest.permission.READ_EXTERNAL_STORAGE)
         permissionsArr.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
         permissionsArr.add(Manifest.permission.CAMERA)*/
        playlistId = arguments?.getInt("playlist_id")
        playlistName = arguments?.getString("playlist_name")
        playlistImageUrl = arguments?.getString("playlist_image_url")

        fragmentPlaylistBinding.btnNotification.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_notification)
        }
        Glide.with(mainActivity)
            .load(playlistImageUrl)
            .timeout(6000)
            .error(R.drawable.logo)
            .placeholder(R.drawable.logo)
            .into(fragmentPlaylistBinding.imageBack)
        fragmentPlaylistBinding.tvTitle.text=playlistName
        if (sessionManager?.getTheme().equals("night")) {

            // fragmentPlaylistBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.black))
          //  fragmentPlaylistBinding.tvTitle.setTextColor(getResources().getColor(R.color.white))

            fragmentPlaylistBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            /*fragmentPlaylistBinding.btnBack.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )*/


        } else {

            //  fragmentPlaylistBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.white))
          //  fragmentPlaylistBinding.tvTitle.setTextColor(getResources().getColor(R.color.black))

            fragmentPlaylistBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            /*fragmentPlaylistBinding.btnBack.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )*/


        }

        mainActivity.mBottomNavigationView.visibility = View.GONE


        fragmentPlaylistBinding.btnBack.setOnClickListener {
            mainActivity.onBackPressed()
        }
        mIsLoading = false;
        mIsLastPage = false;
        setupRecyclewrView()
        // getAlbumsData()

        getPlayListSong(playlistId.toString())

        playerViewModel.getCurrentSongId.observe(mainActivity, Observer {
            playlistAdapter!!.notifyItemRangeChanged(0, playlistAdapter!!.itemCount)
        })

        return root

    }

    private fun setupRecyclewrView() {
        playlistAdapter = PlaylistAdapter(context = mainActivity, this)
        val mLayoutManager = GridLayoutManager(mainActivity, 1)

        fragmentPlaylistBinding.recPlaylist.layoutManager = mLayoutManager
        // fragmentPlaylistBinding.recPlaylist.addOnScrollListener(recyclerOnScroll)
        fragmentPlaylistBinding.recPlaylist.itemAnimator = DefaultItemAnimator()
        fragmentPlaylistBinding.recPlaylist.adapter = playlistAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mIsLoading = false
        mIsLastPage = false
        mCurrentPage = 0
    }


    var mProgressDialog: ProgressDialog? = null
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

    override fun deleteSelectedItem(songId: String) {
        removeFromList(songId)
    }

    override fun onClickSelectedItem(modelList: List<CommonDataModel1>, position: Int) {


        playerViewModel.startSongModel1.value = PlayerDataModel1(1, position, modelList)
    }

    private fun removeFromList(requestId: String) {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<FavouritesResponseModel?>? = uploadAPIs.removeFromPlaylist(
                "Bearer " + sessionManager?.getToken(),
                RemoveFromPlayListRequestModel(requestId.toInt())
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
                                        "Removed from playlist",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    getPlayListSong(playlistId.toString())
                                    //loadMoreItems(true)

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

    private fun getPlayListSong(playlistId: String) {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<PlayListDetailsResponse?>? = uploadAPIs.getPlayListDetails(
                "Bearer " + sessionManager?.getToken(),
                playlistId
            )
            mcall?.enqueue(object : Callback<PlayListDetailsResponse?> {
                @SuppressLint("SuspiciousIndentation")
                override fun onResponse(
                    call: Call<PlayListDetailsResponse?>,
                    response: retrofit2.Response<PlayListDetailsResponse?>,
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
                                    /*Toast.makeText(
                                        mainActivity,
                                        "Removed from playlist",
                                        Toast.LENGTH_SHORT
                                    ).show()*/

                                    val data = result.data
                                    if (data == null || data.data.isNullOrEmpty()){

                                        fragmentPlaylistBinding.noData.root.visibility=View.VISIBLE
                                        fragmentPlaylistBinding.playAllLl.visibility=View.GONE
                                            hideProgressDialog()
                                            return@apply
                                    }else {
                                        val commonDataModel1 = ArrayList<CommonDataModel1>()
                                        data!!.data!!.forEach { item ->
                                            commonDataModel1.add(item!!)
                                        }
                                        fragmentPlaylistBinding.playAllLl.visibility=View.VISIBLE
                                        playlistAdapter?.updateList(commonDataModel1)

                                        fragmentPlaylistBinding.playAllLl.setOnClickListener {
                                            playerViewModel.startSongModel1.value =
                                                PlayerDataModel1(
                                                    1,
                                                    0,
                                                    commonDataModel1 as List<CommonDataModel1>
                                                )

                                            playlistAdapter!!.notifyItemRangeChanged(0,commonDataModel1.size)

                                        }
                                    }
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
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<PlayListDetailsResponse?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(mainActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

}