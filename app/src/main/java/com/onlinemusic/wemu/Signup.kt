package com.onlinemusic.wemu

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.billingclient.api.*
import com.onlinemusic.wemu.databinding.ActivitySignupBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.FavouritesResponseModel


import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.utils.Utilities
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.*

class Signup : AppCompatActivity() {

    lateinit var activitySignupBinding: ActivitySignupBinding
    val myCalendar = Calendar.getInstance()
    var radioButton: RadioButton? = null
    var isPasswordVisible = false
    var isCnfPasswordVisible = false

    var mSubscription: String? = ""

    lateinit var billingClient: BillingClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySignupBinding = DataBindingUtil.setContentView(this, R.layout.activity_signup)


        init()

    }

    @SuppressLint("ResourceAsColor")
    fun init(){



        val eventdate =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                myCalendar[Calendar.YEAR] = year
                myCalendar[Calendar.MONTH] = monthOfYear
                myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                eventdateupdateLabel()
            }

        activitySignupBinding.etDOB.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DATE, 0)
            val datePickerDialog = DatePickerDialog(
                this, eventdate, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        activitySignupBinding.pwdHideBtn.setOnClickListener {
            if (!isPasswordVisible) {
                activitySignupBinding.etPassword.transformationMethod = null
                activitySignupBinding.pwdHideBtn.setImageResource(R.drawable.eye_grey)
                isPasswordVisible = true

            } else {
                activitySignupBinding.etPassword.transformationMethod =
                    PasswordTransformationMethod()
                activitySignupBinding.pwdHideBtn.setImageResource(R.drawable.eye_white)
                isPasswordVisible = false
            }
            activitySignupBinding.etPassword.setSelection(activitySignupBinding.etPassword.length())
        }
        activitySignupBinding.cnfPwdHideBtn.setOnClickListener {
            if (!isCnfPasswordVisible) {
                activitySignupBinding.etConfirmPassword.transformationMethod = null
                activitySignupBinding.cnfPwdHideBtn.setImageResource(R.drawable.eye_grey)
                isCnfPasswordVisible = true

            } else {
                activitySignupBinding.etConfirmPassword.transformationMethod =
                    PasswordTransformationMethod()
                activitySignupBinding.cnfPwdHideBtn.setImageResource(R.drawable.eye_white)
                isCnfPasswordVisible = false
            }

            activitySignupBinding.etConfirmPassword.setSelection(activitySignupBinding.etConfirmPassword.length())
        }
        activitySignupBinding.llBasicsubscription.setOnClickListener {

            activitySignupBinding.llBasicsubscription.setBackgroundColor(Color.parseColor("#000000"))
            activitySignupBinding.llBuysubscription.setBackgroundColor(R.color.darker_gray)
            mSubscription = "0"
        }


        activitySignupBinding.llBuysubscription.setOnClickListener {

            activitySignupBinding.llBasicsubscription.setBackgroundColor(R.color.darker_gray)
            activitySignupBinding.llBuysubscription.setBackgroundColor(Color.parseColor("#000000"))

            mSubscription = "1"

        }

        activitySignupBinding.btnLogin.setOnClickListener {

            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
        //Handlle click event
        activitySignupBinding.tvTerms.movementMethod = LinkMovementMethod.getInstance();
        activitySignupBinding.tvTerms.isClickable = true;


        activitySignupBinding.tvTerms.text= if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml("By signing up you agree to our "+"<a href='https://www.termsandcondiitionssample.com/live.php?token=BMdX8FaXjTq9qDA3SofoJEeVqUdQPRmA'>Terms and Privacy Policy</a>", Html.FROM_HTML_MODE_LEGACY)
        }else{
            Html.fromHtml("By signing up you agree to our "+"<a href='https://www.termsandcondiitionssample.com/live.php?token=BMdX8FaXjTq9qDA3SofoJEeVqUdQPRmA'>Terms and Privacy Policy</a>")
        }
        /*activitySignupBinding.tvTerms.setOnClickListener {

            val intent = Intent(this, Webview::class.java)
            startActivity(intent)

        }*/


        activitySignupBinding.btnSignup.setOnClickListener {

            val selectedID = activitySignupBinding.radioGroup.getCheckedRadioButtonId()
            radioButton = findViewById(selectedID)

            if (activitySignupBinding.etName.text.length <= 5) {
                Toast.makeText(this, "The name must be at least 6 characters", Toast.LENGTH_SHORT).show()
            }
            else if (activitySignupBinding.etEmail.text.isEmpty()) {
                Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show()
            }
            else if(!Patterns.EMAIL_ADDRESS.matcher(activitySignupBinding.etEmail.text.toString().trim()).matches())
            {
                Toast.makeText(this, "Enter a valid Email address", Toast.LENGTH_SHORT).show()
            }
            else if (activitySignupBinding.etDOB.text.isEmpty()) {
                Toast.makeText(this, "Enter DOB", Toast.LENGTH_SHORT).show()
            }
            else if (selectedID==-1) {
                Toast.makeText(this, "Select Gender", Toast.LENGTH_SHORT).show()
            }
            else if (radioButton?.text?.length == 0) {
                Toast.makeText(this, "Select Gender", Toast.LENGTH_SHORT).show()
            }
            else if (activitySignupBinding.etUsername.text.isEmpty()) {
                Toast.makeText(this, "Enter Username", Toast.LENGTH_SHORT).show()
            }
            else if (activitySignupBinding.etPassword.text.isEmpty()) {
                Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show()
            }
            else if (activitySignupBinding.etConfirmPassword.text.isEmpty()) {
                Toast.makeText(this, "Enter Confirm Password.", Toast.LENGTH_SHORT).show()
            }
            else if (activitySignupBinding.etPassword.text.length <= 7) {
                Toast.makeText(this, "The password must be at least 8 characters.", Toast.LENGTH_SHORT).show()
            }
            else if (activitySignupBinding.etConfirmPassword.text.length <= 7) {
                Toast.makeText(this, "The confirm password must be at least 8 characters.", Toast.LENGTH_SHORT).show()
            }
            else if (activitySignupBinding.etPassword.text.toString() != activitySignupBinding.etConfirmPassword.text.toString()) {
                Toast.makeText(this, "Password doesn't Matched!", Toast.LENGTH_SHORT).show()
            }
            else if (!activitySignupBinding.cbAgreement.isChecked) {
                Toast.makeText(this, "Please Check agreement", Toast.LENGTH_SHORT).show()
            }
            else {

                if (mSubscription.equals("1")) {
                    signup()
                   // startTransaction()

                }else if (mSubscription.equals("")){
                    Toast.makeText(this, "Please select subscription type", Toast.LENGTH_SHORT).show()
                }else{
                    signup()
                }
            }
        }
    }

    private fun startTransaction(accessToken: String, message: String) {
        var productList = ArrayList<String>()
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
                                                var orderId = purchase.orderId
                                                var purchaseToken = purchase.purchaseToken
                                                var purchaseState = purchase.purchaseState
                                                checkPaymentStatus(orderId,purchaseToken,purchaseState,accessToken,message)
                                                //signup()
                                                //entitleUserProducts()
                                            }
                                            else -> {
                                                Log.e("BillingClient", "Failed to acknowledge purchase $billingResult")
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
                            billingClient.launchBillingFlow(this@Signup, billingFlowParams)
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

    private fun signup() {

        showProgressDialog()
        if (CheckConnectivity.getInstance(this).isOnline) {

        val retrofit: Retrofit = ApiClient.retrofitInstance!!
        val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)

        val name: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySignupBinding.etName.text.toString())
        val email: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySignupBinding.etEmail.text.toString())
        val password: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySignupBinding.etPassword.text.toString())
        val dob: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySignupBinding.etDOB.text.toString())
        val gender: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), radioButton?.getText()!!.toString())
        val username: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySignupBinding.etUsername.text.toString())
        val password_confirmation: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), activitySignupBinding.etConfirmPassword.text.toString())
        val subscription: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),"0" )
       // val subscription: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),mSubscription.toString() )

            val mcall: Call<ResponseBody?>? = uploadAPIs.signup(
                name, email, password, dob, gender,
                username, password_confirmation, subscription
            )
            mcall?.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: retrofit2.Response<ResponseBody?>,
                ) {
                    try {
                        if (response.isSuccessful && response.body() != null) {
                            val result = response.body()!!.string()
                            Log.v("responseannouncement-->", result)
                            val mjonsresponse = JSONObject(result)
                            val status = mjonsresponse.getBoolean("status")
                            val message = mjonsresponse.getString("message")
//                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            if (status) {

                                val jsonObject = mjonsresponse.getJSONObject("data")
                                val accessToken = jsonObject.getString("access_token")
                                /*val builder = AlertDialog.Builder(this@Signup)
                            builder.setMessage(message)
                            builder.setPositiveButton(
                                "Ok"
                            ) { dialog, which ->
                                val intent = Intent(this@Signup, OTPverification::class.java)
                                intent.putExtra("email", activitySignupBinding.etEmail.text.toString())
                                startActivity(intent)
                                finish()
                                dialog.cancel()

                            }
                            val alert = builder.create()
                            alert.show()*/
                                if (mSubscription.toString() == "1") {
                                    Utilities.alertDialogUtil(this@Signup,"Signup",message,
                                        isCancelable = false,
                                        isPositive = true,
                                        isNegetive = false,
                                        isNeutral = false,"Ok","","",object : Utilities.OnItemClickListener{
                                            override fun onItemClickAction(
                                                type: Int,
                                                dialogInterface: DialogInterface
                                            ) {
                                                if (type==1){
                                                    val intent = Intent(this@Signup, OTPverification::class.java)
                                                    intent.putExtra("email", activitySignupBinding.etEmail.text.toString())
                                                    intent.putExtra(
                                                        "accessToken",
                                                        accessToken.toString()
                                                    )
                                                    startActivity(intent)
                                                    dialogInterface.dismiss()
                                                    finish()
                                                }
                                            }

                                        })

                                  //  startTransaction(accessToken, message)
                                } else {
                                    Utilities.alertDialogUtil(this@Signup,
                                        "Signup",
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
                                                    val intent = Intent(
                                                        this@Signup,
                                                        OTPverification::class.java
                                                    )
                                                    intent.putExtra(
                                                        "email",
                                                        activitySignupBinding.etEmail.text.toString()
                                                    )
                                                    startActivity(intent)
                                                    dialogInterface.dismiss()
                                                    finish()
                                                }
                                            }

                                        })

                                }


                            } else {
                                Utilities.alertDialogUtil(this@Signup,
                                    "Signup",
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

                        }else {
                            Utilities.alertDialogUtil(this@Signup,
                                "Signup",
                                response.message(),
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
            hideProgressDialog()
            Toast.makeText(this, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
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
                                    Utilities.alertDialogUtil(this@Signup,"Signup",message,
                                        isCancelable = false,
                                        isPositive = true,
                                        isNegetive = false,
                                        isNeutral = false,"Ok","","",object : Utilities.OnItemClickListener{
                                            override fun onItemClickAction(
                                                type: Int,
                                                dialogInterface: DialogInterface
                                            ) {
                                                if (type==1){
                                                    val intent = Intent(this@Signup, OTPverification::class.java)
                                                    intent.putExtra("email", activitySignupBinding.etEmail.text.toString())
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

    private fun eventdateupdateLabel() {
        val myFormat = "dd-MM-yyyy" //In which you need put here
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        activitySignupBinding.etDOB.setText(sdf.format(myCalendar.time))

    }



    override fun onDestroy() {

        super.onDestroy()
    }


}