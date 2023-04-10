package com.onlinemusic.wemu

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.android.billingclient.api.*

import com.onlinemusic.wemu.databinding.ActivityPaymentBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.FavouritesResponseModel
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.utils.Utilities
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit


class Payment : AppCompatActivity() {
    lateinit var binding: ActivityPaymentBinding
    var sessionManager: SessionManager? = null

    private var isPaymentDone: Boolean = false
    lateinit var billingClient: BillingClient
    private var isOtpVerified=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_payment)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_payment)
        sessionManager = SessionManager(this)
        val intent = intent

            isOtpVerified = intent.getBooleanExtra("otpVerified",false)


        val subscribeStatus = sessionManager?.getSubscribed()
        binding.ivClose.visibility = View.VISIBLE
        if (isOtpVerified){
            binding.llPurchase.visibility = View.VISIBLE
            binding.freePlanSelect.visibility = View.VISIBLE
            binding.subscriptionPlanSelect.visibility = View.GONE

            binding.llPurchase.isClickable = true
            binding.relativeFree.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.yellowgreyborder
                )
            )
            binding.payedPlanLl.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_rectangle3
                )
            )
        }
        if (subscribeStatus == true) {
            binding.freePlanSelect.visibility = View.GONE
            binding.freePlanLl.isClickable = false
            binding.subscriptionPlanSelect.visibility = View.VISIBLE
            binding.llPurchase.visibility = View.GONE
            binding.llPurchase.isEnabled = false
            binding.freePlanLl.isEnabled = false
            binding.payedPlanLl.isEnabled = false
            binding.llPurchase.isClickable = false

            binding.relativeFree.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_rectangle4
                )
            )
            binding.payedPlanLl.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.blueyellowborder
                )
            )

        } else {
            binding.llPurchase.visibility = View.VISIBLE
            binding.freePlanSelect.visibility = View.VISIBLE
            binding.subscriptionPlanSelect.visibility = View.GONE

            binding.llPurchase.isClickable = true
            binding.relativeFree.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.yellowgreyborder
                )
            )
            binding.payedPlanLl.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_rectangle3
                )
            )

        }

        binding.ivClose.setOnClickListener {
            if (isOtpVerified){
                Utilities.alertDialogUtil(this@Payment,"Subscription","Do you want to skip the subscription now?",
                    isCancelable = false,
                    isPositive = true,
                    isNegetive = true,
                    isNeutral = false,"Ok","No","",object : Utilities.OnItemClickListener{
                        override fun onItemClickAction(
                            type: Int,
                            dialogInterface: DialogInterface
                        ) {
                            if (type==1){
                                /* val intent = Intent(this@OTPverification, OTPverification::class.java)
                                // intent.putExtra("email", activitySignupBinding.etEmail.text.toString())
                                 startActivity(intent)*/

                                val intent = Intent(this@Payment, Login::class.java)
                                startActivity(intent)

                                dialogInterface.dismiss()
                                finish()
                            }else if (type==2){
                                dialogInterface.dismiss()
                            }
                        }

                    })

            }else
            onBackPressedDispatcher.onBackPressed()
        }
        binding.freePlanLl.setOnClickListener {

            binding.freePlanSelect.visibility = View.VISIBLE
            binding.subscriptionPlanSelect.visibility = View.GONE
            binding.llPurchase.isClickable = false
            binding.relativeFree.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.yellowgreyborder
                )
            )
            binding.payedPlanLl.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_rectangle3
                )
            )
            binding.llPurchase.visibility = View.VISIBLE


            // binding.freePlanSelect.visibility = View.VISIBLE
            // binding.proPlanSelect.visibility = View.GONE
        }

        binding.payedPlanLl.setOnClickListener {
            if(subscribeStatus == false) {
                binding.llPurchase.visibility = View.VISIBLE
                binding.llPurchase.isClickable = true
                binding.freePlanSelect.visibility = View.GONE
                binding.subscriptionPlanSelect.visibility = View.VISIBLE
                binding.relativeFree.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_rectangle4
                    )
                )
                binding.payedPlanLl.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.blueyellowborder
                    )
                )
            }
            else
            {

            }
            // binding.freePlanSelect.visibility = View.GONE
            //   binding.proPlanSelect.visibility = View.VISIBLE
        }
        binding.llPurchase.setOnClickListener {
          //  Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
            startInAppTransaction()
        }


    }

    private fun startInAppTransaction() {
        var productList = ArrayList<String>()
       productList.add("1wemu_subscription")
       // productList.add("android.test.purchased")
        val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    Log.d("fvhgvhgv", purchase.purchaseToken)
                    purchases?.apply {
                        //processPurchases(purchase)
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            Log.d("purchase", Purchase.PurchaseState.PURCHASED.toString())

                            // Implement server verification
                            // If purchase token is OK, then unlock user access to the content
                            //acknowledgePurchase(purchase)
                            if (!purchase.isAcknowledged) {
                                //acknowledge(purchase.purchaseToken)
                                val acknowledgePurchaseParams =
                                    AcknowledgePurchaseParams.newBuilder()
                                        .setPurchaseToken(purchase.purchaseToken)
                                        .build()
                                val acknowledgePurchaseResponseListener =
                                    AcknowledgePurchaseResponseListener {
                                        //getMessage("Purchase acknowledged")
                                        Log.d("gfhgvv", "success")
                                        when (billingResult.responseCode) {
                                            BillingClient.BillingResponseCode.OK -> {
                                                Log.d(
                                                    "gfhgvv",
                                                    BillingClient.BillingResponseCode.OK.toString()
                                                )
                                                val orderId = purchase.orderId
                                                val purchaseToken = purchase.purchaseToken
                                                val purchaseState = purchase.purchaseState


                                                checkPaymentStatus(orderId,purchaseToken,purchaseState)

                                                //entitleUserProducts()
                                            }
                                            else -> {
                                                Log.e(
                                                    "BillingClient",
                                                    "Failed to acknowledge purchase $billingResult"
                                                )
                                            }
                                        }
                                    }
                                billingClient.acknowledgePurchase(
                                    acknowledgePurchaseParams,
                                    acknowledgePurchaseResponseListener
                                );

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
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases()
            .setListener(purchasesUpdatedListener).build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {

                Log.e("SERVICE DISCONNECTED","DISCONNECTED SERVICE GOOGLE SUBS")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val params = SkuDetailsParams.newBuilder()
                    params.setSkusList(productList).setType(BillingClient.SkuType.SUBS)
                    billingClient.querySkuDetailsAsync(params.build())
                    { billingResult, arrList ->
                        for (skuDetails in arrList!!) {
                            val billingFlowParams =
                                BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build()
                            // val resPonsecode = billingClient.launchBillingFlow(this@SubscriptionPaymentActivity,billingFlowParams)
                            //Log.d("resPonsecode",resPonsecode.toString())
                            billingClient.launchBillingFlow(this@Payment, billingFlowParams)
                                .takeIf { billingResult ->
                                    billingResult.responseCode != BillingClient.BillingResponseCode.OK
                                }
                                ?.let { billingResult ->
                                    Log.e(
                                        "BillingClient",
                                        "Failed to launch billing flow $billingResult"
                                    )
                                }
                        }
                    }
                }

            }

        })
    }

    private fun checkPaymentStatus(orderId: String, purchaseToken: String, purchaseState: Int) {
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
                    "Bearer " + sessionManager?.getToken(), amount, type, via,
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

                                var status = result.status
                                if(status == true)
                                {
                                   // Toast.makeText(this@MainActivity,"Song has been added from favourites",Toast.LENGTH_SHORT).show()

                                    //isFavourate = true
                                    Utilities.alertDialogUtil(this@Payment,"Login","Subscription purchase successfull",
                                        isCancelable = false,
                                        isPositive = true,
                                        isNegetive = false,
                                        isNeutral = false,"Ok","","",object :Utilities.OnItemClickListener{
                                            override fun onItemClickAction(
                                                type: Int,
                                                dialogInterface: DialogInterface
                                            ) {
                                                if (type==1){

                                                    sessionManager?.setSubscribed(true)
                                                    dialogInterface.dismiss()
                                                    binding.llPurchase.visibility = View.VISIBLE
                                                    binding.llPurchase.isEnabled = false
                                                    binding.freePlanLl.isEnabled = false
                                                    binding.payedPlanLl.isEnabled = false
                                                    binding.llPurchase.isClickable = false

                                                    binding.relativeFree.setBackgroundDrawable(
                                                        ContextCompat.getDrawable(
                                                            this@Payment,
                                                            R.drawable.ic_rectangle4
                                                        )
                                                    )
                                                    binding.payedPlanLl.setBackgroundDrawable(
                                                        ContextCompat.getDrawable(
                                                            this@Payment,
                                                            R.drawable.blueyellowborder
                                                        )
                                                    )
                                                    if (isOtpVerified){
                                                        val intent = Intent(this@Payment, Login::class.java)
                                                        startActivity(intent)
                                                    }
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
                    hideProgressDialog()
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


}