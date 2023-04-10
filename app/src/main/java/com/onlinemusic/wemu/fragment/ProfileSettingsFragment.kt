package com.onlinemusic.wemu.fragment

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation

import com.onlinemusic.wemu.MainActivity
import com.onlinemusic.wemu.R

import com.onlinemusic.wemu.databinding.FragmentProfileSettingsBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.CountryResponse
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.utils.Utilities
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit


class ProfileSettingsFragment : Fragment() {
    lateinit var fragmentProfileSettingBinding: FragmentProfileSettingsBinding
    var sessionManager: SessionManager? = null
    var countryArr : ArrayList<CountryResponse> = ArrayList()
    lateinit var popup: PopupWindow
    var countryId =""
    var currentpasswordvisible = false
    var newpasswordvisible = false
    var confirmpasswordvisible = false
    private lateinit var mainActivity: MainActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentProfileSettingBinding =
            DataBindingUtil.inflate(inflater,R.layout.fragment_profile_settings, container, false)
        val root = fragmentProfileSettingBinding.root
        this.mainActivity = activity as MainActivity
        sessionManager = SessionManager(requireContext())
        popup = PopupWindow(requireContext())
        mainActivity.mBottomNavigationView.visibility = View.GONE
        fragmentProfileSettingBinding.deleteAcnt.setOnClickListener {
            deleteUser()
        }
        fragmentProfileSettingBinding.countryTxtLL.setOnClickListener {
            showCountries()
        }
        fragmentProfileSettingBinding.saveAllDetails.setOnClickListener {

            if(fragmentProfileSettingBinding.websiteUrl.text.isNotEmpty() && !Patterns.WEB_URL.matcher(fragmentProfileSettingBinding.websiteUrl.text.toString()).matches())
            {
                Toast.makeText(requireContext(), "Enter Correct web link", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveAllDetails()
        }
        fragmentProfileSettingBinding.btnBack.setOnClickListener {
            mainActivity.onBackPressed()
        }
        fragmentProfileSettingBinding.btnNotification.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_notification)
        }
        fragmentProfileSettingBinding.currentpwdHideBtn.setOnClickListener {
            if(!currentpasswordvisible)
            {
                fragmentProfileSettingBinding.etCurrentpassword.transformationMethod = null
                fragmentProfileSettingBinding.currentpwdHideBtn.setImageResource(R.drawable.eye_grey)
                currentpasswordvisible = true

            }
            else
            {
                fragmentProfileSettingBinding.etCurrentpassword.transformationMethod = PasswordTransformationMethod()
                fragmentProfileSettingBinding.currentpwdHideBtn.setImageResource(R.drawable.eye_white)
                currentpasswordvisible = false
            }
        }
        fragmentProfileSettingBinding.newpwdHideBtn.setOnClickListener {
            if(!newpasswordvisible)
            {
                fragmentProfileSettingBinding.etNewpassword.transformationMethod = null
                fragmentProfileSettingBinding.newpwdHideBtn.setImageResource(R.drawable.eye_grey)
                newpasswordvisible = true

            }
            else
            {
                fragmentProfileSettingBinding.etNewpassword.transformationMethod = PasswordTransformationMethod()
                fragmentProfileSettingBinding.newpwdHideBtn.setImageResource(R.drawable.eye_white)
                newpasswordvisible = false
            }
        }

        fragmentProfileSettingBinding.cpwdHideBtn.setOnClickListener {
            if(!confirmpasswordvisible)
            {
                fragmentProfileSettingBinding.etConfirmPassword.transformationMethod = null
                fragmentProfileSettingBinding.cpwdHideBtn.setImageResource(R.drawable.eye_grey)
                confirmpasswordvisible = true

            }
            else
            {
                fragmentProfileSettingBinding.etConfirmPassword.transformationMethod = PasswordTransformationMethod()
                fragmentProfileSettingBinding.cpwdHideBtn.setImageResource(R.drawable.eye_white)
                confirmpasswordvisible = false
            }
        }




