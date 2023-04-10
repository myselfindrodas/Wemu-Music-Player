package com.onlinemusic.wemu

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.billingclient.api.*

import com.onlinemusic.wemu.databinding.ActivityOtpverificationBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.FavouritesResponseModel
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

class OTPverification : AppCompatActivity() {

    lateinit var activityOtpverificationBinding: ActivityOtpverificationBinding
    var otp: String? = ""
    var email: String? = ""
    var forgotpasswordemail: String? = ""
    var forgotpassword: String? = ""
    var accessToken: String? = ""
    lateinit var billingClient: BillingClient
    var sessionManager: SessionManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityOtpverificationBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_otpverification)
        sessionManager = SessionManager(this)

        val intent = intent
        if (intent.getStringExtra("forgotpasswordemail") == null) {
            forgotpasswordemail = ""
        } else {
            forgotpasswordemail = intent.getStringExtra("forgotpasswordemail")
        }

        if (intent.getStringExtra("email") == null) {
            email = ""
        } else {
            email = intent.getStringExtra("email")
        }
        if (intent.getStringExtra("accessToken") == null) {
            accessToken = ""
        } else {
            accessToken = intent.getStringExtra("accessToken")
        }

        if (intent.getStringExtra("forgotpassword") == null) {
            forgotpassword = ""
        } else {
            forgotpassword = intent.getStringExtra("forgotpassword")

        }
        init()

    }

    fun init() {

        val otptext = ArrayList<EditText>()
        otptext.add(activityOtpverificationBinding.otp1)
        otptext.add(activityOtpverificationBinding.otp2)
        otptext.add(activityOtpverificationBinding.otp3)
        otptext.add(activityOtpverificationBinding.otp4)
        setOtpEditTextHandler(otptext)

        if (forgotpassword?.length!! > 0) {
            activityOtpverificationBinding.tvotpEmail.text =
                "Please enter the number code send your " + forgotpasswordemail
        } else {
            activityOtpverificationBinding.tvotpEmail.text =
                "Please enter the number code send your " + email
        }


        activityOtpverificationBinding.btnSubmit.setOnClickListener {

            otp = activityOtpverificationBinding.otp1.text.toString() +
                    activityOtpverificationBinding.otp2.text.toString() +
                    activityOtpverificationBinding.otp3.text.toString() +
                    activityOtpverificationBinding.otp4.text.toString()

            if (otp?.length!! > 3) {

                if (forgotpassword?.length!! > 0) {
                    OTPverfication2()
                } else {
                    OTPverfication()
                }
            } else {
                Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show()
            }


        }


        activityOtpverificationBinding.btnResendOtp.setOnClickListener {

            if (forgotpassword?.length!!>0){
                ResendOTP2()
            }else{
                ResendOTP()
            }
        }
    }


    private fun setOtpEditTextHandler(otpEt: ArrayList<EditText>) { //This is the function to be called
        for (i in 0..3) { //Its designed for 6 digit OTP
            otpEt.get(i).addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    if (i == 3 && otpEt[i].text.toString().isNotEmpty()) {

                    } else if (otpEt[i].text.toString().isNotEmpty()) {
                        otpEt[i + 1]
                            .requestFocus()
                    }
                }
            })
            otpEt[i].setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (event.action != KeyEvent.ACTION_DOWN) {
                    return@OnKeyListener false
                }
                if (keyCode == KeyEvent.KEYCODE_DEL &&
                    otpEt[i].text.toString().isEmpty() && i != 0
                ) {
                    otpEt[i - 1].setText("")
                    otpEt[i - 1].requestFocus()
                }
                false
            })
        }
    }


    private fun OTPverfication() {
        if (CheckConnectivity.getInstance(applicationContext).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val email: RequestBody =
                RequestBody.create("text/plain".toMediaTypeOrNull(), email.toString())
            val otp: RequestBody =
                RequestBody.create("text/plain".toMediaTypeOrNull(), otp.toString())
            val mcall: Call<ResponseBody?>? = uploadAPIs.verifyotp(email, otp)
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
                            var message = mjonsresponse.getString("message")
                            Toast.makeText(this@OTPverification, message, Toast.LENGTH_SHORT).show()
                            if (status) {

                                if (accessToken.equals("")){
                                    val intent = Intent(this@OTPverification, Login::class.java)
                                    startActivity(intent)
                                    finish()
                                }else{

                                    sessionManager?.setToken(accessToken)
                                    val intent = Intent(this@OTPverification, Payment::class.java)
                                    intent.putExtra("otpVerified", true)
                                    startActivity(intent)
                                }
                                   // startTransaction(accessToken!!,message)

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


    private fun OTPverfication2() {

        if (CheckConnectivity.getInstance(this).isOnline) {

            showProgressDialog()

        val retrofit: Retrofit = ApiClient.retrofitInstance!!
        val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
        val email: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), forgotpasswordemail.toString())
        val otp: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), otp.toString())
            val mcall: Call<ResponseBody?>? = uploadAPIs.verifyotp(email, otp)
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
                            var message = mjonsresponse.getString("message")
                            Toast.makeText(this@OTPverification, message, Toast.LENGTH_SHORT).show()
                            if (status) {
                                val intent = Intent(this@OTPverification, Resetpassword::class.java)
                                intent.putExtra("forgotpasswordemail", forgotpasswordemail)
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
        }else {
            Toast.makeText(this, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }

    }


    private fun ResendOTP() {


        if (CheckConnectivity.getInstance(applicationContext).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val email: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), email.toString())
            val mcall: Call<ResponseBody?>? = uploadAPIs.resendotp(email)
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
                            Toast.makeText(this@OTPverification, message, Toast.LENGTH_SHORT).show()

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

    private fun ResendOTP2() {

        if (CheckConnectivity.getInstance(applicationContext).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val email: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), forgotpasswordemail.toString())
            val mcall: Call<ResponseBody?>? = uploadAPIs.resendotp(email)
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
                            Toast.makeText(this@OTPverification, message, Toast.LENGTH_SHORT).show()

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


    private fun startTransaction(accessToken: String, message: String) {
        var productList = java.util.ArrayList<String>()
        productList.add("1wemu_subscription")
        val purchasesUpdatedListener = PurchasesUpdatedListener{
                billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    Log.d("fvhgvhgv",purchase.purchaseToken)
                    purchases?.apply {
                        //processPurchases(purchase)
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            Log.d("purchase", Purchase.PurchaseState.PURCHASED.toString())

                            // Implement server verification
                            // If purchase token is OK, then unlock user access to the content
                            //acknowledgePurchase(purchase)
                            if(!purchase.isAcknowledged) {
                                //acknowledge(purchase.purchaseToken)
                                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.purchaseToken)
                                    .build()
                                val acknowledgePurchaseResponseListener =
                                    AcknowledgePurchaseResponseListener {
                                        //getMessage("Purchase acknowledged")
                                        Log.d("gfhgvv","success")
                                        when (billingResult.responseCode) {
                                            BillingClient.BillingResponseCode.OK -> {
                                                Log.d("gfhgvv", BillingClient.BillingResponseCode.OK.toString())
                                                val orderId = purchase.orderId
                                                val purchaseToken = purchase.purchaseToken
                                                val purchaseState = purchase.purchaseState
                                                checkPaymentStatus(orderId,purchaseToken,purchaseState,accessToken,message)
                                                //signup()
                                                //entitleUserProducts()
                                            }
                                            else -> {
                                                Log.e("BillingClient", "Failed to acknowledge purchase $billingResult")

                                                Utilities.alertDialogUtil(this@OTPverification,"Subscription","Your subscription is canceled",
                                                    isCancelable = false,
                                                    isPositive = true,
                                                    isNegetive = false,
                                                    isNeutral = false,"Ok","","",object : Utilities.OnItemClickListener{
                                                        override fun onItemClickAction(
                                                            type: Int,
                                                            dialogInterface: DialogInterface
                                                        ) {
                                                            if (type==1){
                                                                /* val intent = Intent(this@OTPverification, OTPverification::class.java)
                                                                // intent.putExtra("email", activitySignupBinding.etEmail.text.toString())
                                                                 startActivity(intent)*/

                                                                val intent = Intent(this@OTPverification, Login::class.java)
                                                                startActivity(intent)

                                                                dialogInterface.dismiss()
                                                                finish()
                                                            }
                                                        }

                                                    })

                                            }
                                        }
                                    }
                                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);

                                /*   billingClient.acknowledgePurchase(acknowledgePurchaseParams) {
                                       //Give thanks for the purchase
                                       Log.d("hgjh",)
                                       Toast.makeText(this@SubscriptionPaymentActivity,"success",Toast.LENGTH_SHORT).show()
                                   }*/
                            }
                        }
                    }
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                // Handle any other error codes.
            }

        }
        billingClient = BillingClient.newBuilder(this).
        enablePendingPurchases().
        setListener(purchasesUpdatedListener).build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {

            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
                {
                    var params = SkuDetailsParams.newBuilder()
                    params.setSkusList(productList).setType(BillingClient.SkuType.SUBS)
                    billingClient.querySkuDetailsAsync(params.build())
                    {
                            billingResult,arrList->
                        for(skuDetails in arrList!!) {
                            val billingFlowParams =
                                BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build()
                            // val resPonsecode = billingClient.launchBillingFlow(this@SubscriptionPaymentActivity,billingFlowParams)
                            //Log.d("resPonsecode",resPonsecode.toString())
                            billingClient.launchBillingFlow(this@OTPverification, billingFlowParams)
                                .takeIf { billingResult ->
                                    billingResult.responseCode != BillingClient.BillingResponseCode.OK }
                                ?.let { billingResult ->
                                    Log.e("BillingClient", "Failed to launch billing flow $billingResult")
                                }
                        }
                    }
                }

            }

        })
    }


    private fun checkPaymentStatus(
        orderId: String,
        purchaseToken: String,
        purchaseState: Int,
        accessToken: String,
        message: String
    ) {
        if (CheckConnectivity.getInstance(applicationContext).isOnline) {
            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val amount: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), "7")
            val type: RequestBody = RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                "PRO"
            )
            val via: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), "INAPP")
            val stripe_customer_id: RequestBody = RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                orderId
            )
            val customer_response: RequestBody = RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                purchaseState.toString()
            )
            val stripe_subscription_id: RequestBody = RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                purchaseToken
            )

            val mcall: Call<FavouritesResponseModel?>? =
                uploadAPIs.updateTransaction(
                    "Bearer " + accessToken, amount, type, via,
                    stripe_customer_id, customer_response, stripe_subscription_id
                )
            mcall?.enqueue(object : Callback<FavouritesResponseModel?> {
                override fun onResponse(
                    call: Call<FavouritesResponseModel?>,
                    response: retrofit2.Response<FavouritesResponseModel?>,
                ) {
                    try {
                        if (response.body() != null) {

                            val result = response.body()!!
                            /* Log.v("responseannouncement-->", result)
                             val mjonsresponse = JSONObject(result)
                             val data = mjonsresponse.getJSONObject("data")*/
                            result.apply {

                                val status = result.status
                                if(status == true)
                                {
                                    // Toast.makeText(this@MainActivity,"Song has been added from favourites",Toast.LENGTH_SHORT).show()

                                    //isFavourate = true
                                    Utilities.alertDialogUtil(this@OTPverification,"Signup",message,
                                        isCancelable = false,
                                        isPositive = true,
                                        isNegetive = false,
                                        isNeutral = false,"Ok","","",object : Utilities.OnItemClickListener{
                                            override fun onItemClickAction(
                                                type: Int,
                                                dialogInterface: DialogInterface
                                            ) {
                                                if (type==1){
                                                   /* val intent = Intent(this@OTPverification, OTPverification::class.java)
                                                   // intent.putExtra("email", activitySignupBinding.etEmail.text.toString())
                                                    startActivity(intent)*/

                                                    val intent = Intent(this@OTPverification, Login::class.java)
                                                    startActivity(intent)

                                                    dialogInterface.dismiss()
                                                    finish()
                                                }
                                            }

                                        })

                                }
                                else
                                {
                                    //Toast.makeText(this@MainActivity,"Song has been removed from favourites",Toast.LENGTH_SHORT).show()
                                    //ivFavourate.setImageResource(R.drawable.ic_heart)
                                }


                            }






                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }

                }

                override fun onFailure(call: Call<FavouritesResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        }
        else{
            Toast.makeText(this, "In", Toast.LENGTH_SHORT).show()
        }
    }

    var mProgressDialog: ProgressDialog? = null

    @SuppressLint("SuspiciousIndentation")
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