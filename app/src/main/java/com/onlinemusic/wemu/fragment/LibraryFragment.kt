package com.onlinemusic.wemu.fragment

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
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
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.onlinemusic.wemu.*
import com.onlinemusic.wemu.databinding.FragmentLibraryBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit


class LibraryFragment : Fragment() {

    lateinit var libraryBinding: FragmentLibraryBinding


    lateinit var mainActivity:MainActivity
    var sessionManager: SessionManager? = null

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

        libraryBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_library,container,false);
        val root = libraryBinding.root
        mainActivity=activity as MainActivity


        libraryBinding.btnNotification.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_notification)
        }
        mainActivity.mBottomNavigationView?.visibility = View.VISIBLE

        sessionManager = SessionManager(mainActivity)
        var subscribeStatus = sessionManager?.getSubscribed()
        if(subscribeStatus == false)
        {
            libraryBinding.btnMyplaylist.visibility= View.GONE
        }
        else
        {
            libraryBinding.btnMyplaylist.visibility= View.VISIBLE

        }

        if (sessionManager?.getTheme().equals("night")){

            libraryBinding.tvTitle.setTextColor(getResources().getColor(R.color.white))
            libraryBinding.tvYourtitle.setTextColor(getResources().getColor(R.color.white))
            libraryBinding.btnNewmusic.setTextColor(getResources().getColor(R.color.white))
            libraryBinding.btnTopmusic.setTextColor(getResources().getColor(R.color.white))
            libraryBinding.btnAlbum.setTextColor(getResources().getColor(R.color.white))
            libraryBinding.btnRecentlyplayed.setTextColor(getResources().getColor(R.color.white))
            libraryBinding.btnMyplaylist.setTextColor(getResources().getColor(R.color.white))
            libraryBinding.btnMyFav.setTextColor(getResources().getColor(R.color.white))
            libraryBinding.btnArtist.setTextColor(getResources().getColor(R.color.white))
            libraryBinding.btnPartner.setTextColor(getResources().getColor(R.color.white))
            //libraryBinding.btnPartnerText.setTextColor(getResources().getColor(R.color.white))
            libraryBinding.btnNotification.setColorFilter(ContextCompat.getColor(mainActivity, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY)

        }
        else
        {
            //libraryBinding.rlBgtheme.setBackgroundColor(getResources().getColor(R.color.white))

            libraryBinding.tvTitle.setTextColor(getResources().getColor(R.color.black))
            libraryBinding.tvYourtitle.setTextColor(getResources().getColor(R.color.black))
            libraryBinding.btnNewmusic.setTextColor(getResources().getColor(R.color.black))
            libraryBinding.btnTopmusic.setTextColor(getResources().getColor(R.color.black))
            libraryBinding.btnAlbum.setTextColor(getResources().getColor(R.color.black))
            libraryBinding.btnRecentlyplayed.setTextColor(getResources().getColor(R.color.black))
            libraryBinding.btnMyplaylist.setTextColor(getResources().getColor(R.color.black))
            libraryBinding.btnMyFav.setTextColor(getResources().getColor(R.color.black))
            libraryBinding.btnArtist.setTextColor(getResources().getColor(R.color.black))
            libraryBinding.btnPartner.setTextColor(getResources().getColor(R.color.black))
           // libraryBinding.btnPartnerText.setTextColor(getResources().getColor(R.color.black))
            libraryBinding.btnNotification.setColorFilter(ContextCompat.getColor(mainActivity, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY)
           // libraryBinding.btnArtist.setColorFilter(ContextCompat.getColor(mainActivity, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY)

        }
        libraryBinding.btnMyplaylist.setOnClickListener {

            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_playlist)


        }
        libraryBinding.btnAlbum.setOnClickListener {

            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_album)

        }
        libraryBinding.btnNewmusic.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_newmusic)

        }


        libraryBinding.btnTopmusic.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_topmusic)
        }

        libraryBinding.btnRecentlyplayed.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("key","recent")
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_recentlyplayed, bundle)
        }


        libraryBinding.btnMyFav.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_favourite)
        }

        libraryBinding.btnArtist.setOnClickListener {

            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_artist)

        }
        libraryBinding.btnPartner.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://developer.shyamfuture.in/wemuonline/public/partner")))
        }
        getProfiledetails()
        return root
    }



    private fun getProfiledetails(){

        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

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
                            // Toast.makeText(mainActivity, message, Toast.LENGTH_SHORT).show()
                            if (status) {


                                val avatar = data.getString("avatar")


                                /* Glide.with(this@Profiledetails)
                                     .load("https://developer.shyamfuture.in/wemuonline/public/upload/photos/" + avatar)
                                     .into(activityProfiledetailsBinding.imgPrf)*/
                                Glide.with(mainActivity)
                                    .load("$image_url/$avatar")
                                    .timeout(6000)
                                    .error(R.drawable.logo)
                                    .placeholder(R.drawable.logo)
                                    .into(libraryBinding.imgPrf)


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
            Toast.makeText(mainActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
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