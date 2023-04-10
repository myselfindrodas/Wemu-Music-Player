package com.onlinemusic.wemu.fragment


import android.Manifest
import android.app.ProgressDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.onlinemusic.wemu.MainActivity
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.adapter.PlayListCreatedAdapter
import com.onlinemusic.wemu.databinding.FragmentPlaylistBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.FavouritesResponseModel
import com.onlinemusic.wemu.responseModel.playlist.responce.DataX
import com.onlinemusic.wemu.responseModel.playlistitems.response.PlaylistCreatedResponseModel
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URLConnection
import java.util.*
import java.util.regex.Pattern

class PlaylistFragment : Fragment(),
    PlayListCreatedAdapter.OnPlaylistItemClickListener {

    lateinit var fragmentPlaylistBinding: FragmentPlaylistBinding
    private var mBottomSheetBehavior1: BottomSheetBehavior<*>? = null
    lateinit var mainActivity: MainActivity
    var sessionManager: SessionManager? = null

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
    var progress: ProgressDialog? = null
    var bitmap: Bitmap? = null
    lateinit var file_multi: MultipartBody.Part

    var mIsLoading = false
    var mIsLastPage = false
    var mCurrentPage = 0


    final var pageSize = 10

    var arrayList: ArrayList<DataX> = ArrayList()
    lateinit var playlistAdapter: PlayListCreatedAdapter
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
            DataBindingUtil.inflate(inflater, R.layout.fragment_playlist, container, false)
        val root = fragmentPlaylistBinding.root
        mainActivity = activity as MainActivity
        sessionManager = SessionManager(mainActivity)

        permissionsArr.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        permissionsArr.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissionsArr.add(Manifest.permission.CAMERA)
        fragmentPlaylistBinding.btnNotification.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_notification)
        }
        if (sessionManager?.getTheme().equals("night")) {

            // fragmentPlaylistBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.black))
            fragmentPlaylistBinding.llPlus.setBackgroundColor(getResources().getColor(R.color.greyblack))
            fragmentPlaylistBinding.tvTitle.setTextColor(getResources().getColor(R.color.white))

            fragmentPlaylistBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentPlaylistBinding.imgback.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

            fragmentPlaylistBinding.imgplus.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        } else {

            //  fragmentPlaylistBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.white))
            fragmentPlaylistBinding.llPlus.setBackgroundColor(getResources().getColor(R.color.greywhite))
            fragmentPlaylistBinding.tvTitle.setTextColor(getResources().getColor(R.color.black))

            fragmentPlaylistBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )
            fragmentPlaylistBinding.imgback.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )


            fragmentPlaylistBinding.imgplus.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        }

        mainActivity.mBottomNavigationView?.visibility = View.GONE

        fragmentPlaylistBinding.btn.btnSubmit.setOnClickListener {
            //Toast.makeText(requireContext(), "success", Toast.LENGTH_SHORT).show()
            if (fragmentPlaylistBinding.btn.imgName.text.equals("Add Image")) {
                Toast.makeText(requireContext(), "Image cannot be blank", Toast.LENGTH_SHORT).show()

            } else if (fragmentPlaylistBinding.btn.playlistTitle.text.equals("")) {
                Toast.makeText(
                    requireContext(),
                    "Playlist title cannot be blank",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                submitData()
            }
        }
        fragmentPlaylistBinding.btn.dismissDialog.setOnClickListener {
            fragmentPlaylistBinding.btn.bottomSheet.visibility = View.GONE
            fragmentPlaylistBinding.btn.imgName.text = "Add Image"
            fragmentPlaylistBinding.btn.playlistTitle.setText("")
        }


        fragmentPlaylistBinding.btn.chooseImg.setOnClickListener {
            // Toast.makeText(requireContext(), "success", Toast.LENGTH_SHORT).show()
            if (
                ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {

                val layoutInflater =
                    mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val layout: View = layoutInflater.inflate(R.layout.camera_layout, null, false)
                val dialog = BottomSheetDialog(mainActivity)
                (dialog).behavior.peekHeight = 200
                dialog.setContentView(layout)
                val takephoto = dialog.findViewById<View>(R.id.camera_ll) as Button?
                val choosegallery = dialog.findViewById<View>(R.id.gallery_ll) as Button?
                takephoto!!.setOnClickListener {
                    camerapic()
                    camera = true
                    gallery = false
                    dialog.dismiss()
                }

                choosegallery!!.setOnClickListener {
                    opengallery()
                    gallery = true
                    camera = false
                    dialog.dismiss()
                }
                dialog.show()
            } else {
                Log.d("jhvjhvjv", "jkhhihb")
                val PERMISSIONS: Array<String> = arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,

                    )
                ActivityCompat.requestPermissions(
                    mainActivity,
                    PERMISSIONS,
                    requestCodes
                );

            }
        }

        fragmentPlaylistBinding.btnAddplaylist.setOnClickListener {
            fragmentPlaylistBinding.btn.bottomSheet.visibility = View.VISIBLE
            /* if (mBottomSheetBehavior1?.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                 fragmentPlaylistBinding.btn.bottomSheet.visibility = View.VISIBLE
                 mBottomSheetBehavior1?.setState(BottomSheetBehavior.STATE_EXPANDED)
                 mBottomSheetBehavior1?.setDraggable(true)
             } else {
                 mBottomSheetBehavior1?.setState(BottomSheetBehavior.STATE_COLLAPSED)
                 fragmentPlaylistBinding.btn.bottomSheet.visibility = View.GONE
             }*/
        }

        fragmentPlaylistBinding.btnBack.setOnClickListener {
            mainActivity.onBackPressed()
        }
        mIsLoading = false;
        mIsLastPage = false;
        playlistAdapter = PlayListCreatedAdapter(true,mainActivity,this)
        setupRecyclewrView()
        // getAlbumsData()
        loadMoreItems(true)

        return root

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
            val mcall: Call<PlaylistCreatedResponseModel?>? = uploadAPIs.getCreatedPlaylistData(
                "Bearer " + sessionManager?.getToken(),

                mCurrentPage.toString()
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

                                    if (  isFirstPage && (data ==null || data.data!!.isNullOrEmpty())){
                                        fragmentPlaylistBinding.noData.tvNoSongs.text="Create playlist"
                                        fragmentPlaylistBinding.noData.root.visibility=View.VISIBLE
                                        hideProgressDialog()
                                        return@apply
                                    }
                                    if (data!!.data!!.isNotEmpty()) {

                                        fragmentPlaylistBinding.noData.root.visibility=View.GONE
                                        // activityAlbumdetailsBinding.mainLl.visibility = View.VISIBLE
                                        if (isFirstPage) playlistAdapter?.updateList(data?.data!!) else playlistAdapter?.addToList(
                                            data?.data!!
                                        )
                                        mIsLoading = false
                                        mIsLastPage = mCurrentPage == result.data?.lastPage
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
                                        Toast.makeText(
                                            requireContext(),
                                            "no data found",
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

    private fun setupRecyclewrView() {
        var mLayoutManager = GridLayoutManager(mainActivity, 2)

        fragmentPlaylistBinding.recPlaylist.layoutManager = mLayoutManager
        fragmentPlaylistBinding.recPlaylist.addOnScrollListener(recyclerOnScroll)
        fragmentPlaylistBinding.recPlaylist.itemAnimator = DefaultItemAnimator()
        fragmentPlaylistBinding.recPlaylist.setAdapter(playlistAdapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mIsLoading = false
        mIsLastPage = false
        mCurrentPage = 0
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

    private fun submitData() {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)

            var types = fragmentPlaylistBinding.btn.playlistTitle.text.toString()
            val type: RequestBody = types.toRequestBody("text/plain".toMediaTypeOrNull())
            val mcall: Call<FavouritesResponseModel?>? =
                uploadAPIs.createPlaylist("Bearer " + sessionManager?.getToken(), type, file_multi)
            mcall?.enqueue(object : Callback<FavouritesResponseModel?> {
                override fun onResponse(
                    call: Call<FavouritesResponseModel?>,
                    response: Response<FavouritesResponseModel?>,
                ) {
                    Log.d("hdfhff", response.body().toString())
                    try {
                        if (response.body() != null) {

                            fragmentPlaylistBinding.btn.bottomSheet.visibility = View.GONE
                            mCurrentPage = 0
                            fragmentPlaylistBinding.btn.imgName.text = "Add Image"
                            fragmentPlaylistBinding.btn.playlistTitle.setText("")
                            Toast.makeText(
                                mainActivity,
                                "Playlist created successfully",
                                Toast.LENGTH_LONG
                            ).show()
                            loadMoreItems(true)


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

    private fun opengallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.type = if (mimeTypes.size == 1) mimeTypes[0] else "*/*"
                if (mimeTypes.size > 0) {
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                }
            } else {
                var mimeTypesStr = ""
                for (mimeType in mimeTypes) {
                    mimeTypesStr += "$mimeType|"
                }
                intent.type = mimeTypesStr.substring(0, mimeTypesStr.length - 1)
            }
            isKitKat = true
            startActivityForResult(
                Intent.createChooser(intent, "Select file"),
                PICK_IMAGE_REQUEST
            )
        } else {
            isKitKat = false
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.type = if (mimeTypes.size == 1) mimeTypes[0] else "*/*"
                if (mimeTypes.size > 0) {
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                }
            } else {
                var mimeTypesStr = ""
                for (mimeType in mimeTypes) {
                    mimeTypesStr += "$mimeType|"
                }
                intent.type = mimeTypesStr.substring(0, mimeTypesStr.length - 1)
            }
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_REQUEST
            )
        }
    }

    private fun camerapic() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(
            cameraIntent,
            CAMERA_REQUEST
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null) {
            var isImageFromGoogleDrive = false
            val uri = data.data
            if (isKitKat && DocumentsContract.isDocumentUri(mainActivity, uri)) {
                if ("com.android.externalstorage.documents" == uri!!.authority) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        filePath =
                            Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    } else {
                        val DIR_SEPORATOR = Pattern.compile("/")
                        val rv: MutableSet<String> = HashSet()
                        val rawExternalStorage = System.getenv("EXTERNAL_STORAGE")
                        val rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE")
                        val rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET")
                        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
                            if (TextUtils.isEmpty(rawExternalStorage)) {
                                rv.add("/storage/sdcard0")
                            } else {
                                rv.add(rawExternalStorage)
                            }
                        } else {
                            val rawUserId: String
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                rawUserId = ""
                            } else {
                                val path = Environment.getExternalStorageDirectory().absolutePath
                                val folders = DIR_SEPORATOR.split(path)
                                val lastFolder = folders[folders.size - 1]
                                var isDigit = false
                                try {
                                    Integer.valueOf(lastFolder)
                                    isDigit = true
                                } catch (ignored: NumberFormatException) {
                                }
                                rawUserId = if (isDigit) lastFolder else ""
                            }
                            if (TextUtils.isEmpty(rawUserId)) {
                                rv.add(rawEmulatedStorageTarget)
                            } else {
                                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId)
                            }
                        }
                        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
                            val rawSecondaryStorages =
                                rawSecondaryStoragesStr.split(File.pathSeparator.toRegex())
                                    .toTypedArray()
                            Collections.addAll(rv, *rawSecondaryStorages)
                        }
                        val temp = rv.toTypedArray()
                        for (i in temp.indices) {
                            val tempf = File(temp[i] + "/" + split[1])
                            if (tempf.exists()) {
                                filePath = temp[i] + "/" + split[1]
                            }
                        }
                    }
                } else if ("com.android.providers.downloads.documents" == uri!!.authority) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    var cursor: Cursor? = null
                    val column = "_data"
                    val projection = arrayOf(column)
                    try {
                        cursor = mainActivity.contentResolver.query(
                            contentUri, projection, null, null,
                            null
                        )
                        if (cursor != null && cursor.moveToFirst()) {
                            val column_index = cursor.getColumnIndexOrThrow(column)
                            filePath = cursor.getString(column_index)
                        }
                    } finally {
                        cursor?.close()
                    }
                } else if ("com.android.providers.media.documents" == uri!!.authority) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])
                    var cursor: Cursor? = null
                    val column = "_data"
                    val projection = arrayOf(column)
                    try {
                        cursor = mainActivity.contentResolver.query(
                            contentUri!!,
                            projection,
                            selection,
                            selectionArgs,
                            null
                        )
                        if (cursor != null && cursor.moveToFirst()) {
                            val column_index = cursor.getColumnIndexOrThrow(column)
                            filePath = cursor.getString(column_index)
                        }
                    } finally {
                        cursor?.close()
                    }
                } else if ("com.google.android.apps.docs.storage" == uri!!.authority) {
                    isImageFromGoogleDrive = true
                }
            } else if ("content".equals(uri!!.scheme, ignoreCase = true)) {
                var cursor: Cursor? = null
                val column = "_data"
                val projection = arrayOf(column)
                try {
                    cursor = mainActivity.contentResolver.query(
                        uri!!,
                        projection,
                        null,
                        null,
                        null
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        val column_index = cursor.getColumnIndexOrThrow(column)
                        filePath = cursor.getString(column_index)
                    }
                } finally {
                    cursor?.close()
                }
            } else if ("file".equals(uri!!.scheme, ignoreCase = true)) {
                filePath = uri!!.path
            }
            try {
                println("Real Path 1$filePath")

                bitmap = MediaStore.Images.Media.getBitmap(mainActivity.contentResolver, uri)
                println("bitmap image==$bitmap")
                val file_name = filePath!!.substring(filePath!!.lastIndexOf("/") + 1)
                val filenameArray = file_name.split("\\.".toRegex()).toTypedArray()
                val extension = filenameArray[filenameArray.size - 1]
                println("extension$extension")
                val f = File(filePath)
                val mimeType = URLConnection.guessContentTypeFromName(f.name)
                file_body = f!!.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                fragmentPlaylistBinding.btn.imgName.setText(file_name);
                /* var file_multi= MultipartBody.Part.createFormData(
                     "image",
                     f!!.name,
                     f!!.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                 )*/
                val fileReqBody = RequestBody.create("image/jpg".toMediaTypeOrNull(), f!!)
                file_multi = MultipartBody.Part.createFormData("image", f.name, fileReqBody)


                //submitData(part)
                println("file_bodypathasd$file_body")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (requestCode == CAMERA_REQUEST) {
            /** camera intent blank or not  */
            if (data == null) {
            } else {
                val bitmap = data.extras!!["data"] as Bitmap?
                if (bitmap != null) {

                    val tempUri: Uri = getImageUri(mainActivity, bitmap)!!
                    filePath = getRealPathFromURI(tempUri)
                    println("pathasd$filePath")

                    val f = File(filePath)
                    val mimeType = URLConnection.guessContentTypeFromName(f.name)
                    file_body = f!!.asRequestBody("multipart/form-data".toMediaTypeOrNull())

                    fragmentPlaylistBinding.btn.imgName.setText(f.name);
                    val fileReqBody = RequestBody.create("image/jpg".toMediaTypeOrNull(), f!!)
                    file_multi = MultipartBody.Part.createFormData("image", f.name, fileReqBody)


                    var file_multi = MultipartBody.Part.createFormData(
                        "image",
                        f!!.name,
                        f!!.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    )

                    println("file_bodypathasd$file_body")
                    //submitData(part)

                }
            }
        }

    }


    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        /*ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);*/
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.contentResolver,
            inImage,
            "IMG_" + Calendar.getInstance().time,
            null
        )
        return Uri.parse(path)
    }

    fun getRealPathFromURI(uri: Uri?): String? {
        val cursor = mainActivity.contentResolver.query(uri!!, null, null, null, null)
        cursor!!.moveToFirst()
        val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        return cursor.getString(idx)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodes) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mainActivity, "all permissions granted", Toast.LENGTH_LONG).show()

                val layoutInflater =
                    mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val layout: View = layoutInflater.inflate(R.layout.camera_layout, null, false)
                val dialog = BottomSheetDialog(mainActivity)
                (dialog).behavior.peekHeight = 200
                dialog.setContentView(layout)
                val takephoto = dialog.findViewById<View>(R.id.camera_ll) as Button?
                val choosegallery = dialog.findViewById<View>(R.id.gallery_ll) as Button?
                takephoto!!.setOnClickListener {
                    camerapic()
                    camera = true
                    gallery = false
                    dialog.dismiss()
                }

                choosegallery!!.setOnClickListener {
                    opengallery()
                    gallery = true
                    camera = false
                    dialog.dismiss()
                }
                dialog.show()
            } else {
                Toast.makeText(
                    mainActivity,
                    "Please accept permissions before proceeding",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
/*
    private fun removeFromList(songId: String) {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            //showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<FavouritesResponseModel?>? = uploadAPIs.addtoPlaylist(
                "Bearer " + sessionManager?.getToken(),
                songId
            )
            mcall?.enqueue(object : Callback<FavouritesResponseModel?> {
                override fun onResponse(
                    call: Call<FavouritesResponseModel?>,
                    response: retrofit2.Response<FavouritesResponseModel?>,
                ) {
                    try {
                        if (response.body() != null) {

                            val result = response.body()!!
                            *//* Log.v("responseannouncement-->", result)
                             val mjonsresponse = JSONObject(result)
                             val data = mjonsresponse.getJSONObject("data")*//*
                            result.apply {

                                var status = result.status
                                if (status == true) {
                                    Toast.makeText(
                                        mainActivity,
                                        "Removed from playlist",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    mIsLoading = false
                                    mIsLastPage = false
                                    mCurrentPage = 0
                                    loadMoreItems(true)

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
    */
    override fun onClick(
    modelData: List<com.onlinemusic.wemu.responseModel.playlistitems.response.DataX>,
    position: Int
    ) {


    }

}