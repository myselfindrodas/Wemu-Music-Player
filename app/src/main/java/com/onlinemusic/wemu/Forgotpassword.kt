package com.onlinemusic.wemu

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil

import com.onlinemusic.wemu.databinding.ActivityForgotpasswordBinding
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

class Forgotpassword : AppCompatActivity() {

    lateinit var activityForgotpasswordBinding: ActivityForgotpasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityForgotpasswordBinding = DataBindingUtil.setContentView(this, R.layout.activity_forgotpassword)

        activityForgotpasswordBinding.btnGetOTP.setOnClickListener {

            if (activityForgotpasswordBinding.etUsername.text.isEmpty()){
                Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show()
            }else{

                forgotpassword()

            }


        }
    }

    private fun forgotpassword(){


        if (CheckConnectivity.getInstance(applicationContext).isOnline) {

        showProgressDialog()

        val retrofit: Retrofit = ApiClient.retrofitInstance!!
        val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
        val email: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activityForgotpasswordBinding.etUsername.text.toString())
        val mcall: Call<ResponseBody?>? = uploadAPIs.forgotpassword(email)
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
                        Toast.makeText(this@Forgotpassword, message, Toast.LENGTH_SHORT).show()
                        if (status) {
                            val intent = Intent(this@Forgotpassword, OTPverification::class.java)
                            intent.putExtra("forgotpasswordemail", activityForgotpasswordBinding.etUsername.text.toString())
                            intent.putExtra("forgotpassword", "forgotpassword")
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