package com.onlinemusic.wemu

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.version_check.VersionRequestModel
import com.onlinemusic.wemu.responseModel.version_check.version_response.VersionCheckResponse
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.utils.PrefManager
import com.onlinemusic.wemu.utils.Utilities
import com.onlinemusic.wemu.utils.Welcome
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit


class Splash : AppCompatActivity() {

    private var prefManager: PrefManager? = null
    var sessionManager: SessionManager? = null

    companion object {

        private final val CLIENT_ID = "cbb15eb84f444e86be30a25cde8c2eaf"
        private final val REDIRECT_URI = "com.example.wemu://callback"
        private final val REQUEST_CODE = 1337;
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        prefManager = PrefManager(this)
        sessionManager = SessionManager(this)
     //   throw RuntimeException("Test Crash") // Force a crash
       // gotoHomePage()
        appUpdate()
    }

    private fun appUpdate() {
        if (CheckConnectivity.getInstance(this).isOnline) {

            //showProgressDialog()
            val pInfo = packageManager.getPackageInfo(packageName, 0)
           // val versionCodes = BuildConfig.VERSION_CODE
            val versionCodes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                BuildConfig.VERSION_CODE
            }
            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<VersionCheckResponse?>? = uploadAPIs.postVersionCheck(
                VersionRequestModel("0", versionCodes.toString())
            )
            mcall?.enqueue(object : Callback<VersionCheckResponse?> {
                override fun onResponse(
                    call: Call<VersionCheckResponse?>,
                    response: retrofit2.Response<VersionCheckResponse?>,
                ) {
                    try {
                        if (response.isSuccessful && response.body() != null) {

                            val result = response.body()!!
                            /* Log.v("responseannouncement-->", result)
                             val mjonsresponse = JSONObject(result)
                             val data = mjonsresponse.getJSONObject("data")*/
                            result.apply {

                                val status = result.status
                                if (status == true) {
                                    Toast.makeText(
                                        this@Splash,
                                        result.message,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    Utilities.alertDialogUtil(this@Splash,
                                        "Update WEMU",
                                        result.message.toString(),
                                        isCancelable = false,
                                        isPositive = true,
                                        isNegetive = false,
                                        isNeutral = false,
                                        "Update Now",
                                        "",
                                        "",
                                        object : Utilities.OnItemClickListener {
                                            override fun onItemClickAction(
                                                type: Int,
                                                dialogInterface: DialogInterface
                                            ) {
                                                if (type == 1) {
                                                    dialogInterface.dismiss()

                                                    gotoPlaystore()
                                                    finish()
                                                }
                                            }

                                        })

                                } else {

                                    gotoHomePage()

                                    //Toast.makeText(mainActivity,"Removed from playlist",Toast.LENGTH_SHORT).show()
                                }

                            }


                        } else {

                            //Toast.makeText(mainActivity,"Removed from playlist",Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    //  hideProgressDialog()
                }

                override fun onFailure(call: Call<VersionCheckResponse?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(this, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }

    }

    private fun gotoHomePage(){
        Handler(Looper.getMainLooper()).postDelayed({

            if (prefManager!!.isFirstTimeLaunch) {
                val intent = Intent(this@Splash, Welcome::class.java)
                startActivity(intent)
                finish()
            } else {

                if (sessionManager!!.isLoggedIn) {
                    val intent = Intent(this@Splash, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this@Splash, Login::class.java)
                    startActivity(intent)
                    finish()
                }
            }


        }, (3000).toLong())
    }

    private fun gotoPlaystore(){
        val appPackageName = packageName // package name of the app

        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (anfe: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
    }

    /*private fun authenticateSpotify() {
        val builder: AuthorizationRequest.Builder =
            AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
        builder.setScopes(arrayOf<String>(SCOPES))
        val request: AuthorizationRequest = builder.build()
        //AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request)
    }*/
}