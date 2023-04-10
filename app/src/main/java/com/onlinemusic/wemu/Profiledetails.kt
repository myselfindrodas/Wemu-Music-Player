package com.onlinemusic.wemu

import android.Manifest
import android.app.ProgressDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.net.Uri.parse
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore


import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.onlinemusic.wemu.databinding.ActivityProfiledetailsBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URLConnection
import java.util.*
import java.util.regex.Pattern


class Profiledetails : AppCompatActivity() {


    lateinit var activityProfiledetailsBinding: ActivityProfiledetailsBinding
    var sessionManager: SessionManager? = null
    var permissionsArr:ArrayList<String> = ArrayList()

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityProfiledetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_profiledetails)
        sessionManager = SessionManager(applicationContext)
        permissionsArr.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        permissionsArr.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissionsArr.add(Manifest.permission.CAMERA)

        activityProfiledetailsBinding.btnBack.setOnClickListener {

            onBackPressed()
        }
        activityProfiledetailsBinding.profileEdt.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }

        activityProfiledetailsBinding.btnPayment.setOnClickListener {


        }
        activityProfiledetailsBinding.imgPicker.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
                val layoutInflater =getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val layout: View = layoutInflater.inflate(R.layout.camera_layout, null, false)
                val dialog = BottomSheetDialog(this)
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
            }
            else
            {
                 val PERMISSIONS: Array<String> = arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,

                )
                ActivityCompat.requestPermissions(this,
                    PERMISSIONS,
                    requestCodes);

            }
        }
        getProfiledetails()
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


    private fun getProfiledetails(){

        if (CheckConnectivity.getInstance(applicationContext).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<ResponseBody?>? = uploadAPIs.myprofile("Bearer "+sessionManager?.getToken())
            mcall?.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: retrofit2.Response<ResponseBody?>,
                )
                {
                    try {
                        if (response.body() != null) {
                            val result = response.body()!!.string()
                            Log.v("responseannouncement-->", result)
                            val mjonsresponse = JSONObject(result)
                            val status = mjonsresponse.getBoolean("status")
                            val message = mjonsresponse.getString("message")
                            val image_url = mjonsresponse.getString("image_url")
                            val data = mjonsresponse.getJSONObject("data")
                            Toast.makeText(this@Profiledetails, message, Toast.LENGTH_SHORT).show()
                            if (status) {

                                val username = data.getString("username")
                                val email = data.getString("email")
                                val name = data.getString("name")
                                val gender = data.getString("gender")
                                val language = data.getString("language")
                                val avatar = data.getString("avatar")
                                val cover = data.getString("cover")
                                val dob = data.getString("dob")

                                activityProfiledetailsBinding.tvProfName.text = username
                                activityProfiledetailsBinding.tvInfo.text = email
                                activityProfiledetailsBinding.tvGender.text = gender
                               /* Glide.with(this@Profiledetails)
                                    .load("https://developer.shyamfuture.in/wemuonline/public/upload/photos/" + avatar)
                                    .into(activityProfiledetailsBinding.imgPrf)*/
                                Glide.with(this@Profiledetails)
                                    .load(image_url +"/"+ avatar)
                                    .timeout(6000)
                                    .error(R.drawable.logo)
                                    .placeholder(R.drawable.logo)
                                    .into(activityProfiledetailsBinding.imgPrf)

                                Glide.with(this@Profiledetails)
                                    .load(image_url +"/"+ cover)
                                    .timeout(6000)
                                    .error(R.drawable.logo)
                                    .placeholder(R.drawable.logo)
                                    .into(activityProfiledetailsBinding.bgPrf)


                            }

                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(applicationContext, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }

//        if (CheckConnectivity.getInstance(applicationContext).isOnline) {
//            showProgressDialog()
//            val jsonRequest: JsonObjectRequest = object : JsonObjectRequest(
//                Method.GET, Allurl.GetProfiledetails, null,
//                Response.Listener { response: JSONObject ->
//                    Log.i("Response-->", response.toString())
//                    try {
//
//                        val result = JSONObject(response.toString())
//                        val status = result.getBoolean("status")
//                        val message = result.getString("message")
//                        val data = result.getJSONObject("data")
//                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//                        if (status) {
//
//                            val username = data.getString("username")
//                            val email = data.getString("email")
//                            val name = data.getString("name")
//                            val gender = data.getString("gender")
//                            val language = data.getString("language")
//                            val avatar = data.getString("avatar")
//                            val cover = data.getString("cover")
//                            val dob = data.getString("dob")
//
//                            activityProfiledetailsBinding.tvProfName.text = username
//                            activityProfiledetailsBinding.tvInfo.text = email
//                            activityProfiledetailsBinding.tvGender.text = gender
//                            Glide.with(this).load("https://developer.shyamfuture.in/wemuonline/public/" + avatar)
//                                .into(activityProfiledetailsBinding.imgPrf)
//
//                        }
//
//                    } catch (e: JSONException) {
//                        e.printStackTrace()
//                    }
//                    hideProgressDialog()
//                },
//                Response.ErrorListener { error ->
//                    Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show()
//                    hideProgressDialog()
//                }) {
//                @Throws(AuthFailureError::class)
//                override fun getHeaders(): Map<String, String> {
//                    val params: MutableMap<String, String> = HashMap()
//                    params["Authorization"] = "Bearer "+sessionManager?.getToken()
//                    return params
//                }
//            }
//            Volley.newRequestQueue(this).add(jsonRequest)
//        } else {
//            Toast.makeText(applicationContext, "Ooops! Internet Connection Error", Toast.LENGTH_SHORT).show()
//        }

    }


    var mProgressDialog: ProgressDialog? = null

    fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(this)
            mProgressDialog!!.setMessage("Loading...")
            mProgressDialog!!.isIndeterminate = true
        }
        if(!this.isFinishing)
        mProgressDialog!!.show()
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            var isImageFromGoogleDrive = false
            val uri = data.data
            if (isKitKat && DocumentsContract.isDocumentUri(applicationContext, uri)) {
                if ("com.android.externalstorage.documents" == uri!!.authority) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        filePath =
                            Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }
                    else
                    {
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
                }
                else if ("com.android.providers.downloads.documents" == uri!!.authority)
                {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    var cursor: Cursor? = null
                    val column = "_data"
                    val projection = arrayOf(column)
                    try {
                        cursor = applicationContext.contentResolver.query(
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
                }
                else if ("com.android.providers.media.documents" == uri!!.authority)
                {
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
                        cursor = applicationContext.contentResolver.query(
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
            }
            else if ("content".equals(uri!!.scheme, ignoreCase = true))
            {
                var cursor: Cursor? = null
                val column = "_data"
                val projection = arrayOf(column)
                try {
                    cursor = applicationContext.contentResolver.query(
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

                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                println("bitmap image==$bitmap")
                val file_name = filePath!!.substring(filePath!!.lastIndexOf("/") + 1)
                val filenameArray = file_name.split("\\.".toRegex()).toTypedArray()
                val extension = filenameArray[filenameArray.size - 1]
                println("extension$extension")
                val f = File(filePath)
                val mimeType = URLConnection.guessContentTypeFromName(f.name)
                file_body =  f!!.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                activityProfiledetailsBinding.imgPrf.setImageBitmap(bitmap);
               /* var file_multi= MultipartBody.Part.createFormData(
                    "image",
                    f!!.name,
                    f!!.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                )*/
                val fileReqBody = RequestBody.create("image/jpg".toMediaTypeOrNull(), f!!)
                val part: MultipartBody.Part = MultipartBody.Part.createFormData("image", f.name, fileReqBody)


                submitData(part)
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

                    val tempUri: Uri = getImageUri(applicationContext, bitmap)!!
                    filePath = getRealPathFromURI(tempUri)
                    println("pathasd$filePath")

                    val f = File(filePath)
                    val mimeType = URLConnection.guessContentTypeFromName(f.name)
                   file_body =  f!!.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    activityProfiledetailsBinding.imgPrf.setImageBitmap(bitmap);


                    val fileReqBody = RequestBody.create("image/jpg".toMediaTypeOrNull(), f!!)
                    val part: MultipartBody.Part = MultipartBody.Part.createFormData("image", f.name, fileReqBody)


                    var file_multi= MultipartBody.Part.createFormData(
                        "image",
                        f!!.name,
                        f!!.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    )

                    println("file_bodypathasd$file_body")
                    submitData(part)

                }
            }
        }

    }

    private fun submitData( file_multi: MultipartBody.Part) {
        if (CheckConnectivity.getInstance(applicationContext).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)

            var types = "avatar"
            val type: RequestBody = types.toRequestBody("text/plain".toMediaTypeOrNull())
            val mcall: Call<ResponseBody?>? = uploadAPIs.uploadImage("Bearer "+sessionManager?.getToken(),type,file_multi)
            mcall?.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>,
                )
                {
                    Log.d("hdfhff",response.body().toString())
                    try {
                        if (response.body() != null) {
                            val result = response.body()!!.string()
                            Log.v("responseannouncement-->", result)
                            val mjonsresponse = JSONObject(result)

                            Toast.makeText(this@Profiledetails,"Image uploaded successfully",Toast.LENGTH_LONG).show()
                            getProfiledetails()


                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(applicationContext, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
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
        return parse(path)
    }

    fun getRealPathFromURI(uri: Uri?): String? {
        val cursor = contentResolver.query(uri!!, null, null, null, null)
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
                Toast.makeText(this, "all permissions granted", Toast.LENGTH_LONG).show()

                val layoutInflater =getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val layout: View = layoutInflater.inflate(R.layout.camera_layout, null, false)
                val dialog = BottomSheetDialog(this)
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
                Toast.makeText(this, "Please accept permissions before proceeding", Toast.LENGTH_LONG).show()
            }
        }
    }
}