package com.onlinemusic.wemu

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.onlinemusic.wemu.databinding.ActivityResetpasswordBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit

class Resetpassword : AppCompatActivity() {

    lateinit var activityResetpasswordBinding: ActivityResetpasswordBinding
    var forgotpasswordemail: String?=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityResetpasswordBinding = DataBindingUtil.setContentView(this, R.layout.activity_resetpassword)
        val intent = intent
        forgotpasswordemail = intent.getStringExtra("forgotpasswordemail")

        activityResetpasswordBinding.btnSubmit.setOnClickListener {

            if (activityResetpasswordBinding.etPassword.text.length==0){
                Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show()
            }else if (activityResetpasswordBinding.etconfirmPassword.text.length==0){
                Toast.makeText(this, "Enter Confirm Password", Toast.LENGTH_SHORT).show()
            }else if (!activityResetpasswordBinding.etPassword.text.toString().equals(activityResetpasswordBinding.etconfirmPassword.text.toString())){
                Toast.makeText(this, "Password Doesnot Matched!", Toast.LENGTH_SHORT).show()
            }else{

                resetpassword()

            }

        }
    }

    private fun resetpassword(){


        if (CheckConnectivity.getInstance(applicationContext).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val email: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), forgotpasswordemail.toString())
            val password: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activityResetpasswordBinding.etPassword.text.toString())
            val password_confirmation: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activityResetpasswordBinding.etconfirmPassword.text.toString())
            val mcall: Call<ResponseBody?>? = uploadAPIs.resetpassword(email, password, password_confirmation)
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
                            Toast.makeText(this@Resetpassword, message, Toast.LENGTH_SHORT).show()
                            if (status) {
                                val intent = Intent(this@Resetpassword, Login::class.java)
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