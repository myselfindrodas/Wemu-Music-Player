package com.onlinemusic.wemu

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.*
import android.graphics.Point
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.text.format.DateUtils
import android.util.Log
import android.view.*
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.android.billingclient.api.*
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.onlinemusic.wemu.adapter.PlayListCreatedAdapter
import com.onlinemusic.wemu.adapter.SongDetailsAdapter
import com.onlinemusic.wemu.databinding.ActivityMainBinding
import com.onlinemusic.wemu.databinding.DialogAddToPlaylistBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.musicplayerdemo.MusicService
import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.onlinemusic.wemu.responseModel.FavouritesResponseModel
import com.onlinemusic.wemu.responseModel.add_to_playlist.AddToPlaylistRequestModel
import com.onlinemusic.wemu.responseModel.playlistitems.response.DataX
import com.onlinemusic.wemu.responseModel.playlistitems.response.PlaylistCreatedResponseModel
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.utils.Coroutines
import com.onlinemusic.wemu.utils.Utilities
import com.onlinemusic.wemu.viewmodel.PlayerViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity(), ServiceConnection,
    SongDetailsAdapter.OnItemClickListener {

    private var isPaymentDone: Boolean = false
    lateinit var mBottomNavigationView: BottomNavigationView
    private lateinit var mNavController: NavController
    var sessionManager: SessionManager? = null
    private lateinit var playerViewModel: PlayerViewModel
    lateinit var billingClient: BillingClient
    var mLayoutManager: RecyclerView.LayoutManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        playerViewModel = ViewModelProvider(this)[PlayerViewModel::class.java]
        mBottomNavigationView = binding.bottomNavigationView


        sessionManager = SessionManager(this)

        // notifyBroadcast = NotificationReceiver()


        mMainActivity = this
        with(binding) {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.flFragmentOne) as NavHostFragment
            mNavController = navHostFragment.navController

            // mNavController = findNavController(R.id.flFragment)
            mNavController.navigate(R.id.nav_home)

            if (sessionManager?.getTheme().equals("night")) {
                playerLayout.llSongList.setBackgroundColor(
                    resources.getColor(
                        R.color.black,
                        resources.newTheme()
                    )
                )
            } else {
                playerLayout.llSongList.setBackgroundColor(
                    resources.getColor(
                        R.color.orange,
                        resources.newTheme()
                    )
                )
            }
            changeTheme()
//        setCurrentFragment(homefragment)


            //  bottomNavigationView.setupWithNavController(mNavController)

            bottomNavigationView.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.home -> mNavController.navigate(R.id.nav_home)
                    R.id.genres -> mNavController.navigate(R.id.nav_genres)
                    R.id.library -> mNavController.navigate(R.id.nav_library)
                    R.id.profile -> mNavController.navigate(R.id.nav_profile)

                }
                return@setOnItemSelectedListener true
            }
            /* bottomNavigationView?.setOnNavigationItemSelectedListener {
                 when(it.itemId){
                     R.id.home-> mNavController?.navigate(R.id.nav_home)
                     R.id.genres->mNavController?.navigate(R.id.nav_genres)
                     R.id.library->mNavController?.navigate(R.id.nav_library)
                     R.id.profile->mNavController?.navigate(R.id.nav_profile)

                 }
                 true
             }*/


            playerLayout.root.visibility = View.GONE

            with(paymentLayout) {
                val subscribeStatus = sessionManager?.getSubscribed()
                if (subscribeStatus == true) {
                    llPurchase.visibility = View.VISIBLE
                    freePlanSelect.visibility = View.GONE
                    subscriptionPlanSelect.visibility = View.VISIBLE
                    // llPurchaseFree.visibility = View.GONE
                    llPurchase.isEnabled = false
                    relativeFree.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.ic_rectangle4
                        ),
                    )
                    payedPlanLl.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.blueyellowborder
                        )
                    )
                    freePlanLl.isEnabled = false
                    payedPlanLl.isEnabled = false
                } else {
                    llPurchase.visibility = View.VISIBLE
                    freePlanSelect.visibility = View.VISIBLE
                    subscriptionPlanSelect.visibility = View.GONE
                    llPurchase.isClickable = true
                    //llPurchaseFree.visibility = View.VISIBLE
                    relativeFree.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.yellowgreyborder
                        )
                    )
                    payedPlanLl.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.ic_rectangle3
                        )
                    )

                }
                ivClose.setOnClickListener {
                    // Toast.makeText(this@MainActivity, "clicked", Toast.LENGTH_SHORT).show()
                    root.visibility = View.GONE
                }

                freePlanLl.setOnClickListener {
                    llPurchase.visibility = View.VISIBLE
                    llPurchase.isClickable = true
                    relativeFree.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.yellowgreyborder
                        )
                    )
                    payedPlanLl.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.ic_rectangle3
                        )
                    )

                    // binding.freePlanSelect.visibility = View.VISIBLE
                    // binding.proPlanSelect.visibility = View.GONE
                }

                payedPlanLl.setOnClickListener {
                    llPurchase.visibility = View.VISIBLE
                    llPurchase.isClickable = true
                    relativeFree.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.ic_rectangle4
                        )
                    )
                    payedPlanLl.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.blueyellowborder
                        )
                    )
                    // binding.freePlanSelect.visibility = View.GONE
                    //   binding.proPlanSelect.visibility = View.VISIBLE
                }
                llPurchase.setOnClickListener {
                    val productList = ArrayList<String>()
                    productList.add("1wemu_subscription")
                    val purchasesUpdatedListener =
                        PurchasesUpdatedListener { billingResult, purchases ->
                            if (billingResult.responseCode == com.android.billingclient.api.BillingClient.BillingResponseCode.OK && purchases != null) {
                                for (purchase in purchases) {
                                    android.util.Log.d("fvhgvhgv", purchase.purchaseToken)
                                    purchases?.apply {
                                        //processPurchases(purchase)
                                        if (purchase.purchaseState == com.android.billingclient.api.Purchase.PurchaseState.PURCHASED) {
                                            android.util.Log.d(
                                                "purchase",
                                                com.android.billingclient.api.Purchase.PurchaseState.PURCHASED.toString()
                                            )

                                            // Implement server verification
                                            // If purchase token is OK, then unlock user access to the content
                                            //acknowledgePurchase(purchase)
                                            if (!purchase.isAcknowledged) {
                                                //acknowledge(purchase.purchaseToken)
                                                val acknowledgePurchaseParams =
                                                    com.android.billingclient.api.AcknowledgePurchaseParams.newBuilder()
                                                        .setPurchaseToken(purchase.purchaseToken)
                                                        .build()
                                                val acknowledgePurchaseResponseListener =
                                                    AcknowledgePurchaseResponseListener {
                                                        //getMessage("Purchase acknowledged")
                                                        android.util.Log.d("gfhgvv", "success")
                                                        when (billingResult.responseCode) {
                                                            com.android.billingclient.api.BillingClient.BillingResponseCode.OK -> {
                                                                android.util.Log.d(
                                                                    "gfhgvv",
                                                                    com.android.billingclient.api.BillingClient.BillingResponseCode.OK.toString()
                                                                )

                                                                val orderId = purchase.orderId
                                                                val purchaseToken =
                                                                    purchase.purchaseToken
                                                                val purchaseState =
                                                                    purchase.purchaseState
                                                                checkPaymentStatus(
                                                                    orderId,
                                                                    purchaseToken,
                                                                    purchaseState
                                                                )
                                                                //entitleUserProducts()
                                                            }
                                                            else -> {
                                                                android.util.Log.e(
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
                            } else if (billingResult.responseCode == com.android.billingclient.api.BillingClient.BillingResponseCode.USER_CANCELED) {
                                // Handle an error caused by a user cancelling the purchase flow.
                            } else {
                                // Handle any other error codes.
                            }

                        }
                    billingClient =
                        com.android.billingclient.api.BillingClient.newBuilder(this@MainActivity)
                            .enablePendingPurchases().setListener(purchasesUpdatedListener).build()
                    billingClient.startConnection(object : BillingClientStateListener {
                        override fun onBillingServiceDisconnected() {

                        }

                        @SuppressLint("LogNotTimber")
                        override fun onBillingSetupFinished(billingResult: BillingResult) {
                            if (billingResult.responseCode == com.android.billingclient.api.BillingClient.BillingResponseCode.OK) {
                                val params =
                                    com.android.billingclient.api.SkuDetailsParams.newBuilder()
                                params.setSkusList(productList)
                                    .setType(com.android.billingclient.api.BillingClient.SkuType.SUBS)
                                billingClient.querySkuDetailsAsync(params.build())
                                { billingResult, arrList ->
                                    for (skuDetails in arrList!!) {
                                        val billingFlowParams =
                                            com.android.billingclient.api.BillingFlowParams.newBuilder()
                                                .setSkuDetails(skuDetails).build()
                                        // val resPonsecode = billingClient.launchBillingFlow(this@SubscriptionPaymentActivity,billingFlowParams)
                                        //Log.d("resPonsecode",resPonsecode.toString())
                                        billingClient.launchBillingFlow(
                                            this@MainActivity,
                                            billingFlowParams
                                        )
                                            .takeIf { billingResult ->
                                                billingResult.responseCode != com.android.billingclient.api.BillingClient.BillingResponseCode.OK
                                            }
                                            ?.let { billingResult ->
                                                android.util.Log.e(
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
            }

            /*playerViewModel.startSongModel.observeForever(Observer {
                it.let { playedModel->


                        if (!binding.playerLayout.root.isVisible){
                            binding.playerLayout.root.visibility=View.VISIBLE
                        }
                        if (playedModel.isPlaying==1) {
                            nextTrackCount = 0

                                showSong(
                                    playedModel.playerModel[playedModel.position],
                                    playedModel.playerModel,
                                    playedModel.position,
                                    playedModel.playerModel.size-1
                                )

                        }
                    else if(playedModel.isPlaying == 2)
                        {
                            if (!binding.playerLayout.root.isVisible) {
                                binding.playerLayout.root.visibility = View.GONE
                                stopPlaying()
                            }
                        }
                    else
                        {

                        }

                }
            })*/


        }


        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }*/
        startService(intent)
        setupSongListRecyclerView()

        playerViewModel.getCurrentSongId.observe(this, Observer {
            songDetailsAdapter.notifyItemRangeChanged(0, songDetailsAdapter.itemCount)
        })
        playerViewModel.startSongModel1.observeForever(Observer {
            it.let { playedModel ->
                if (!binding.playerLayout.root.isVisible) {
                    binding.playerLayout.root.visibility = View.VISIBLE
                }

                if (playedModel.isPlaying == 1) {

                    nextTrackCount = 0
                    arrayList.clear()
                    // println("ARRAY SIZE INSIDE:   ${modelDataList.size}     POSITION:   $mPosition")
                    /*playedModel.playerModel.forEach { forEach ->
                        if (!arrayList.contains(forEach)) {
                            arrayList.add(forEach)
                        }

                    }*/
                    arrayList.addAll(playedModel.playerModel)
                    showSong2(
                        arrayList[playedModel.position],
                        arrayList,
                        playedModel.position,
                        arrayList.size - 1
                    )
                } else if (playedModel.isPlaying == 2) {

                    if (binding.playerLayout.root.isVisible) {
                        binding.playerLayout.root.visibility = View.GONE
                        stopPlaying()
                    }
                } else {

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
                                if (status == true) {
                                    // Toast.makeText(this@MainActivity,"Song has been added from favourites",Toast.LENGTH_SHORT).show()

                                    //isFavourate = true
                                    Utilities.alertDialogUtil(this@MainActivity,
                                        "Login",
                                        "Subscription purchase successfull",
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
                                                    sessionManager?.setSubscribed(true)
                                                    binding.paymentLayout.root.visibility =
                                                        View.GONE
                                                    showSong2(
                                                        modelDataPayment!!,
                                                        modelDataListPayment,
                                                        mPositionPayment,
                                                        totalLengthPayment,
                                                        trackChangeFromPlayerPayment
                                                    )
                                                    dialogInterface.dismiss()

                                                }
                                            }

                                        })
                                } else {
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
        } else {
            Toast.makeText(this, "In", Toast.LENGTH_SHORT).show()
        }
    }

//OpenPopup

    fun changeTheme() {
        if (sessionManager?.getTheme().equals("night")) {
            binding.clMainLayout.setBackgroundColor(
                resources.getColor(
                    R.color.black,
                    resources.newTheme()
                )
            )
        } else {
            binding.clMainLayout.setBackgroundColor(
                resources.getColor(
                    R.color.orange,
                    resources.newTheme()
                )
            )
        }
    }

    private fun shareAppLink(subject: String, message: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            var shareMessage = "\nLet me recommend you this application\n\n"
            //  shareMessage = "$shareMessage ${message}\n\n"
            shareMessage =
                """
                ${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
                
                """.trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "Choose app"))
        } catch (e: java.lang.Exception) {
            //e.toString();
        }
    }

    override fun onBackPressed() {

        if (binding.playerLayout.rlFullScreenActivity.isVisible) {

            val paddingSDP = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._70sdp)
            if (songId > 0)
                binding.rlFragment.setPadding(0, 0, 0, paddingSDP)
            binding.playerLayout.rlBottomActivity.visibility = View.VISIBLE
            binding.playerLayout.rlFullScreenActivity.visibility = View.GONE
        } else if (binding.paymentLayout.root.isVisible) {
            binding.paymentLayout.root.visibility = View.GONE
        } else
            super.onBackPressed()
    }

    var nextTrackCount = 0
    var isPlayLoop = false
    var isPlayRand = false


    var modelDataPayment: CommonDataModel1? = null
    var modelDataListPayment: List<CommonDataModel1> = ArrayList<CommonDataModel1>()
    var mPositionPayment: Int = 0
    var totalLengthPayment: Int = 0
    var trackChangeFromPlayerPayment: Boolean = false


    companion object {
        const val RECEIVER_MESSAGE: String = "RECEIVER_MESSAGE"
        const val RECEIVER_INTENT: String = "RECEIVER_INTENT"
        var name = ""
        var songUrl = ""
        var isPlaying = false
        var musicService: MusicService? = null
        var songIcon: String? = ""

        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityMainBinding
        var mModelData: CommonDataModel1? = null
        var mediaresult = 0

        @SuppressLint("StaticFieldLeak")
        var mMainActivity: MainActivity? = null
        fun MainActivityInstance(): MainActivity {
            if (mMainActivity == null) {
                mMainActivity = MainActivity()
            }
            return mMainActivity!!
        }

        fun onLogout() {

            musicService!!.stopPlayerService()
        }

        fun nextSong(mainActivity: MainActivity, isNext: Boolean) {
            if (isNext) {
                if (itemPosition == arrayList.size - 1)
                    itemPosition = 0
                else
                    itemPosition++
            } else {
                if (itemPosition == 0) {
                    itemPosition = 0
                } else
                    itemPosition--
            }
            println("ARRAY SIZE:   ${arrayList.size}     POSITION:   $itemPosition")
            mainActivity.showSong2(
                arrayList[itemPosition],
                arrayList,
                itemPosition,
                arrayList.size - 1,
                true
            )
        }

        var songId: Int = 0

        @SuppressLint("StaticFieldLeak")
        lateinit var songName: TextView

        @SuppressLint("StaticFieldLeak")
        lateinit var previousBtn: ImageView

        @SuppressLint("StaticFieldLeak")
        lateinit var playBtn: ImageView

        @SuppressLint("StaticFieldLeak")
        lateinit var playBtnShort: ImageView

        @SuppressLint("StaticFieldLeak")
        lateinit var nextBtn: ImageView

        @SuppressLint("StaticFieldLeak")
        lateinit var seekBar: SeekBar

        var x = 0
        var itemPosition = 0
        var arrayList: ArrayList<CommonDataModel1> = ArrayList()

    }

    private lateinit var songDetailsAdapter: SongDetailsAdapter

    private fun setupSongListRecyclerView() {
        songDetailsAdapter = SongDetailsAdapter(this@MainActivity, this@MainActivity)
        mLayoutManager = GridLayoutManager(this@MainActivity, 1)
        binding.playerLayout.songsList.layoutManager = mLayoutManager
        binding.playerLayout.songsList.itemAnimator = DefaultItemAnimator()
        binding.playerLayout.songsList.adapter = songDetailsAdapter
    }

    private fun showSong2(
        modelData: CommonDataModel1,
        modelDataList: List<CommonDataModel1>,
        mPosition: Int,
        totalLength: Int, trackChangeFromPlayer: Boolean = false
    ) {
        showProgressDialog()

        songId = modelData.song_id
        playerViewModel.getCurrentSongId.value = modelData.song_id
        songIcon = modelData.thumbnail
        mModelData = modelData
        if (modelData.audio_location.isNullOrEmpty()) {
            Toast.makeText(this, "No audio link provided", Toast.LENGTH_SHORT).show()
            return
        }
        itemPosition = mPosition

        musicService!!.addList(modelDataList)

        /*val layoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layerBinding = SongsBottomsheetBinding.inflate(layoutInflater, null, false);
*/
        var position = mPosition
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
        }

        var isFavourate = false

        addDetailsView(modelData.song_id.toString())

        songDetailsAdapter.updateData(modelDataList)
        getSongPlayCount(modelData.song_id)

        Coroutines.main {
            playServiceSongs(modelData.audio_location, modelData)
        }
        with(binding.playerLayout) {
            playBtn = ivPlayIcon
            playBtnShort = ivPlayPause
            songName = tvTitle
            x = 0
            var btnPlayState = false;

            // isPlaying = btnPlayState
            //  pbLoading.visibility = View.GONE
            // ivPlayIcon.visibility = View.GONE
            songsTitle.text = modelData.title
            if (modelData.artists_name.isNotEmpty()) {
                var artist = ""
                var artistName = modelData.artists_name?.forEach {
                    it
                    artist += it + ","

                }
                val idList: String = modelData.artists_name?.toString().toString()
                val csv = idList.substring(1, idList.length - 1).replace(", ", ",")
                tvSongDesc.text = csv
                tvTitle.text = csv
            } else {
                tvSongDesc.text = modelData.description
                tvDesc.text = modelData.description
                tvTitle.text = modelData.description

            }


            tvDesc.text = modelData.title

            songsTitle.isSelected = true
            tvSongDesc.isSelected = true
            tvDesc.isSelected = true
            tvTitle.isSelected = true

            Glide.with(applicationContext)
                .load(modelData.thumbnail)
                .timeout(6000)
                .error(R.drawable.logo)
                .placeholder(R.drawable.logo)
                .into(ivThumbnail)
            Glide.with(applicationContext)
                .load(modelData.thumbnail)
                .timeout(6000)
                .error(R.drawable.logo)
                .placeholder(R.drawable.logo)
                .into(ivLargeThumbnail)

            if (!trackChangeFromPlayer) {


                val paddingSDP = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._70sdp)
                binding.rlFragment.setPadding(0, 0, 0, paddingSDP)

                rlBottomActivity.visibility = View.VISIBLE
                rlFullScreenActivity.visibility = View.GONE
                if (llSongList.isVisible)
                    llSongList.visibility = View.GONE
                //(dialog).behavior.peekHeight = 200
            } else {
                rlBottomActivity.visibility = View.GONE
                rlFullScreenActivity.visibility = View.VISIBLE
                if (llSongList.isVisible)
                    llSongList.visibility = View.GONE
                //  (dialog).behavior.peekHeight = getScreenWidth(mainActivity as Activity)
                //(dialog).behavior.peekHeight = getScreenHeight()

            }


            //isPLAYING = false

            //  PlayerServiceInstance().reset()


            val mHandler = Handler(Looper.getMainLooper())
            //Seekbar on UI thread

            this@MainActivity.runOnUiThread(object : Runnable {
                override fun run() {
                    isPLAYING = PlayerServiceInstance().isPlaying
                    if (isPLAYING) {
                        val mCurrentPosition: Int = PlayerServiceInstance().currentPosition / 1000
                        val mCurrentPositionTxt =
                            DateUtils.formatElapsedTime((PlayerServiceInstance().currentPosition / 1000).toLong())
                        seekbarPlay.progress = mCurrentPosition
                        seekbarPlay.max = PlayerServiceInstance().duration / 1000

                        val durationText =
                            DateUtils.formatElapsedTime((PlayerServiceInstance().duration / 1000).toLong()) // converting time in millis to minutes:second format eg 14:15 min


                        tvNextTime.text = durationText.toString()
                        tvPrevTime.text = mCurrentPositionTxt.toString()


                        if (animationView.isAnimating)
                            animationView.visibility = View.VISIBLE
                        else
                            animationView.visibility = View.GONE
                    }
                    mHandler.postDelayed(this, 1000)
                }
            })

            /*svScroll.setOnTouchListener(object :View.OnTouchListener{
                override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                    Toast.makeText(this@MainActivity, "ScrollView Disabled", Toast.LENGTH_SHORT).show();
                    return true;
                }

            })*/

            ivFavourate.setImageResource(R.drawable.ic_heart)
            isFavourate = false
            if (modelData.is_liked == 1) {
                ivFavourate.setImageResource(R.drawable.ic_heart_red)
                isFavourate = true
            } else {
                ivFavourate.setImageResource(R.drawable.ic_heart)
                isFavourate = false
            }

            ivPrevTrack.setOnClickListener {
                if (position > 0) {

                    // setSongPosition(false)

                    // playServiceSongs(modelData.audio_location)
                    if (isPlayRand)
                        position = (modelDataList.indices).random()
                    else
                        position--
                    itemPosition = position

                    //  arrayList.clear()
                    //  arrayList.addAll(modelDataList)
                    mModelData = modelData
                    showSong2(modelDataList[position], modelDataList, position, totalLength, true)
                    return@setOnClickListener
                } else {
                    position = 0
                    itemPosition = position
                    showSong2(modelDataList[position], modelDataList, position, totalLength, true)

                    /*Toast.makeText(
                        this@MainActivity,
                        "No previous song available",
                        Toast.LENGTH_SHORT
                    ).show()*/
                    return@setOnClickListener

                }
            }

            val subscribeStatus = sessionManager?.getSubscribed()

            ivNextTrack.setOnClickListener {
                if (position < totalLength - 1) {
                    if (nextTrackCount == 5 && !isPaymentDone) {
                        if (subscribeStatus == false) {
                            Toast.makeText(
                                this@MainActivity,
                                "Pay for premium version to proceed further",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.paymentLayout.root.visibility = View.VISIBLE

                            modelDataPayment = modelData
                            modelDataListPayment = modelDataList
                            mPositionPayment = mPosition
                            totalLengthPayment = totalLength
                            trackChangeFromPlayerPayment = trackChangeFromPlayer
                            return@setOnClickListener
                        } /*else {
                            nextTrackCount++
                        }*/
                    } else
                        nextTrackCount++

                    if (isPlayRand)
                        position = (modelDataList.indices).random()
                    else
                        position++

                    //setSongPosition(true)

                    itemPosition = position

                    // playServiceSongs(modelData.audio_location)
                    showSong2(modelDataList[position], modelDataList, position, totalLength, true)
                    return@setOnClickListener
                } else {
                    position = 0
                    itemPosition = position

                    // playServiceSongs(modelData.audio_location)
                    showSong2(modelDataList[position], modelDataList, position, totalLength, true)

                    /* Toast.makeText(
                         this@MainActivity,
                         "No next song available",
                         Toast.LENGTH_SHORT
                     ).show()*/
                    return@setOnClickListener


                }
            }

            ivPrevTrack1.setOnClickListener {
                if (position > 0) {

                    // setSongPosition(false)

                    // playServiceSongs(modelData.audio_location)
                    if (isPlayRand)
                        position = (modelDataList.indices).random()
                    else
                        position--
                    itemPosition = position

                    // arrayList.clear()
                    //  arrayList.addAll(modelDataList)
                    mModelData = modelData
                    showSong2(modelDataList[position], modelDataList, position, totalLength, false)
                    return@setOnClickListener
                } else {
                    position = 0
                    itemPosition = position
                    showSong2(modelDataList[position], modelDataList, position, totalLength, true)

                    /*Toast.makeText(
                        this@MainActivity,
                        "No previous song available",
                        Toast.LENGTH_SHORT
                    ).show()*/
                    return@setOnClickListener

                }
            }

            //  val subscribeStatus = sessionManager?.getSubscribed()

            ivNextTrack1.setOnClickListener {
                if (position < totalLength) {
                    if (nextTrackCount == 5 && !isPaymentDone) {
                        if (subscribeStatus == false) {
                            Toast.makeText(
                                this@MainActivity,
                                "Pay for premium version to proceed further",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.paymentLayout.root.visibility = View.VISIBLE

                            modelDataPayment = modelData
                            modelDataListPayment = modelDataList
                            mPositionPayment = mPosition
                            totalLengthPayment = totalLength
                            trackChangeFromPlayerPayment = trackChangeFromPlayer
                            return@setOnClickListener
                        } else {
                            nextTrackCount++
                        }
                    } else
                        nextTrackCount++

                    if (isPlayRand)
                        position = (modelDataList.indices).random()
                    else
                        position++

                    //setSongPosition(true)

                    itemPosition = position

                    // playServiceSongs(modelData.audio_location)
                    showSong2(modelDataList[position], modelDataList, position, totalLength, false)
                    return@setOnClickListener
                } else {
                    position = 0
                    itemPosition = position

                    // playServiceSongs(modelData.audio_location)
                    showSong2(modelDataList[position], modelDataList, position, totalLength, true)

                    /*Toast.makeText(
                        this@MainActivity,
                        "No next song available",
                        Toast.LENGTH_SHORT
                    ).show()*/
                    return@setOnClickListener


                }
            }

            ivPlayPause.setOnClickListener {
//                println("SEEK BAR   ${mp!!.currentPosition}")
                PlayerServiceInstance().let { itMP ->

                    vibrate(vib)
                    if (btnPlayState) {
                        itMP.seekTo(x)
                        itMP.start()
                        musicService!!.showNotification(
                            R.drawable.ic_pause,
                            modelData
                        )
                        ivPlayIcon.setImageResource(R.drawable.ic_pause)
                        ivPlayPause.setImageResource(R.drawable.pause_icon)
                        //ContextCompat.getDrawable(this@MainActivity, R.drawable.pause_icon)

                        btnPlayState = false;
                        isPLAYING = true
                    } else {
                        x = itMP.currentPosition;
                        itMP.pause();
                        Log.v("log", "" + x);
                        musicService!!.showNotification(
                            R.drawable.play,
                            modelData
                        )
                        ivPlayIcon.setImageResource(R.drawable.play)
                        ivPlayPause.setImageResource(R.drawable.play_icon)
                        //  ContextCompat.getDrawable(this@MainActivity, R.drawable.play_icon)
                        btnPlayState = true;
                        isPLAYING = false
                    }

                }
            }

            btnPlayPause.setOnClickListener {
                //  println("SEEK BAR   ${mp!!.currentPosition}")
                PlayerServiceInstance().let { itMP ->

                    vibrate(vib)
                    if (btnPlayState) {
                        itMP.seekTo(x)
                        itMP.start()
                        musicService!!.showNotification(
                            R.drawable.ic_pause,
                            modelData
                        )
                        ivPlayIcon.setImageResource(R.drawable.ic_pause)
                        ivPlayPause.setImageResource(R.drawable.pause_icon)
                        //ContextCompat.getDrawable(this@MainActivity, R.drawable.pause_icon)
                        btnPlayState = false;
                    } else {
                        x = itMP.currentPosition;
                        itMP.pause();
                        Log.v("log", "" + x);
                        musicService!!.showNotification(
                            R.drawable.play,
                            modelData
                        )
                        ivPlayIcon.setImageResource(R.drawable.play)

                        ivPlayPause.setImageResource(R.drawable.play_icon)
                        //ContextCompat.getDrawable(this@MainActivity, R.drawable.play_icon)
                        btnPlayState = true;
                    }

                }
            }


            ivFavourate.setOnClickListener {
                if (isFavourate) {
                    ivFavourate.setImageResource(R.drawable.ic_heart)
                    isFavourate = false
                    modelData.is_liked = 0
                    modelDataList[position].is_liked = 0
                    arrayList[position].is_liked = 0
                    addToFavourites(ivFavourate, animationView, modelData.song_id.toString())
                } else {
                    animationView.playAnimation()
                    animationView.visibility = View.VISIBLE
                    ivFavourate.setImageResource(R.drawable.ic_heart_red)
                    isFavourate = true
                    modelData.is_liked = 1
                    modelDataList[position].is_liked = 1
                    arrayList[position].is_liked = 1
                    addToFavourites(ivFavourate, animationView, modelData.song_id.toString())
                }
                /*Handler(Looper.getMainLooper()).postDelayed({
                    animationView.pauseAnimation()
                    animationView.
                }, 2000)*/
            }

            seekbarPlay.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        PlayerServiceInstance().seekTo(progress * 1000);

                        //   musicService!!.mediaPlayer!!.seekTo(progress)

                        musicService!!.showNotification(
                            if (isPlaying) R.drawable.ic_pause else R.drawable.play,
                            modelData
                        )

                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            })

            /*llSongProfile.setOnTouchListener { view, motionEvent ->

                svScroll.isNestedScrollingEnabled=false
                return@setOnTouchListener true
            }*/
            // clControl.requestDisallowInterceptTouchEvent(true)
            rlBottomActivity.setOnClickListener {
                //Toast.makeText(context, "PRESSED", Toast.LENGTH_SHORT).show()
                rlBottomActivity.visibility = View.GONE
                rlFullScreenActivity.visibility = View.VISIBLE

                llSongList.visibility = View.GONE
                // nsvScroll.visibility = View.VISIBLE
                // nsvScroll.fullScroll(ScrollView.FOCUS_UP);
                //  (dialog).behavior.peekHeight = getScreenWidth(mainActivity as Activity)
                //(dialog).behavior.peekHeight = getScreenHeight()
            }


            btnBack.setOnClickListener {
                // Toast.makeText(context, "PRESSED", Toast.LENGTH_SHORT).show()

                if (llSongList.isVisible) {
                    llSongList.visibility = View.GONE
                    // nsvScroll.visibility = View.VISIBLE
                    // binding.playerLayout.nsvScroll.fullScroll(ScrollView.FOCUS_UP);
                } else {


                    val paddingSDP = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._70sdp)
                    binding.rlFragment.setPadding(0, 0, 0, paddingSDP)
                    rlBottomActivity.visibility = View.VISIBLE
                    rlFullScreenActivity.visibility = View.GONE
                }
                // (dialog).behavior.peekHeight = 200
            }


            listItem.setOnClickListener {
                llSongList.visibility = View.VISIBLE
                // nsvScroll.visibility=View.GONE
                // scrollToView(svScroll,txt1)
            }


            /*listItem.setOnClickListener {
                // Toast.makeText(this@MainActivity, "clicked", Toast.LENGTH_SHORT).show()
                // Toast.makeText(context, "PRESSED", Toast.LENGTH_SHORT).show()
                rlBottomActivity.visibility = View.VISIBLE
                rlFullScreenActivity.visibility = View.GONE
                // (dialog).behavior.peekHeight = 200
            }*/


            if (isPlayLoop) {
                ivLoopSong.setColorFilter(
                    ContextCompat.getColor(this@MainActivity, R.color.textyellow),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                ivLoopSong.setColorFilter(
                    ContextCompat.getColor(this@MainActivity, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            if (isPlayRand) {
                ivRandSong.setColorFilter(
                    ContextCompat.getColor(this@MainActivity, R.color.textyellow),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                ivRandSong.setColorFilter(
                    ContextCompat.getColor(this@MainActivity, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }

            ivShare.setOnClickListener {
                shareAppLink(modelData.title, modelData.audio_location)
            }
            ivLoopSong.setOnClickListener {
                if (isPlayLoop) {
                    isPlayLoop = false
                    ivLoopSong.setColorFilter(
                        ContextCompat.getColor(this@MainActivity, R.color.white),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    );

                } else {
                    isPlayLoop = true
                    ivLoopSong.setColorFilter(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.textyellow
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    );
                }
            }

            ivRandSong.setOnClickListener {

                if (isPlayRand) {
                    isPlayRand = false
                    ivRandSong.setColorFilter(
                        ContextCompat.getColor(this@MainActivity, R.color.white),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    );

                } else {
                    isPlayRand = true
                    ivRandSong.setColorFilter(
                        ContextCompat.getColor(this@MainActivity, R.color.textyellow),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    );
                }
            }

            ivPlayPause.setImageResource(R.drawable.pause_icon)

            ivPlayIcon.setImageResource(R.drawable.ic_pause)

            println(modelData.audio_location)



            PlayerServiceInstance().setOnCompletionListener(object :
                MediaPlayer.OnCompletionListener {
                override fun onCompletion(p0: MediaPlayer?) {

                    if (position < totalLength) {

                        if (!isPlayLoop) {
                            if (isPlayRand)
                                position = (modelDataList.indices).random()
                            else
                                position++
                        }

                        itemPosition = position
                        showSong2(
                            modelDataList[position],
                            modelDataList,
                            position,
                            totalLength,
                            true
                        )

                    } else {
                        if (!isPlayLoop) {
                            if (isPlayRand)
                                position = (modelDataList.indices).random()
                            else
                                position = 0
                        }
                        itemPosition = position
                        showSong2(
                            modelDataList[position],
                            modelDataList,
                            position,
                            totalLength,
                            true
                        )
                    }
                }

            })

            rlFullScreenActivity.setOnClickListener {
                return@setOnClickListener
            }
            llSongList.setOnClickListener {
                return@setOnClickListener
            }
        }

        hideProgressDialog()
        // RelativeLayout headerLay = (RelativeLayout) dialog.findViewById(R.id.addTask_dialog_header);
    }


    private fun scrollToView(scrollViewParent: ScrollView, view: View) {
        // Get deepChild Offset
        val childOffset = Point()
        getDeepChildOffset(scrollViewParent, view.parent, view, childOffset)
        // Scroll to child.
        scrollViewParent.smoothScrollTo(0, childOffset.y)
    }

    // slide the view from below itself to the current position
    fun slideUp(view: View) {
        //view.visibility = View.VISIBLE
        val animate = TranslateAnimation(
            0F,  // fromXDelta
            0F,  // toXDelta
            view.height.toFloat(),  // fromYDelta
            0F
        ) // toYDelta
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    // slide the view from its current position to below itself
    fun slideDown(view: View) {
        val animate = TranslateAnimation(
            0F,  // fromXDelta
            0F,  // toXDelta
            0F,  // fromYDelta
            view.height.toFloat()
        ) // toYDelta
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
    }

    private fun getDeepChildOffset(
        mainParent: ViewGroup,
        parent: ViewParent,
        child: View,
        accumulatedOffset: Point
    ) {
        val parentGroup = parent as ViewGroup
        accumulatedOffset.x += child.left
        accumulatedOffset.y += child.top
        if (parentGroup == mainParent) {
            return
        }
        getDeepChildOffset(mainParent, parentGroup.parent, parentGroup, accumulatedOffset)
    }

    override fun onDestroy() {
        musicService!!.stopPlayerService()

        super.onDestroy()
    }

    private fun getSongPlayCount(songId: Int) {
        if (CheckConnectivity.getInstance(this).isOnline) {

            //showProgressDialog()
            Log.d("hgvjhngvjgv", "Song played Called")
            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<FavouritesResponseModel?>? =
                uploadAPIs.songViewCount("Bearer " + sessionManager?.getToken(), songId.toString())
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
                                if (status == true) {
                                    // Toast.makeText(this@MainActivity,"Song ",Toast.LENGTH_SHORT).show()

                                    //isFavourate = true

                                } else {
                                    //  Toast.makeText(this@MainActivity,"Song has been removed from favourites",Toast.LENGTH_SHORT).show()

                                }


                            }


                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    //  hideProgressDialog()
                }

                override fun onFailure(call: Call<FavouritesResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(this, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addDetailsView(song_id: String) {
        if (CheckConnectivity.getInstance(this).isOnline) {

            //showProgressDialog()
            Log.d("hgvjhngvjgv", "View Called")
            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<FavouritesResponseModel?>? =
                uploadAPIs.songPlayedView("Bearer " + sessionManager?.getToken(), song_id)
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
                                if (status == true) {
                                    // Toast.makeText(this@MainActivity,"Song ",Toast.LENGTH_SHORT).show()

                                    //isFavourate = true

                                } else {
                                    //  Toast.makeText(this@MainActivity,"Song has been removed from favourites",Toast.LENGTH_SHORT).show()

                                }


                            }


                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    //  hideProgressDialog()
                }

                override fun onFailure(call: Call<FavouritesResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(this, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }

    }

    private fun addToFavourites(
        ivFavourate: ImageView,
        animationView: LottieAnimationView,
        song_id: String
    ) {
        if (CheckConnectivity.getInstance(this).isOnline) {

            //showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<FavouritesResponseModel?>? =
                uploadAPIs.addtoLikeSongs("Bearer " + sessionManager?.getToken(), song_id)
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
                                if (status == true) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Song has been added in favourites",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    animationView.playAnimation()
                                    animationView.visibility = View.VISIBLE
                                    ivFavourate.setImageResource(R.drawable.ic_heart_red)
                                    //isFavourate = true

                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Song has been removed from favourites",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    ivFavourate.setImageResource(R.drawable.ic_heart)
                                }


                            }


                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    //  hideProgressDialog()
                }

                override fun onFailure(call: Call<FavouritesResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(this, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }
    /*fun getScreenHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }

    fun getScreenWidth(activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.height() - insets.top - insets.bottom
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }*/

    fun vibrate(vib: Vibrator) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            vib!!.vibrate(200)
        }
    }

    var isPLAYING = false

    var mp: MediaPlayer? = null

    fun PlayerInstance(): MediaPlayer {
        if (mp == null)
            mp = MediaPlayer()
        return mp!!
    }

    fun PlayerServiceInstance(): MediaPlayer {
        if (musicService!!.mediaPlayer == null)
            musicService!!.mediaPlayer = MediaPlayer()
        return musicService!!.mediaPlayer!!
    }

    fun playSongs(audioUrl: String) {


        if (!PlayerInstance().isPlaying) {
            isPLAYING = true
            try {
                mp?.apply {
                    mp?.reset()
                    setDataSource(audioUrl)
                    prepare()
                    start()
                    /* setOnPreparedListener(object :MediaPlayer.OnPreparedListener{
                         override fun onPrepared(p0: MediaPlayer?) {

                         }

                     })*/

                }
            } catch (e: Exception) {
                Log.e("LOG_TAG", e.message.toString())
            }
        } else {
            isPLAYING = false
            stopPlaying()
        }

    }

    fun playServiceSongs(audioUrl: String, modelData: CommonDataModel1) {
        /* if (musicService!!.mediaPlayer == null) {
             // bottomll.visibility = View.VISIBLE
             musicService!!.mediaPlayer = MediaPlayer()
             musicService!!.mediaPlayer!!.reset()
             // mediaPlayer!!.setDataSource("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
             musicService!!.mediaPlayer!!.setDataSource(audioUrl)
             musicService!!.mediaPlayer!!.prepare()
             musicService!!.mediaPlayer!!.start()
             musicService!!.mediaPlayer?.duration?.div(
                 1000
             )
             musicService!!.showNotification(
                 R.drawable.ic_pause, modelData
             )
         } else {*/
        PlayerServiceInstance().reset()
        // PlayerServiceInstance().setAudioStreamType(AudioManager.STREAM_MUSIC);
        PlayerServiceInstance().setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        try {

            PlayerServiceInstance().setDataSource(audioUrl)
            PlayerServiceInstance().prepare()
            PlayerServiceInstance().start()
        } catch (e: IOException) {

            Toast.makeText(this, "mp3 not found", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }


        PlayerServiceInstance().setOnPreparedListener(object : MediaPlayer.OnPreparedListener {
            override fun onPrepared(p0: MediaPlayer?) {
                hideProgressDialog()
            }

        })
        PlayerServiceInstance().duration?.div(
            1000
        )
        /*PlayerServiceInstance().setOnBufferingUpdateListener(object :
            MediaPlayer.OnBufferingUpdateListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onBufferingUpdate(p0: MediaPlayer?, p1: Int) {
                var percent = 0
                if (p1 < 0) {
                    percent = Math.round((Math.abs(p1) - 1) * 100.0 / Int.MAX_VALUE)
                        .toInt()
                    binding.playerLayout.ivPlayIcon.visibility = View.GONE
                    binding.playerLayout.pbLoading.apply {
                        visibility = View.VISIBLE
                        setProgress(percent, true)
                    }
                } else if (p1 > 100) {
                    percent = Math.round((Math.abs(p1) - 1) * 100.0 / Int.MAX_VALUE)
                        .toInt()
                    binding.playerLayout.ivPlayIcon.visibility = View.VISIBLE
                    binding.playerLayout.pbLoading.visibility = View.GONE
                }

            }

        })*/

        musicService!!.showNotification(
            R.drawable.ic_pause, modelData
        )
        //  }

        //notifyBroadcast.receiverListener(this)
        /* if (!PlayerInstance().isPlaying) {
             isPLAYING = true
             try {
                 mp?.apply {
                     mp?.reset()
                     setDataSource(audioUrl)
                     prepare()
                     start()
                     *//* setOnPreparedListener(object :MediaPlayer.OnPreparedListener{
                         override fun onPrepared(p0: MediaPlayer?) {

                         }

                     })*//*

                }
            } catch (e: Exception) {
                Log.e("LOG_TAG", e.message.toString())
            }
        } else {
            isPLAYING = false
            stopPlaying()
        }*/

    }

    private fun stopPlaying() {
        if (mp != null && mp!!.isPlaying) {
            mp?.reset()
            mp = null
        }
    }


    override fun onClick(modelData: List<CommonDataModel1>, position: Int, type: String) {

        if (type == "Play_Song") {
            showSong2(arrayList[position], arrayList, position, arrayList.size - 1, true)
            // playerViewModel.startSongModel1.value = PlayerDataModel1(1, position, modelData)
            binding.playerLayout.llSongList.visibility = View.GONE


            //
        } else if (type == "Add_To_Playlist") {
            showAddToPlayListDialog(modelData[position].song_id.toString())
            // addtoPlaylist(modelData,position)
        } else if (type == "Like_Song") {
            likeSong(modelData, position)
        } else if (type == "Share_Song") {
            shareAppLink(modelData[position].title, modelData[position].audio_location)
        }


    }

    lateinit var playlistAdapter: PlayListCreatedAdapter

    var dismissDialog: Dialog? = null
    private fun showAddToPlayListDialog(song_id: String) {
        val dialog = Dialog(this, R.style.DialogSlideAnim)

        dialog.getWindow()?.setBackgroundDrawableResource(R.color.greywhite)
        dialog.getWindow()
            ?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        val binding: DialogAddToPlaylistBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                this
            ), R.layout.dialog_add_to_playlist, null, false
        )
        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(true)
        dismissDialog = dialog
        dialog.setCancelable(true)
        dialog.show()
        with(binding) {

            playlistAdapter =
                PlayListCreatedAdapter(context = this@MainActivity, onItemClickListener = object :
                    PlayListCreatedAdapter.OnPlaylistItemClickListener {
                    override fun onClick(modelData: List<DataX>, position: Int) {
                        addtoPlaylist(modelData, position, song_id)
                        dialog.dismiss()
                    }

                })
            var mLayoutManager = GridLayoutManager(this@MainActivity, 2)

            imgback.setOnClickListener {
                dialog.dismiss()
            }
            mCurrentPlayListPage = 0
            loadMorePlayListItems(true)
            recPlaylist.layoutManager = mLayoutManager
            recPlaylist.addOnScrollListener(recyclerOnScrollPlaylist)
            recPlaylist.itemAnimator = DefaultItemAnimator()
            recPlaylist.adapter = playlistAdapter
        }
        /* val rvTest = dialog.findViewById(R.id.rvTest)
         rvTest.setHasFixedSize(true)
         rvTest.layoutManager = LinearLayoutManager(context)
         rvTest.addItemDecoration(SimpleDividerItemDecoration(context, R.drawable.divider))

         val rvAdapter = DataDialogAdapter(context, rvTestList)
         rvTest.adapter = rvAdapter*/
    }


    val recyclerOnScrollPlaylist = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            // number of visible items
            val visibleItemCount = recyclerView.layoutManager?.childCount;
            // number of items in layout
            val totalItemCount = recyclerView.layoutManager?.itemCount;
            // the position of first visible item
            val firstVisibleItemPosition =
                (recyclerView.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()

            val isNotLoadingAndNotLastPage = !mIsLoading && !mIsPlayListLastPage;
            // flag if number of visible items is at the last
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount!! >= totalItemCount!!;
            // validate non negative values
            val isValidFirstItem = firstVisibleItemPosition >= 0;
            // validate total items are more than possible visible items
            val totalIsMoreThanVisible = totalItemCount >= pageSize;
            // flag to know whether to load more
            val shouldLoadMore =
                isValidFirstItem && isAtLastItem && totalIsMoreThanVisible && isNotLoadingAndNotLastPage

            if (shouldLoadMore) loadMorePlayListItems(false);
        }
    }


    var mIsLoading = false;
    var mIsLastPage = false;
    var mIsPlayListLastPage = false;
    var mCurrentPlayListPage = 0;
    final var pageSize = 10;
    private fun loadMorePlayListItems(isFirstPage: Boolean) {
        // change loading state
        mIsLoading = true
        mCurrentPlayListPage += 1

        // update recycler adapter with next page
        if (CheckConnectivity.getInstance(this@MainActivity).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<PlaylistCreatedResponseModel?>? = uploadAPIs.getCreatedPlaylistData(
                "Bearer " + sessionManager?.getToken(),

                mCurrentPlayListPage.toString()
            )
            mcall?.enqueue(object : Callback<PlaylistCreatedResponseModel?> {
                override fun onResponse(
                    call: Call<PlaylistCreatedResponseModel?>,
                    response: retrofit2.Response<PlaylistCreatedResponseModel?>,
                ) {
                    try {
                        hideProgressDialog()
                        if (response.body() != null) {

                            val result = response.body()!!
                            //Log.v("responseannouncement-->", result)
                            /*  val mjonsresponse = JSONObject(result)
                              val data = mjonsresponse.getJSONObject("data")
                              val dataArray = data.getJSONArray("data")*/
                            Log.d("result", response.body()!!.status.toString())
                            if (result.status == true) {

                                result.apply {

                                    if (data!!.data!!.isNotEmpty()) {

                                        // activityAlbumdetailsBinding.mainLl.visibility = View.VISIBLE
                                        if (isFirstPage) playlistAdapter?.updateList(data?.data!!) else playlistAdapter?.addToList(
                                            data?.data!!
                                        )
                                        mIsLoading = false
                                        mIsPlayListLastPage =
                                            mCurrentPlayListPage == result.data?.lastPage
                                    } else if (message == "No songs available") {
                                        Log.d("mbvmbv", "hfvjhvj")
                                        hideProgressDialog()
                                        mIsLoading = false
                                        // activityAlbumdetailsBinding.mainLl.visibility = View.GONE
                                        Toast.makeText(
                                            this@MainActivity,
                                            "no data found",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        hideProgressDialog()
                                        Toast.makeText(
                                            this@MainActivity,
                                            "no data found",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                }

                            } else {
                                mIsLoading = false
                                hideProgressDialog()
                                if (dismissDialog != null) {
                                    dismissDialog?.dismiss()
                                }
                                Toast.makeText(
                                    this@MainActivity,
                                    "Please create a playlist first!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<PlaylistCreatedResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(this@MainActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT)
                .show()
        }

    }

    private fun addtoPlaylist(modelData: List<DataX>, position: Int, song_id: String) {
        if (CheckConnectivity.getInstance(this).isOnline) {

            //showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<FavouritesResponseModel?>? = uploadAPIs.addtoPlaylist(
                "Bearer " + sessionManager?.getToken(),
                AddToPlaylistRequestModel(song_id.trim().toInt(), modelData[position].id)
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
                                if (status == true) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Successfully added into playlist",
                                        Toast.LENGTH_SHORT
                                    ).show()


                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Removed from playlist",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            }


                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    //  hideProgressDialog()
                }

                override fun onFailure(call: Call<FavouritesResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(this@MainActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT)
                .show()
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
        if (!isFinishing)
            mProgressDialog!!.show()
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }


    private fun likeSong(modelData: List<CommonDataModel1>, position: Int) {
        if (CheckConnectivity.getInstance(this).isOnline) {

            //showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<FavouritesResponseModel?>? = uploadAPIs.addtoLikeSongs(
                "Bearer " + sessionManager?.getToken(),
                modelData[position].song_id.toString()
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
                                if (status == true) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Song has been liked",
                                        Toast.LENGTH_SHORT
                                    ).show()


                                } else {
                                    //Toast.makeText(mainActivity,"Removed from playlist",Toast.LENGTH_SHORT).show()
                                }

                            }


                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    //  hideProgressDialog()
                }

                override fun onFailure(call: Call<FavouritesResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(this@MainActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT)
                .show()
        }

    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {

        if (musicService == null) {
            val binder = p1 as MusicService.MyBinder
            musicService = binder.currentService()
            musicService!!.audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            mediaresult = musicService!!.audioManager.requestAudioFocus(
                musicService,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )

        }
        musicService!!.mediaPlayer = MediaPlayer()
        musicService!!.seekBarSetup()
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }
    /*private fun isUserHasSubscription(context: Context) {
        billingClient =
            BillingClient.newBuilder(context).enablePendingPurchases().setListener(this).build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS)
                billingClient.queryPurchaseHistoryAsync(
                    BillingClient.SkuType.SUBS
                ) { billingResult1: BillingResult, list: List<PurchaseHistoryRecord?>? ->
                    Log.d(
                        "billingprocess",
                        "purchasesResult.getPurchasesList():" + purchasesResult.purchasesList
                    )
                    if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK &&
                        !Objects.requireNonNull(purchasesResult.purchasesList).isEmpty()
                    ) {

                        //here you can pass the user to use the app because he has an active subscription
                        val myIntent = Intent(context, MainActivity::class.java)
                        startActivity(myIntent)
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.d("billingprocess", "onBillingServiceDisconnected")
            }
        })
    }*/

}