        fragmentProfileSettingBinding.btnChangepassword.setOnClickListener {

            if (fragmentProfileSettingBinding.etCurrentpassword.text.length<=7){
                Toast.makeText(requireContext(), "Enter Current Password", Toast.LENGTH_SHORT).show()
            }else if (fragmentProfileSettingBinding.etNewpassword.text.length<=7){
                Toast.makeText(requireContext(), "Enter New Password", Toast.LENGTH_SHORT).show()
            }else if (fragmentProfileSettingBinding.etConfirmPassword.text.length<=7){
                Toast.makeText(requireContext(), "Enter Confirm Password", Toast.LENGTH_SHORT).show()
            }else if (!fragmentProfileSettingBinding.etNewpassword.text.toString().equals(fragmentProfileSettingBinding.etConfirmPassword.text.toString())){
                Toast.makeText(requireContext(), "Password Doesnot Matched!", Toast.LENGTH_SHORT).show()
            }else{
                changepassword()
            }
        }
        getProfileDetails()
        getCountries()
        fragmentProfileSettingBinding.saveGeneralSettings.setOnClickListener {
            saveGeneralSettings()
        }
        fragmentProfileSettingBinding.saveProfileSettings.setOnClickListener {
            saveProfileSettings()
        }


        return root
    }
    private fun saveAllDetails() {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {
            var ageCheck=""
            showProgressDialog()
            if(fragmentProfileSettingBinding.ageEdt.text.toString() == "")
            {
                ageCheck="0"
            }
            else
            {
                ageCheck=fragmentProfileSettingBinding.ageEdt.text.toString()
            }


            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val country_id: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), countryId)
            val gender: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), fragmentProfileSettingBinding.gender.text.toString())
            val age: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), ageCheck)
            val fullName: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), fragmentProfileSettingBinding.fullName.text.toString())
            val about: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), fragmentProfileSettingBinding.descriptionAbt.text.toString())
            val facebook_user: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), fragmentProfileSettingBinding.facebookUser.text.toString())
            val website: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), fragmentProfileSettingBinding.websiteUrl.text.toString())
            val mcall: Call<ResponseBody?>? =
                uploadAPIs.updateAllProfileSettings("Bearer "+sessionManager?.getToken(),country_id,gender,age,
                    fullName, about,facebook_user, website)
            mcall?.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: retrofit2.Response<ResponseBody?>,
                ) {
                    try {
                        if (response.body() != null) {
                            val result = response.body()!!.string()
                            Log.v("responseannouncement-->", result)
                            val mjonsresponse = JSONObject(result)
                            val status = mjonsresponse.getBoolean("status")
                            val message = mjonsresponse.getString("message")

                            //  Toast.makeText(this@Setting, message, Toast.LENGTH_SHORT).show()
                            if (status) {
                                Utilities.alertDialogUtil(mainActivity,"Login",message,
                                    isCancelable = false,
                                    isPositive = true,
                                    isNegetive = false,
                                    isNeutral = false,"Ok","","",object : Utilities.OnItemClickListener{
                                        override fun onItemClickAction(
                                            type: Int,
                                            dialogInterface: DialogInterface
                                        ) {
                                            if (type==1){
                                                getProfileDetails()
                                                dialogInterface.dismiss()
                                                fragmentProfileSettingBinding.mainLl.clearFocus();
                                            }
                                        }

                                    })

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
    }

    private fun saveProfileSettings() {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val country_id: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), countryId)
            val gender: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), fragmentProfileSettingBinding.gender.text.toString())
            val age: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), fragmentProfileSettingBinding.ageEdt.text.toString())
            val fullName: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), fragmentProfileSettingBinding.fullName.text.toString())
            val about: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), fragmentProfileSettingBinding.descriptionAbt.text.toString())
            val facebook_user: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), fragmentProfileSettingBinding.facebookUser.text.toString())
            val website: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), fragmentProfileSettingBinding.websiteUrl.text.toString())
            val mcall: Call<ResponseBody?>? =
                uploadAPIs.updateAllProfileSettings("Bearer "+sessionManager?.getToken(),country_id,gender,age,
                    fullName, about,facebook_user, website)
            mcall?.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: retrofit2.Response<ResponseBody?>,
                ) {
                    try {
                        if (response.body() != null) {
                            val result = response.body()!!.string()
                            Log.v("responseannouncement-->", result)
                            val mjonsresponse = JSONObject(result)
                            val status = mjonsresponse.getBoolean("status")
                            val message = mjonsresponse.getString("message")
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                            if (status) {
                                getProfileDetails()
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
    }

    private fun saveGeneralSettings() {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val country_id: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), countryId)
            val gender: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), fragmentProfileSettingBinding.gender.text.toString())
            val age: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), fragmentProfileSettingBinding.ageEdt.text.toString())
            val mcall: Call<ResponseBody?>? = uploadAPIs.updateGeneralSettings("Bearer "+sessionManager?.getToken(), country_id, gender, age)
            mcall?.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: retrofit2.Response<ResponseBody?>,
                ) {
                    try {
                        if (response.body() != null) {
                            val result = response.body()!!.string()
                            Log.v("responseannouncement-->", result)
                            val mjonsresponse = JSONObject(result)
                            val status = mjonsresponse.getBoolean("status")
                            val message = mjonsresponse.getString("message")
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                            if (status) {
                                getProfileDetails()
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
    }

    private fun showCountries() {
        val layoutInflater = mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout: View = layoutInflater.inflate(R.layout.popup_layout_state, null, false)

        val main_layout_seasonlist =
            layout.findViewById<View>(R.id.main_layout_seasonlist) as LinearLayout

        // Creating the PopupWindow

        // Creating the PopupWindow
        popup.contentView = layout
        popup.width = fragmentProfileSettingBinding.countryTxtLL.width
        popup.height = LinearLayout.LayoutParams.WRAP_CONTENT
        popup.isOutsideTouchable = true
        popup.isFocusable = true
        for (i in countryArr.indices) {
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            params.setMargins(10, 10, 10, 10)
            val tv = TextView(mainActivity)
            tv.layoutParams = params
            tv.text = countryArr[i].name
            tv.tag = countryArr[i].name
            tv.setTextColor(Color.parseColor("#000000"))
            tv.setPadding(5, 5, 5, 5)
            tv.textSize = 15f
            tv.setOnClickListener {


                fragmentProfileSettingBinding.countryTxt.setText(countryArr[i].name)
                Log.d("country",countryArr[i].id.toString())
                countryId = countryArr[i].id.toString()
                popup.dismiss()
            }
            main_layout_seasonlist.addView(tv)
        }


        popup.setOnDismissListener(PopupWindow.OnDismissListener { // TODO Auto-generated method stub
            popup.dismiss()
        })


        popup.setBackgroundDrawable(BitmapDrawable())
        popup.showAsDropDown( fragmentProfileSettingBinding.countryTxtLL)
    }

    private fun getCountries() {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<ResponseBody?>? = uploadAPIs.getCountryData("Bearer "+sessionManager?.getToken())
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
                            val data = mjonsresponse.getJSONArray("data")
                            for(i in 0 until data.length())
                            {

                                var id = data.getJSONObject(i).getInt("id")
                                var shortname = data.getJSONObject(i).getString("shortname")
                                var name = data.getJSONObject(i).getString("name")
                                var countryResponse = CountryResponse(id.toString().toInt(),shortname,name)
                                countryArr.add(countryResponse)
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
        }
        else
        {
            Toast.makeText(mainActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteUser() {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<ResponseBody?>? = uploadAPIs.deleteProfile("Bearer "+sessionManager?.getToken())
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
                            val status = mjonsresponse.getBoolean("success")
                            val message = mjonsresponse.getString("message")
                           /* Toast.makeText(this@Setting, message, Toast.LENGTH_SHORT).show()
                            if(status.equals("true"))
                            {
                                sessionManager?.logoutUser()
                                val intent = Intent(this@Setting, Login::class.java)
                                startActivity(intent)
                                finish()
                            }*/

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
        }
        else
        {
            Toast.makeText(mainActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getProfileDetails() {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {
            Log.d("jvjhb","hit")
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
                            val data = mjonsresponse.getJSONObject("data")
                            //  Toast.makeText(this@Setting, message, Toast.LENGTH_SHORT).show()
                            if (status) {

                                val username =if (data.isNull("username")) "" else  data.getString("username")
                                val email =if (data.isNull("email")) "" else  data.getString("email")
                                val name = if (data.isNull("name")) "" else data.getString("name")
                                val gender = if (data.isNull("gender")) "" else data.getString("gender")
                                val language =if (data.isNull("language")) "" else  data.getString("language")
                                val avatar = if (data.isNull("avatar")) "" else data.getString("avatar")
                                val cover = if (data.isNull("cover")) "" else data.getString("cover")
                                val dob =  if (data.isNull("dob")) "" else data.getString("dob")
                                val facebook = if (data.isNull("facebook")) "" else data.getString("facebook")
                                val website = if (data.isNull("website")) "" else data.getString("website")
                                val countryid = if (data.isNull("country_id")) 0 else data.getInt("country_id")
                                val about = if (data.isNull("about")) "" else data.getString("about")
                                fragmentProfileSettingBinding.descriptionAbt.setText(about)
                                if(countryid == 0)
                                {
                                    fragmentProfileSettingBinding.countryTxt.setText("")
                                    countryId ="0"
                                }
                                else
                                {
                                    var countryData = data.getJSONObject("country")
                                    val countryname = countryData.getString("name")?:""
                                    val country_id = countryData.getString("id")?:""
                                    fragmentProfileSettingBinding.countryTxt.setText(countryname)
                                    countryId =country_id
                                }
                                /*     if(data.getJSONObject("country") == "null")
                                     {

                                     }
                                     else
                                     {

                                     }*/

                                val age = if (data.isNull("age")) "" else data.getInt("age").toString()
                                Log.d("age",age)
                                if(age.equals("0"))
                                {
                                    fragmentProfileSettingBinding.ageEdt.setText("")
                                }
                                else
                                {
                                    fragmentProfileSettingBinding.ageEdt.setText(age)
                                }

                                fragmentProfileSettingBinding.etUsername.setText(username)
                                fragmentProfileSettingBinding.etEmail.setText(email)
                                fragmentProfileSettingBinding.gender.setText(gender)
                                fragmentProfileSettingBinding.fullName.setText(name)
                               /* if (facebook.isNotEmpty()) {
                                    fragmentProfileSettingBinding.facebookUser.visibility=View.VISIBLE
                                    fragmentProfileSettingBinding.facebookUser.setText(facebook)
                                }else{*/
                                    fragmentProfileSettingBinding.facebookUser.visibility=View.GONE
                               // }
                                fragmentProfileSettingBinding.websiteUrl.setText(website)





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



    }

    private fun changepassword(){

        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val password: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), fragmentProfileSettingBinding.etNewpassword.text.toString())
            val password_confirmation: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), fragmentProfileSettingBinding.etConfirmPassword.text.toString())
            val old_password: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), fragmentProfileSettingBinding.etCurrentpassword.text.toString())
            val mcall: Call<ResponseBody?>? = uploadAPIs.changepassword("Bearer "+sessionManager?.getToken(), password, password_confirmation, old_password)
            mcall?.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: retrofit2.Response<ResponseBody?>,
                ) {
                    try {
                        if (response.body() != null) {
                            val result = response.body()!!.string()
                            Log.v("responseannouncement-->", result)
                            val mjonsresponse = JSONObject(result)
                            val status = mjonsresponse.getBoolean("status")
                            val message = mjonsresponse.getString("message")
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                            /*if (status) {
                                val intent = Intent(this@Setting, Setting::class.java)
                                startActivity(intent)
                                finish()
                            }*/
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
//            val params = JSONObject()
//            try {
//                params.put("password", activitySettingBinding.etNewpassword.text.toString())
//                params.put("password_confirmation", activitySettingBinding.etConfirmPassword.text.toString())
//                params.put("old_password", activitySettingBinding.etCurrentpassword.text.toString())
//
//            } catch (e: JSONException) {
//                e.printStackTrace()
//            }
//            val jsonRequest: JsonObjectRequest = object : JsonObjectRequest(
//                Method.POST, Allurl.ChangePassword, params,
//                Response.Listener { response: JSONObject ->
//                    Log.i("Response-->", response.toString())
//                    try {
//
//                        val result = JSONObject(response.toString())
//                        val status = result.getBoolean("status")
//                        val message = result.getString("message")
//                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//                        if (status) {
//                            val intent = Intent(this, Setting::class.java)
//                            startActivity(intent)
//                            finish()
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
//            Toast.makeText(
//                applicationContext,
//                "Ooops! Internet Connection Error",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
    }


    var mProgressDialog: ProgressDialog? = null

    fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(requireContext())
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