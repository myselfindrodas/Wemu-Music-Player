package com.onlinemusic.wemu

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.onlinemusic.wemu.databinding.ActivitySettingBinding
import com.onlinemusic.wemu.responseModel.CountryResponse

import com.onlinemusic.wemu.internet.CheckConnectivity
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

class Setting : AppCompatActivity() {

    lateinit var activitySettingBinding: ActivitySettingBinding
    var sessionManager: SessionManager? = null
    var countryArr : ArrayList<CountryResponse> = ArrayList()
    lateinit var popup: PopupWindow
    var countryId =""
    var currentpasswordvisible = false
    var newpasswordvisible = false
    var confirmpasswordvisible = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySettingBinding = DataBindingUtil.setContentView(this, R.layout.activity_setting)
        sessionManager = SessionManager(applicationContext)
        popup = PopupWindow(this@Setting)
        activitySettingBinding.deleteAcnt.setOnClickListener {
            deleteUser()
        }
        activitySettingBinding.countryTxtLL.setOnClickListener {
            showCountries()
        }
        activitySettingBinding.saveAllDetails.setOnClickListener {
            saveAllDetails()
        }
        activitySettingBinding.btnNotification.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_notification)
        }
        activitySettingBinding.currentpwdHideBtn.setOnClickListener {
            if(!currentpasswordvisible)
            {
                activitySettingBinding.etCurrentpassword.transformationMethod = null
                activitySettingBinding.currentpwdHideBtn.setImageResource(R.drawable.eye_grey)
                currentpasswordvisible = true

            }
            else
            {
                activitySettingBinding.etCurrentpassword.transformationMethod = PasswordTransformationMethod()
                activitySettingBinding.currentpwdHideBtn.setImageResource(R.drawable.eye_white)
                currentpasswordvisible = false
            }
        }
        activitySettingBinding.newpwdHideBtn.setOnClickListener {
            if(!newpasswordvisible)
            {
                activitySettingBinding.etNewpassword.transformationMethod = null
                activitySettingBinding.newpwdHideBtn.setImageResource(R.drawable.eye_grey)
                newpasswordvisible = true

            }
            else
            {
                activitySettingBinding.etNewpassword.transformationMethod = PasswordTransformationMethod()
                activitySettingBinding.newpwdHideBtn.setImageResource(R.drawable.eye_white)
                newpasswordvisible = false
            }
        }

        activitySettingBinding.cpwdHideBtn.setOnClickListener {
            if(!confirmpasswordvisible)
            {
                activitySettingBinding.etConfirmPassword.transformationMethod = null
                activitySettingBinding.cpwdHideBtn.setImageResource(R.drawable.eye_grey)
                confirmpasswordvisible = true

            }
            else
            {
                activitySettingBinding.etConfirmPassword.transformationMethod = PasswordTransformationMethod()
                activitySettingBinding.cpwdHideBtn.setImageResource(R.drawable.eye_white)
                confirmpasswordvisible = false
            }
        }




        activitySettingBinding.btnChangepassword.setOnClickListener {

            if (activitySettingBinding.etCurrentpassword.text.length<=7){
                Toast.makeText(this, "Enter Current Password", Toast.LENGTH_SHORT).show()
            }else if (activitySettingBinding.etNewpassword.text.length<=7){
                Toast.makeText(this, "Enter New Password", Toast.LENGTH_SHORT).show()
            }else if (activitySettingBinding.etConfirmPassword.text.length<=7){
                Toast.makeText(this, "Enter Confirm Password", Toast.LENGTH_SHORT).show()
            }else if (!activitySettingBinding.etNewpassword.text.toString().equals(activitySettingBinding.etConfirmPassword.text.toString())){
                Toast.makeText(this, "Password Doesnot Matched!", Toast.LENGTH_SHORT).show()
            }else{
                changepassword()
            }
        }
        getProfileDetails()
        getCountries()
        activitySettingBinding.saveGeneralSettings.setOnClickListener {
            saveGeneralSettings()
        }
        activitySettingBinding.saveProfileSettings.setOnClickListener {
            saveProfileSettings()
        }



    }
    private fun saveAllDetails() {
        if (CheckConnectivity.getInstance(applicationContext).isOnline) {
            var ageCheck=""
            showProgressDialog()
            if(activitySettingBinding.ageEdt.text.toString() == "")
            {
                ageCheck="0"
            }
            else
            {
                ageCheck=activitySettingBinding.ageEdt.text.toString()
            }

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val country_id: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), countryId)
            val gender: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySettingBinding.gender.text.toString())
            val age: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), ageCheck)
            val fullName: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySettingBinding.fullName.text.toString())
            val about: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySettingBinding.descriptionAbt.text.toString())
            val facebook_user: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySettingBinding.facebookUser.text.toString())
            val website: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySettingBinding.websiteUrl.text.toString())
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
                                Utilities.alertDialogUtil(this@Setting,"Login",message,
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
                                                activitySettingBinding.mainLl.clearFocus();
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
            Toast.makeText(applicationContext, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveProfileSettings() {
        if (CheckConnectivity.getInstance(applicationContext).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val country_id: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), countryId)
            val gender: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySettingBinding.gender.text.toString())
            val age: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySettingBinding.ageEdt.text.toString())
            val fullName: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySettingBinding.fullName.text.toString())
            val about: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySettingBinding.descriptionAbt.text.toString())
            val facebook_user: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySettingBinding.facebookUser.text.toString())
            val website: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySettingBinding.websiteUrl.text.toString())
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
                            Toast.makeText(this@Setting, message, Toast.LENGTH_SHORT).show()
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
            Toast.makeText(applicationContext, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveGeneralSettings() {
        if (CheckConnectivity.getInstance(applicationContext).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val country_id: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), countryId)
            val gender: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySettingBinding.gender.text.toString())
            val age: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySettingBinding.ageEdt.text.toString())
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
                            Toast.makeText(this@Setting, message, Toast.LENGTH_SHORT).show()
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
            Toast.makeText(applicationContext, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCountries() {
        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout: View = layoutInflater.inflate(R.layout.popup_layout_state, null, false)

        val main_layout_seasonlist =
            layout.findViewById<View>(R.id.main_layout_seasonlist) as LinearLayout

        // Creating the PopupWindow

        // Creating the PopupWindow
        popup.contentView = layout
        popup.width = activitySettingBinding.countryTxtLL.width
        popup.height = LinearLayout.LayoutParams.WRAP_CONTENT
        popup.isOutsideTouchable = true
        popup.isFocusable = true
        for (i in countryArr.indices) {
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            params.setMargins(10, 10, 10, 10)
            val tv = TextView(this)
            tv.layoutParams = params
            tv.text = countryArr[i].name
            tv.tag = countryArr[i].name
            tv.setTextColor(Color.parseColor("#000000"))
            tv.setPadding(5, 5, 5, 5)
            tv.textSize = 15f
            tv.setOnClickListener {


                activitySettingBinding.countryTxt.setText(countryArr[i].name)
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
        popup.showAsDropDown( activitySettingBinding.countryTxtLL)
    }

    private fun getCountries() {
        if (CheckConnectivity.getInstance(applicationContext).isOnline) {

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
            Toast.makeText(applicationContext, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteUser() {
        if (CheckConnectivity.getInstance(applicationContext).isOnline) {

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
                            Toast.makeText(this@Setting, message, Toast.LENGTH_SHORT).show()
                            if(status.equals("true"))
                            {
                                sessionManager?.logoutUser()
                                val intent = Intent(this@Setting, Login::class.java)
                                startActivity(intent)
                                finish()
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
            Toast.makeText(applicationContext, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getProfileDetails() {
        if (CheckConnectivity.getInstance(applicationContext).isOnline) {
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

                                val username = data.getString("username")
                                val email = data.getString("email")
                                val name = data.getString("name")
                                val gender = data.getString("gender")
                                val language = data.getString("language")
                                val avatar = data.getString("avatar")
                                val cover = data.getString("cover")
                                val dob = data.getString("dob")
                                val facebook = data.getString("facebook")
                                val website = data.getString("website")
                                val countryid = data.getInt("country_id")
                                val about = data.getString("about")
                                activitySettingBinding.descriptionAbt.setText(about)
                                if(countryid == 0)
                                {
                                    activitySettingBinding.countryTxt.setText("")
                                    countryId ="0"
                                }
                                else
                                {
                                    var countryData = data.getJSONObject("country")
                                    val countryname = countryData.getString("name")
                                    val country_id = countryData.getString("id")
                                    activitySettingBinding.countryTxt.setText(countryname)
                                    countryId =country_id
                                }
                                /*     if(data.getJSONObject("country") == "null")
                                     {

                                     }
                                     else
                                     {

                                     }*/

                                val age = data.getInt("age").toString()
                                Log.d("age",age)
                                if(age.equals("0"))
                                {
                                    activitySettingBinding.ageEdt.setText("")
                                }
                                else
                                {
                                    activitySettingBinding.ageEdt.setText(age)
                                }

                                activitySettingBinding.etUsername.setText(username)
                                activitySettingBinding.etEmail.setText(email)
                                activitySettingBinding.gender.setText(gender)
                                activitySettingBinding.fullName.setText(name)
                                activitySettingBinding.facebookUser.setText(facebook)
                                activitySettingBinding.websiteUrl.setText(website)





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



    }

    private fun changepassword(){

        if (CheckConnectivity.getInstance(applicationContext).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val password: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySettingBinding.etNewpassword.text.toString())
            val password_confirmation: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySettingBinding.etConfirmPassword.text.toString())
            val old_password: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySettingBinding.etCurrentpassword.text.toString())
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
                            Toast.makeText(this@Setting, message, Toast.LENGTH_SHORT).show()
                            if (status) {
                                val intent = Intent(this@Setting, Setting::class.java)
                                startActivity(intent)
                                finish()
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
}