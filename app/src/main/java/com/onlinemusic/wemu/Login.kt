package com.onlinemusic.wemu


import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.messaging.FirebaseMessaging
import com.onlinemusic.wemu.databinding.ActivityLoginBinding
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


class Login : AppCompatActivity() {


    lateinit var activityLoginBinding: ActivityLoginBinding
    var sessionManager: SessionManager? = null
    var isPasswordVisible = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        sessionManager = SessionManager(this)
        fetchFCMToken()
        activityLoginBinding.btnSignup.setOnClickListener {

            val intent = Intent(this@Login, Signup::class.java)
            startActivity(intent)
        }

        activityLoginBinding.btnForgotpassword.setOnClickListener {

            val intent = Intent(this@Login, Forgotpassword::class.java)
            startActivity(intent)
        }
        activityLoginBinding.pwdHideBtn.setOnClickListener {
            if (!isPasswordVisible) {
                activityLoginBinding.etPassword.transformationMethod = null
                activityLoginBinding.pwdHideBtn.setImageResource(R.drawable.eye_grey)
                isPasswordVisible = true

            } else {
                activityLoginBinding.etPassword.transformationMethod =
                    PasswordTransformationMethod()
                activityLoginBinding.pwdHideBtn.setImageResource(R.drawable.eye_white)
                isPasswordVisible = false
            }
            activityLoginBinding.etPassword.setSelection(activityLoginBinding.etPassword.length())
        }

        activityLoginBinding.btnLogin.setOnClickListener {

            if (activityLoginBinding.etUsername.text.length == 0) {
                Toast.makeText(this, "Enter Username", Toast.LENGTH_SHORT).show()
            } else if (activityLoginBinding.etPassword.text.length <= 7) {
                Toast.makeText(
                    this,
                    "The password must be at least 8 characters.",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (isValidEmail(activityLoginBinding.etUsername.text.toString())) {
                login()
            } else {
                Toast.makeText(this, "Please provide valid email address", Toast.LENGTH_SHORT)
                    .show()
            }

        }
    }

    fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    private fun fetchFCMToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { result ->
            if (result != null) {
                sessionManager?.setFCMToken(result)
                // DO your thing with your firebase token
            }
        }

    }

    fun login() {

        if (CheckConnectivity.getInstance(this).isOnline) {
            val android_id: String = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ANDROID_ID
            );
            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)

            hideKeyboard(this)
            val email: RequestBody = RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                activityLoginBinding.etUsername.text.toString()
            )
            val password: RequestBody = RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                activityLoginBinding.etPassword.text.toString()
            )
            val udid: RequestBody = RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                sessionManager?.getFCMToken().toString()
            )
            val device_type: RequestBody =
                RequestBody.create("text/plain".toMediaTypeOrNull(), "android")

            val mcall: Call<ResponseBody?>? = uploadAPIs.login(email, password, udid, device_type)
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
                            var message: String? = ""
                            message = mjonsresponse.getString("message")
                            /*   if (!mjonsresponse.isNull("errors")) {
                                   message = mjonsresponse.getString("errors")
                               } else {
                                   message = mjonsresponse.getString("message")
                               }*/

//                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            if (response.code() == 200) {
                                if (status == true) {
                                    val data = mjonsresponse.getJSONObject("data")
                                    val token = data.getJSONObject("tokens")
                                    val id = data.getInt("id")
                                    val username = data.getString("username")
                                    val email = data.getString("email")
                                    val name = data.getString("name")
                                    val gender = data.getString("gender")
                                    val avatar = data.getString("avatar")
                                    val coverpic = data.getString("cover")
                                    val paymentstatus = data.getString("payment_status")
                                    val accesstoken = token.getString("access_token")
                                    val refreshtoken = token.getString("refresh_token")
                                    val subscription = data.getString("subscription")
                                    Log.d("paymentstatus", paymentstatus.toString())
                                    if (subscription.equals("1")) {
                                        sessionManager?.setSubscribed(true)
                                    } else {
                                        sessionManager?.setSubscribed(false)

                                    }

                                    sessionManager?.setToken(accesstoken)
                                    sessionManager?.setUsername(username)


                                    sessionManager?.createLoginSession(
                                        activityLoginBinding.etUsername.text.toString(),
                                        activityLoginBinding.etPassword.text.toString()
                                    )

                                    /*val builder = AlertDialog.Builder(this@Login)
                                    builder.setMessage(message)
                                    builder.setPositiveButton(
                                        "Ok"
                                    ) { dialog, which ->
                                        val intent = Intent(this@Login, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                        dialog.cancel()

                                    }
                                    val alert = builder.create()
                                    alert.show()*/

                                    Utilities.alertDialogUtil(this@Login,
                                        "Login",
                                        message,
                                        isCancelable = false,
                                        isPositive = true,
                                        isNegetive = false,
                                        isNeutral = false,
                                        "Ok",
                                        "",
                                        "",
                                        object : Utilities.OnItemClickListener {
                                            override fun onItemClickAction(
                                                type: Int,
                                                dialogInterface: DialogInterface
                                            ) {
                                                if (type == 1) {
                                                    val intent =
                                                        Intent(this@Login, MainActivity::class.java)
                                                    startActivity(intent)
                                                    dialogInterface.dismiss()
                                                    this@Login.finish()
                                                }
                                            }

                                        })


                                } else {
                                    Utilities.alertDialogUtil(this@Login,
                                        "Login",
                                        message,
                                        isCancelable = false,
                                        isPositive = true,
                                        isNegetive = false,
                                        isNeutral = false,
                                        "Ok",
                                        "",
                                        "",
                                        object : Utilities.OnItemClickListener {
                                            override fun onItemClickAction(
                                                type: Int,
                                                dialogInterface: DialogInterface
                                            ) {
                                                if (type == 1) {
                                                    dialogInterface.dismiss()
                                                }
                                            }

                                        })
                                }
                            } else if (response.code() == 401) {
                                Utilities.alertDialogUtil(this@Login,
                                    "Login",
                                    message,
                                    isCancelable = false,
                                    isPositive = true,
                                    isNegetive = false,
                                    isNeutral = false,
                                    "Ok",
                                    "",
                                    "",
                                    object : Utilities.OnItemClickListener {
                                        override fun onItemClickAction(
                                            type: Int,
                                            dialogInterface: DialogInterface
                                        ) {
                                            if (type == 1) {
                                                dialogInterface.dismiss()
                                            }
                                        }

                                    })

                            } else {
                                Utilities.alertDialogUtil(this@Login,
                                    "Login",
                                    message,
                                    isCancelable = false,
                                    isPositive = true,
                                    isNegetive = false,
                                    isNeutral = false,
                                    "Ok",
                                    "",
                                    "",
                                    object : Utilities.OnItemClickListener {
                                        override fun onItemClickAction(
                                            type: Int,
                                            dialogInterface: DialogInterface
                                        ) {
                                            if (type == 1) {
                                                dialogInterface.dismiss()
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
        }else {
            Toast.makeText(this, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
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

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}