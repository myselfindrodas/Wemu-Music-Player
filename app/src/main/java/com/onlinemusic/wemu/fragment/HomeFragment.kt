package com.onlinemusic.wemu.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.graphics.Insets
import android.media.MediaPlayer
import android.os.*
import android.text.format.DateUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.onlinemusic.wemu.*
import com.onlinemusic.wemu.adapter.*
import com.onlinemusic.wemu.databinding.DialogAddToPlaylistBinding
import com.onlinemusic.wemu.databinding.FragmentHomeBinding
import com.onlinemusic.wemu.databinding.SongsBottomsheetBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.onlinemusic.wemu.responseModel.FavouritesResponseModel
import com.onlinemusic.wemu.responseModel.add_to_playlist.AddToPlaylistRequestModel
import com.onlinemusic.wemu.responseModel.albumslist.response.DataX
import com.onlinemusic.wemu.responseModel.dashboard.response.BannerData
import com.onlinemusic.wemu.responseModel.dashboard.response.DashboardResponseModel
import com.onlinemusic.wemu.responseModel.playlistitems.response.PlaylistCreatedResponseModel
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.viewmodel.PlayerDataModel1
import com.onlinemusic.wemu.viewmodel.PlayerViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit


class HomeFragment : Fragment(), Popular_week_Adapter.OnItemClickListener,
    PopularSongsAdapter.OnItemClickListener, RecentSongsAdapter.OnItemClickListener,
    NewReleaseAdapter.OnItemClickListener, TrendingNowAdapter.OnItemClickListener,
    NewAlbumsAdapter.OnItemClickListener, RecommendedAdapter.OnItemClickListener {
    var token = ""
    var sessionManager: SessionManager? = null
    lateinit var fragmentHomeBinding: FragmentHomeBinding
    var mProgressDialog: ProgressDialog? = null
    var bannerArray: ArrayList<BannerData> = ArrayList()
    var popularSongsArray: ArrayList<CommonDataModel1> = ArrayList()
    var recentSongsArray: ArrayList<CommonDataModel1> = ArrayList()
    var newReleaseSongsArray: ArrayList<CommonDataModel1> = ArrayList()
    var trendingSongsArray: ArrayList<CommonDataModel1> = ArrayList()

    var mIsPlayListLastPage = false;
    var mCurrentPlayListPage = 0;
    var mIsLoading = false
    final var pageSize = 10
    private lateinit var mainActivity: MainActivity

    var newAlbumsSongsArray: ArrayList<DataX> = ArrayList()


    var recommenddedArray: ArrayList<CommonDataModel1> = ArrayList()
    var popular_weekArray: ArrayList<CommonDataModel1> = ArrayList()
    private val sliderHandler: Handler = Handler(Looper.getMainLooper())
    lateinit var popularSongsAdapter: PopularSongsAdapter
    lateinit var recentSongsAdapter: RecentSongsAdapter
    lateinit var newReleaseAdapter: NewReleaseAdapter
    lateinit var trendingNowAdapter: TrendingNowAdapter
    lateinit var recomendedAdapter: RecommendedAdapter
    lateinit var newAlbumsAdapter: NewAlbumsAdapter
    lateinit var popularWeekAdapter: Popular_week_Adapter
    lateinit var dialog: BottomSheetDialog
    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var modelData: List<CommonDataModel1>


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
        // val view = inflater.inflate(R.layout.fragment_home, container, false)
        fragmentHomeBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        val root = fragmentHomeBinding.root
        modelData = ArrayList()
        playerViewModel = ViewModelProvider(activity as MainActivity)[PlayerViewModel::class.java]

        sessionManager = SessionManager(requireContext())

        this.mainActivity = activity as MainActivity
        dialog = BottomSheetDialog(mainActivity)
        fragmentHomeBinding.btnNotification.setOnClickListener {
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_notification)
        }

        if (sessionManager?.getTheme().equals("night")) {

            //fragmentHomeBinding.rlBgtheme.setBackgroundColor(resources.getColor(R.color.black,resources.newTheme()))
            fragmentHomeBinding.tvtitle.setTextColor(
                resources.getColor(
                    R.color.white,
                    resources.newTheme()
                )
            )

            fragmentHomeBinding.tvPopularsong.setTextColor(
                resources.getColor(
                    R.color.white,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.tvRecentlyplayed.setTextColor(
                resources.getColor(
                    R.color.white,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.tvNewrelease.setTextColor(
                resources.getColor(
                    R.color.white,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.tvTrendingnow.setTextColor(
                resources.getColor(
                    R.color.white,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.tvNewAlbum.setTextColor(
                resources.getColor(
                    R.color.white,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.tvRecommended.setTextColor(
                resources.getColor(
                    R.color.white,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.tvMostpopular.setTextColor(
                resources.getColor(
                    R.color.white,
                    resources.newTheme()
                )
            )

            fragmentHomeBinding.btnViewallPopular.setTextColor(
                resources.getColor(
                    R.color.textyellow,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.recentlyPlayedBtn.setTextColor(
                resources.getColor(
                    R.color.textyellow,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.newReleaseBtn.setTextColor(
                resources.getColor(
                    R.color.textyellow,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.btnTrendingNow.setTextColor(
                resources.getColor(
                    R.color.textyellow,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.newAlbumsBtn.setTextColor(
                resources.getColor(
                    R.color.textyellow,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.btnrecommended.setTextColor(
                resources.getColor(
                    R.color.textyellow,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.popularWeekBtn.setTextColor(
                resources.getColor(
                    R.color.textyellow,
                    resources.newTheme()
                )
            )

            fragmentHomeBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.white
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        } else {
            // fragmentHomeBinding.rlBgtheme.setBackgroundColor(resources.getColor(R.color.white,resources.newTheme()))
            fragmentHomeBinding.tvtitle.setTextColor(
                resources.getColor(
                    R.color.black,
                    resources.newTheme()
                )
            )

            fragmentHomeBinding.tvPopularsong.setTextColor(
                resources.getColor(
                    R.color.black,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.tvRecentlyplayed.setTextColor(
                resources.getColor(
                    R.color.black,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.tvNewrelease.setTextColor(
                resources.getColor(
                    R.color.black,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.tvTrendingnow.setTextColor(
                resources.getColor(
                    R.color.black,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.tvNewAlbum.setTextColor(
                resources.getColor(
                    R.color.black,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.tvRecommended.setTextColor(
                resources.getColor(
                    R.color.black,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.tvMostpopular.setTextColor(
                resources.getColor(
                    R.color.black,
                    resources.newTheme()
                )
            )


            fragmentHomeBinding.btnViewallPopular.setTextColor(
                resources.getColor(
                    R.color.black,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.recentlyPlayedBtn.setTextColor(
                resources.getColor(
                    R.color.black,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.newReleaseBtn.setTextColor(
                resources.getColor(
                    R.color.black,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.btnTrendingNow.setTextColor(
                resources.getColor(
                    R.color.black,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.newAlbumsBtn.setTextColor(
                resources.getColor(
                    R.color.black,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.btnrecommended.setTextColor(
                resources.getColor(
                    R.color.black,
                    resources.newTheme()
                )
            )
            fragmentHomeBinding.popularWeekBtn.setTextColor(
                resources.getColor(
                    R.color.black,
                    resources.newTheme()
                )
            )

            fragmentHomeBinding.btnNotification.setColorFilter(
                ContextCompat.getColor(
                    mainActivity,
                    R.color.black
                ), android.graphics.PorterDuff.Mode.MULTIPLY
            )

        }

        token = sessionManager?.getToken().toString()
//        mainActivity.showBottomView()
        Log.d("token", token)

        fragmentHomeBinding.btnViewallPopular.setOnClickListener {
//            val intent = Intent(requireActivity(), Recentlyplayed::class.java)
//            intent.putExtra("key","popular")
//            startActivity(intent)
            val bundle = Bundle()
            bundle.putString("key", "popular")
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_recentlyplayed, bundle)

        }
        fragmentHomeBinding.recentlyPlayedBtn.setOnClickListener {
//            val intent = Intent(requireActivity(), Recentlyplayed::class.java)
//            intent.putExtra("key","recent")
//            startActivity(intent)
            val bundle = Bundle()
            bundle.putString("key", "recent")
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_recentlyplayed, bundle)
        }
        fragmentHomeBinding.newReleaseBtn.setOnClickListener {
//            val intent = Intent(requireActivity(), Recentlyplayed::class.java)
//            intent.putExtra("key","new_release")
//            startActivity(intent)
            val bundle = Bundle()
            bundle.putString("key", "new_release")
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_recentlyplayed, bundle)
        }
        fragmentHomeBinding.btnTrendingNow.setOnClickListener {
//            val intent = Intent(requireActivity(), Recentlyplayed::class.java)
//            intent.putExtra("key","trending")
//            startActivity(intent)
            val bundle = Bundle()
            bundle.putString("key", "trending")
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_recentlyplayed, bundle)
        }

        fragmentHomeBinding.popularWeekBtn.setOnClickListener {
            /*val intent = Intent(requireActivity(), Recentlyplayed::class.java)
            intent.putExtra("key","popular_week")
            startActivity(intent)*/
            //Toast.makeText(requireContext(), "In progress", Toast.LENGTH_SHORT).show()
            val bundle = Bundle()
            bundle.putString("key", "popular")
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_recentlyplayed, bundle)
        }
        fragmentHomeBinding.etSearch.setOnClickListener {
//            val intent = Intent(requireActivity(), SearchActivity::class.java)
//            startActivity(intent)
            Log.d("hgfhf", "hcdhcghc")
            // playerViewModel.startSongModel1.value = PlayerDataModel1(2, 0, modelData)
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_search)
        }
        fragmentHomeBinding.llSearch.setOnClickListener {
//            val intent = Intent(requireActivity(), SearchActivity::class.java)
//            startActivity(intent)

            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_search)
        }
        fragmentHomeBinding.btnrecommended.setOnClickListener {
//            val intent = Intent(requireActivity(), Recentlyplayed::class.java)
//            intent.putExtra("key","recommended")
//            startActivity(intent)
            val bundle = Bundle()
            bundle.putString("key", "recommended")
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_recentlyplayed, bundle)
        }
        fragmentHomeBinding.newAlbumsBtn.setOnClickListener {
            /*val intent = Intent(requireActivity(), Recentlyplayed::class.java)
            intent.putExtra("key","new_albums")
            startActivity(intent)*/
            //Toast.makeText(requireContext(), "In progress", Toast.LENGTH_SHORT).show()
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_album)
        }
        // getData()
        setupViewPager()
        return root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDashboardData()

        playerViewModel.getCurrentSongId.observe(mainActivity, Observer {
            try {

                if (newReleaseAdapter != null)
                    newReleaseAdapter.notifyItemRangeChanged(0, newReleaseAdapter.itemCount)
                if (popularSongsAdapter != null)
                    popularSongsAdapter.notifyItemRangeChanged(0, popularSongsAdapter.itemCount)
                if (recomendedAdapter != null)
                    recomendedAdapter.notifyItemRangeChanged(0, recomendedAdapter.itemCount)
                if (recentSongsAdapter != null)
                    recentSongsAdapter.notifyItemRangeChanged(0, recentSongsAdapter.itemCount)
                if (trendingNowAdapter != null)
                    trendingNowAdapter.notifyItemRangeChanged(0, trendingNowAdapter.itemCount)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    lateinit var sliderAdapter: SliderAdapter
    private fun setupViewPager() {
        sliderAdapter = SliderAdapter(
            fragmentHomeBinding.viewpager,
            mainActivity
        )
        fragmentHomeBinding.viewpager.adapter = sliderAdapter
        fragmentHomeBinding.viewpager.clipToPadding = false
        fragmentHomeBinding.viewpager.clipChildren = false
        fragmentHomeBinding.viewpager.offscreenPageLimit = 3
        fragmentHomeBinding.viewpager.getChildAt(0).overScrollMode =
            RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(40))
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - Math.abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }
        fragmentHomeBinding.viewpager.setPageTransformer(
            compositePageTransformer
        )

        fragmentHomeBinding.viewpager.registerOnPageChangeCallback(
            object :
                OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    sliderHandler.removeCallbacks(sliderRunnable)
                    sliderHandler.postDelayed(sliderRunnable, 3000)
                }
            })
        fragmentHomeBinding.pagerIndicator2.attachToPager(
            fragmentHomeBinding.viewpager
        )
    }

    private fun getDashboardData() {
        if (CheckConnectivity.getInstance(requireContext()).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<DashboardResponseModel?>? =
                uploadAPIs.getDashboardData("Bearer " + sessionManager?.getToken())
            mcall?.enqueue(object : Callback<DashboardResponseModel?> {
                override fun onResponse(
                    call: Call<DashboardResponseModel?>,
                    response: retrofit2.Response<DashboardResponseModel?>,
                ) {
                    try {
                        hideProgressDialog()


                        if (response.body() != null) {
                            val result = response.body()!!
                            result.apply {

                                var bannerData = data!!.bannerData!! as ArrayList<BannerData>

                                sliderAdapter.updateData(bannerData)

                                sessionManager!!.setSubscribed(data!!.is_pro=="1")

                                val popuplarArrWeek = data.popular
                                // try {

                                for (i in 0 until popuplarArrWeek!!.size) {
                                    val j = i + 1
                                    val song_id = popuplarArrWeek[i].song_id ?: ""
                                    val title = popuplarArrWeek[i].title ?: ""
                                    val description = popuplarArrWeek[i].description ?: ""
                                    val audioLocation =
                                        if (popuplarArrWeek!![i].audio_location == null) "" else popuplarArrWeek!![i].audio_location
                                    val thumbnail = popuplarArrWeek[i].thumbnail ?: ""
                                    val views_count = popuplarArrWeek[i].views_count ?: 0
                                    val like_count = popuplarArrWeek[i].like_count ?: 0
                                    val play_count = popuplarArrWeek[i].play_count ?: 0
                                    val artist_name = popuplarArrWeek[i].artists_name
                                    val playlistId = popuplarArrWeek[i].playlistId
                                    val requestId = popuplarArrWeek[i].request_id
                                    val isLiked = popuplarArrWeek[i].is_liked
                                    val commonDataModel = CommonDataModel1(
                                        j,
                                        song_id.toString().toInt() ?: 0,
                                        title ?: "",
                                        description ?: "",
                                        thumbnail ?: "",
                                        audioLocation ?: "",
                                        views_count ?: 0,
                                        like_count ?: 0,
                                        play_count ?: 0,
                                        artist_name ?: emptyList(),
                                        playlistId ?: "",
                                        requestId ?: "",
                                        isLiked ?: 0

                                    )
                                    var popularWeek = Popular_Week(
                                        song_id.toString().toInt(),
                                        title,
                                        thumbnail
                                    )
                                    popularSongsArray.add(commonDataModel)
                                }
                                /* }catch (e:Exception){
                                     e.printStackTrace()
                                 }*/
                                if (popularSongsArray.size > 0) {
                                    popularSongsAdapter = PopularSongsAdapter(
                                        requireContext(),
                                        this@HomeFragment
                                    )

                                    val horizontaLayoutManagaer =
                                        LinearLayoutManager(
                                            requireContext(),
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                    fragmentHomeBinding.recPopular.layoutManager =
                                        horizontaLayoutManagaer
                                    fragmentHomeBinding.recPopular.adapter = popularSongsAdapter

                                    popularSongsAdapter.updateData(popularSongsArray)
                                }

                                val recentSongsArr = data.recentlyPlayed
                                for (i in 0 until recentSongsArr!!.size) {
                                    val j = i + 1
                                    val song_id = recentSongsArr[i].song_id
                                    val title = recentSongsArr[i].title
                                    val description =
                                        if (recentSongsArr[i].description == null) "" else recentSongsArr[i].description
                                    val audioLocation =
                                        if (data.recentlyPlayed!![i].audio_location == null) "" else data.recentlyPlayed!![i].audio_location
                                    val thumbnail = recentSongsArr[i].thumbnail
                                    val views_count = recentSongsArr[i].views_count
                                    val like_count = recentSongsArr[i].like_count
                                    val play_count = recentSongsArr[i].play_count
                                    var artist_name = recentSongsArr[i].artists_name
                                    val playlistId = recentSongsArr[i].playlistId
                                    val requestId = recentSongsArr[i].request_id
                                    val isLiked = recentSongsArr[i].is_liked

                                    val commonDataModel = CommonDataModel1(
                                        j,
                                        song_id.toString().toInt() ?: 0,
                                        title ?: "",
                                        description ?: "",
                                        thumbnail ?: "",
                                        audioLocation ?: "",
                                        views_count ?: 0,
                                        like_count ?: 0,
                                        play_count ?: 0,
                                        artist_name ?: emptyList(),
                                        playlistId ?: "",
                                        requestId ?: "",
                                        isLiked ?: 0
                                    )
                                    var recentlyPlayednew = RecentlyPlayednew(
                                        song_id.toString().toInt(),
                                        title,
                                        thumbnail
                                    )
                                    recentSongsArray.add(commonDataModel)
                                }
                                if (recentSongsArray.size > 0) {
                                    recentSongsAdapter = RecentSongsAdapter(
                                        requireContext(),
                                        this@HomeFragment
                                    )

                                    val horizontaLayoutManagaer =
                                        LinearLayoutManager(
                                            requireContext(),
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                    fragmentHomeBinding.recRecentplayed.layoutManager =
                                        horizontaLayoutManagaer
                                    fragmentHomeBinding.recRecentplayed.adapter = recentSongsAdapter
                                    recentSongsAdapter.updateData(recentSongsArray)
                                } else {
                                    fragmentHomeBinding.tvRecentlyplayed.visibility = View.GONE
                                    fragmentHomeBinding.recentlyPlayedBtn.visibility = View.GONE
                                }


                                val newReleaseSongsArr = data.newRelease
                                for (i in 0 until newReleaseSongsArr!!.size) {


                                    val j = i + 1
                                    val song_id = newReleaseSongsArr[i].song_id
                                    val title = newReleaseSongsArr[i].title
                                    val description =
                                        if (newReleaseSongsArr[i].description == null) "" else newReleaseSongsArr[i].description

                                    //val description = newReleaseSongsArr[i].description
                                    val audioLocation =
                                        if (data.newRelease!![i].audio_location == null) "" else data.newRelease!![i].audio_location
                                    val thumbnail = newReleaseSongsArr[i].thumbnail
                                    val views_count = newReleaseSongsArr[i].views_count
                                    val like_count = newReleaseSongsArr[i].like_count
                                    val play_count = newReleaseSongsArr[i].play_count
                                    var artist_name = newReleaseSongsArr[i].artists_name
                                    val playlistId = newReleaseSongsArr[i].playlistId
                                    val requestId = newReleaseSongsArr[i].request_id
                                    val isLiked = newReleaseSongsArr[i].is_liked


                                    val commonDataModel = CommonDataModel1(
                                        j,
                                        song_id.toString().toInt() ?: 0,
                                        title ?: "",
                                        description ?: "",
                                        thumbnail ?: "",
                                        audioLocation ?: "",
                                        views_count ?: 0,
                                        like_count ?: 0,
                                        play_count ?: 0,
                                        artist_name ?: emptyList(),
                                        playlistId ?: "",
                                        requestId ?: "",
                                        isLiked ?: 0
                                    )
                                    var newRelease =
                                        NewRelease(song_id.toString().toInt(), title, thumbnail)
                                    newReleaseSongsArray.add(commonDataModel)
                                }
                                if (newReleaseSongsArray.size > 0) {
                                    newReleaseAdapter = NewReleaseAdapter(
                                        requireContext(),
                                        this@HomeFragment
                                    )

                                    val horizontaLayoutManagaer =
                                        LinearLayoutManager(
                                            requireContext(),
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                    fragmentHomeBinding.recNewrelease.layoutManager =
                                        horizontaLayoutManagaer
                                    fragmentHomeBinding.recNewrelease.adapter = newReleaseAdapter
                                    newReleaseAdapter.updateData(newReleaseSongsArray)
                                }


                                val trendingSongsArr = data.trendingNow
                                for (i in 0 until trendingSongsArr!!.size) {

                                    val j = i + 1
                                    val song_id = trendingSongsArr[i].song_id
                                    val title = trendingSongsArr[i].title
                                    val description =
                                        if (trendingSongsArr[i].description == null) "" else trendingSongsArr[i].description

                                    //val description = trendingSongsArr[i].description
                                    val audioLocation =
                                        if (data.trendingNow!![i].audio_location == null) "" else data.trendingNow!![i].audio_location
                                    val thumbnail = trendingSongsArr[i].thumbnail
                                    val views_count = trendingSongsArr[i].views_count
                                    val like_count = trendingSongsArr[i].like_count
                                    val play_count = trendingSongsArr[i].play_count
                                    var artist_name = trendingSongsArr[i].artists_name
                                    val playlistId = trendingSongsArr[i].playlistId
                                    val requestId = trendingSongsArr[i].request_id
                                    val isLiked = trendingSongsArr[i].is_liked

                                    val commonDataModel = CommonDataModel1(
                                        j,
                                        song_id.toString().toInt() ?: 0,
                                        title ?: "",
                                        description ?: "",
                                        thumbnail ?: "",
                                        audioLocation ?: "",
                                        views_count ?: 0,
                                        like_count ?: 0,
                                        play_count ?: 0,
                                        artist_name ?: emptyList(),
                                        playlistId ?: "",
                                        requestId ?: "",
                                        isLiked ?: 0
                                    )
                                    var trendingNow = TrendingNow(
                                        song_id.toString().toInt(),
                                        title,
                                        thumbnail
                                    )

                                    trendingSongsArray.add(commonDataModel)
                                }
                                if (trendingSongsArray.size > 0) {
                                    trendingNowAdapter = TrendingNowAdapter(
                                        requireContext(),
                                        this@HomeFragment
                                    )

                                    val horizontaLayoutManagaer =
                                        LinearLayoutManager(
                                            requireContext(),
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                    fragmentHomeBinding.recTrendingNow.layoutManager =
                                        horizontaLayoutManagaer
                                    fragmentHomeBinding.recTrendingNow.adapter = trendingNowAdapter
                                    trendingNowAdapter.updateData(trendingSongsArray)
                                }

                                val newAlbumSongsArr = data.newAlbum
                                for (i in 0 until newAlbumSongsArr!!.size) {

                                    /*    val j = i + 1
                                        val song_id = newAlbumSongsArr[i].song_id
                                        val title = newAlbumSongsArr[i].title
                                        val description = if(data.newAlbum!![i].description == null ) ""
                                        else newAlbumSongsArr[i].description
                                        val audioLocation =
                                            if (data.newAlbum!![i].audio_location == null) ""
                                            else data.newAlbum!![i].audio_location

                                        val thumbnail = newAlbumSongsArr[i].thumbnail
                                        val commonDataModel = CommonDataModel1(
                                            j,
                                            song_id.toString().toInt(),
                                            title,
                                            description,
                                            thumbnail,
                                            audioLocation
                                        )
                                        var newAlbum =
                                            NewAlbum(song_id.toString().toInt(), title, thumbnail)*/
                                    var data = DataX(
                                        data.newAlbum!![i].categoryId,
                                        data.newAlbum!![i].description,
                                        data.newAlbum!![i].palbumId,
                                        data.newAlbum!![i].thumbnail,
                                        data.newAlbum!![i].title,
                                        data.newAlbum!![i].userId

                                    )

                                    newAlbumsSongsArray.add(data)
                                }
                                if (data.newAlbum!!.isNotEmpty()) {
                                    fragmentHomeBinding.tvNewAlbum.visibility = View.VISIBLE
                                    fragmentHomeBinding.newAlbumsBtn.visibility = View.VISIBLE
                                    newAlbumsAdapter = NewAlbumsAdapter(
                                        requireContext(),
                                        this@HomeFragment
                                    )

                                    val horizontaLayoutManagaer =
                                        LinearLayoutManager(
                                            requireContext(),
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                    fragmentHomeBinding.recNewAlbums.layoutManager =
                                        horizontaLayoutManagaer
                                    fragmentHomeBinding.recNewAlbums.adapter = newAlbumsAdapter
                                    newAlbumsAdapter.updateData(data.newAlbum!!)
                                } else {
                                    fragmentHomeBinding.tvNewAlbum.visibility = View.GONE
                                    fragmentHomeBinding.newAlbumsBtn.visibility = View.GONE
                                }

                                val recomendedSongsArr = data.recomended
                                Log.d("hgcvhgvhgv", recomendedSongsArr?.size.toString())
                                for (i in 0 until recomendedSongsArr!!.size) {

                                    val j = i + 1
                                    val song_id = recomendedSongsArr[i].song_id
                                    val title = recomendedSongsArr[i].title
                                    val description =
                                        if (recomendedSongsArr[i].description == null) "" else recomendedSongsArr[i].description

                                    //val description = recomendedSongsArr[i].description
                                    val audioLocation =
                                        if (data.recomended!![i].audio_location == null) "" else data.recomended!![i].audio_location
                                    val thumbnail = recomendedSongsArr[i].thumbnail
                                    val views_count = recomendedSongsArr[i].views_count
                                    val like_count = recomendedSongsArr[i].like_count
                                    val play_count = recomendedSongsArr[i].play_count
                                    var artist_name = recomendedSongsArr[i].artists_name
                                    val playlistId = recomendedSongsArr[i].playlistId
                                    val requestId = recomendedSongsArr[i].request_id
                                    val isLiked = recomendedSongsArr[i].is_liked

                                    val commonDataModel = CommonDataModel1(
                                        j,
                                        song_id.toString().toInt() ?: 0,
                                        title ?: "",
                                        description ?: "",
                                        thumbnail ?: "",
                                        audioLocation ?: "",
                                        views_count ?: 0,
                                        like_count ?: 0,
                                        play_count ?: 0,
                                        artist_name ?: emptyList(),
                                        playlistId ?: "",
                                        requestId ?: "",
                                        isLiked ?: 0


                                    )
                                    var recomended =
                                        Recomended(song_id.toString().toInt(), title, thumbnail)
                                    recommenddedArray.add(commonDataModel)
                                }
                                if (recomendedSongsArr.size > 0) {
                                    fragmentHomeBinding.tvRecommended.visibility = View.VISIBLE
                                    fragmentHomeBinding.btnrecommended.visibility = View.VISIBLE
                                    recomendedAdapter = RecommendedAdapter(
                                        requireContext(),
                                        this@HomeFragment
                                    )

                                    val horizontaLayoutManagaer =
                                        LinearLayoutManager(
                                            requireContext(),
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                    fragmentHomeBinding.recRecomended.layoutManager =
                                        horizontaLayoutManagaer
                                    fragmentHomeBinding.recRecomended.adapter = recomendedAdapter
                                    recomendedAdapter.updateData(recomendedSongsArr)
                                } else {
                                    fragmentHomeBinding.tvRecommended.visibility = View.GONE
                                    fragmentHomeBinding.btnrecommended.visibility = View.GONE
                                }

                                val popular_weekSongsArr = data.popularWeek
                                for (i in 0 until popular_weekSongsArr!!.size) {


                                    val j = i + 1
                                    val song_id = popular_weekSongsArr[i].song_id
                                    val title = popular_weekSongsArr[i].title
                                    // val description = popular_weekSongsArr[i].description
                                    val description =
                                        if (popular_weekSongsArr[i].description == null) "" else popular_weekSongsArr[i].description

                                    val audioLocation =
                                        if (data.popularWeek!![i].audio_location == null) "" else data.popularWeek!![i].audio_location
                                    val thumbnail = popular_weekSongsArr[i].thumbnail
                                    val views_count = popular_weekSongsArr[i].views_count
                                    val like_count = popular_weekSongsArr[i].like_count
                                    val play_count = popular_weekSongsArr[i].play_count
                                    var artist_name = popular_weekSongsArr[i].artists_name
                                    val playlistId = popular_weekSongsArr[i].playlistId
                                    val requestId = popular_weekSongsArr[i].request_id
                                    val isLiked = popular_weekSongsArr[i].is_liked

                                    val commonDataModel = CommonDataModel1(
                                        j,
                                        song_id.toString().toInt() ?: 0,
                                        title ?: "",
                                        description ?: "",
                                        thumbnail ?: "",
                                        audioLocation ?: "",
                                        views_count ?: 0,
                                        like_count ?: 0,
                                        play_count ?: 0, artist_name ?: emptyList(),
                                        playlistId ?: "",
                                        requestId ?: "",
                                        isLiked ?: 0
                                    )
                                    var popularWeek = Popular_Week(
                                        song_id.toString().toInt(),
                                        title,
                                        thumbnail
                                    )
                                    popular_weekArray.add(commonDataModel)
                                }
                                if (popular_weekArray.size > 0) {
                                    fragmentHomeBinding.tvMostpopular.visibility = View.VISIBLE
                                    fragmentHomeBinding.popularWeekBtn.visibility = View.VISIBLE
                                    popularWeekAdapter = Popular_week_Adapter(
                                        requireContext(),
                                        this@HomeFragment
                                    )

                                    val horizontaLayoutManagaer =
                                        LinearLayoutManager(
                                            requireContext(),
                                            LinearLayoutManager.HORIZONTAL,
                                            false
                                        )
                                    fragmentHomeBinding.recPopularWeek.layoutManager =
                                        horizontaLayoutManagaer
                                    fragmentHomeBinding.recPopularWeek.adapter = popularWeekAdapter
                                    popularWeekAdapter.updateData(popular_weekArray)
                                } else {
                                    fragmentHomeBinding.tvMostpopular.visibility = View.GONE
                                    fragmentHomeBinding.popularWeekBtn.visibility = View.GONE
                                }
                            }


                            /* Log.v("responseannouncement-->", result)
                             val mjonsresponse = JSONObject(result)
                             val data = mjonsresponse.getJSONObject("data")
                             val popuplarArr = data.getJSONArray("popular")*/


                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<DashboardResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(requireContext(), "OOPS! No Internet Connection", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        mainActivity.mBottomNavigationView.visibility = View.VISIBLE
        val imm = mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.hideSoftInputFromWindow(requireView().windowToken, 0)
        //getDashboardData()
    }

    @SuppressLint("SuspiciousIndentation")
    fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(requireContext())
            mProgressDialog!!.setMessage("Loading...")
            mProgressDialog!!.isIndeterminate = true
        }
        if (!mainActivity.isFinishing)
            mProgressDialog!!.show()
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }

    private val sliderRunnable =
        Runnable {
            fragmentHomeBinding.viewpager.currentItem =
                fragmentHomeBinding.viewpager.currentItem + 1
        }

    override fun onClick(modelData: List<CommonDataModel1>, position: Int, type: String) {

        if (type == "Play_Song") {
            playerViewModel.startSongModel1.value = PlayerDataModel1(1, position, modelData)
        } else if (type == "Add_To_Playlist") {
            showAddToPlayListDialog(modelData[position].song_id.toString())
            // addtoPlaylist(modelData,position)
        } else if (type == "Like_Song") {
            likeSong(modelData, position)
        } else {
            shareAppLink(modelData[position].title, modelData[position].audio_location)
        }


        //stopPlaying()
        /* if (!dialog.isShowing) {
             nextTrackCount = 0
             showSong(modelData[position], modelData, position, modelData.size)
         }*/

    }

    private fun likeSong(modelData: List<CommonDataModel1>, position: Int) {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

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
                                        mainActivity,
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
            Toast.makeText(mainActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }

    }

    private fun shareAppLink(subject: String, message: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            var shareMessage = "\nLet me recommend you this application\n\n"
            // shareMessage = "$shareMessage ${message}\n\n"
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


    lateinit var playlistAdapter: PlayListCreatedAdapter
    var dismissDialog: Dialog? = null
    private fun showAddToPlayListDialog(song_id: String) {
        val dialog = Dialog(mainActivity, R.style.DialogSlideAnim)

        dialog.getWindow()?.setBackgroundDrawableResource(R.color.greywhite)
        dialog.getWindow()
            ?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        val binding: DialogAddToPlaylistBinding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.dialog_add_to_playlist, null, false
        )
        dialog.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dismissDialog = dialog
        dialog.show()
        with(binding) {

            playlistAdapter =
                PlayListCreatedAdapter(context = mainActivity, onItemClickListener = object :
                    PlayListCreatedAdapter.OnPlaylistItemClickListener {
                    override fun onClick(
                        modelData: List<com.onlinemusic.wemu.responseModel.playlistitems.response.DataX>,
                        position: Int
                    ) {
                        addtoPlaylist(modelData, position, song_id)
                        dialog.dismiss()
                    }

                })
            var mLayoutManager = GridLayoutManager(mainActivity, 2)

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


    private fun loadMorePlayListItems(isFirstPage: Boolean) {
        // change loading state
        mIsLoading = true
        mCurrentPlayListPage += 1

        // update recycler adapter with next page
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

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
                                            requireContext(),
                                            "no data found",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        hideProgressDialog()
                                        if (dismissDialog != null) {
                                            dismissDialog?.dismiss()
                                        }
                                        Toast.makeText(
                                            requireContext(),
                                            "Please create a playlist first!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                }

                            } else {
                                mIsLoading = false
                                hideProgressDialog()
                                Toast.makeText(
                                    requireContext(),
                                    "no data found",
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
            Toast.makeText(mainActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
        /*apiEndpoint.getPagedList(mCurrentPage).enqueue(object : Callback<PagedList<Any?>?> {
            override fun onResponse(
                call: Call<PagedList<Any?>?>?,
                response: Response<PagedList<Any?>?>
            ) {
                val result: PagedList<Any> = response.body()
                if (result == null) return else if (!isFirstPage) mAdapter.addAll(result.getResults()) else mAdapter.setList(
                    result.getResults()
                )
                mIsLoading = false
                mIsLastPage = mCurrentPage == result.getTotalPages()
            }

            override fun onFailure(call: Call<PagedList<Any?>?>, t: Throwable) {
                Log.e("SomeActivity", t.message!!)
            }
        })*/
    }

    private fun addtoPlaylist(
        modelData: List<com.onlinemusic.wemu.responseModel.playlistitems.response.DataX>,
        position: Int,
        song_id: String
    ) {
        if (CheckConnectivity.getInstance(mainActivity).isOnline) {

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
                                        mainActivity,
                                        "Successfully added into playlist",
                                        Toast.LENGTH_SHORT
                                    ).show()


                                } else {
                                    Toast.makeText(
                                        mainActivity,
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
            Toast.makeText(mainActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }


    //OpenPopup

    var nextTrackCount = 0
    var isPlayLoop = false
    var isPlayRand = false


    private fun showSong(
        modelData: CommonDataModel1,
        modelDataList: List<CommonDataModel1>,
        mPosition: Int,
        totalLength: Int, trackChangeFromPlayer: Boolean = false
    ) {
        if (modelData.audio_location.isNullOrEmpty()) {
            Toast.makeText(mainActivity, "No audio link provided", Toast.LENGTH_SHORT).show()
            return
        }
        val layoutInflater =
            mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layerBinding = SongsBottomsheetBinding.inflate(layoutInflater, null, false)

        var position = mPosition
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                mainActivity.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            mainActivity.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
        }

        var isFavourate = false

        with(layerBinding) {
            pbLoading.visibility = View.VISIBLE
            ivPlayIcon.visibility = View.GONE
            var x = 0
            var b = false
            songsTitle.text = modelData.title
            tvTitle.text = modelData.title
            tvSongDesc.text = modelData.description
            tvDesc.text = modelData.description
            songsTitle.isSelected = true
            tvSongDesc.isSelected = true
            tvDesc.isSelected = true
            tvTitle.isSelected = true
            Glide.with(mainActivity)
                .load(modelData.thumbnail)
                .timeout(6000)
                .error(R.drawable.logo)
                .placeholder(R.drawable.logo)
                .into(ivThumbnail)
            Glide.with(mainActivity)
                .load(modelData.thumbnail)
                .timeout(6000)
                .error(R.drawable.logo)
                .placeholder(R.drawable.logo)
                .into(ivLargeThumbnail)

            isPLAYING = false
            stopPlaying()
            if (!trackChangeFromPlayer) {
                rlBottomActivity.visibility = View.VISIBLE
                rlFullScreenActivity.visibility = View.GONE
                (dialog).behavior.peekHeight = 200
            } else {
                rlBottomActivity.visibility = View.GONE
                rlFullScreenActivity.visibility = View.VISIBLE
                //  (dialog).behavior.peekHeight = getScreenWidth(mainActivity as Activity)
                (dialog).behavior.peekHeight = getScreenHeight()

            }
            dialog.setContentView(layerBinding.root)
            if (!dialog.isShowing)
                dialog.show()

            ivPlayIcon.background =
                ContextCompat.getDrawable(mainActivity, R.drawable.ic_pause)
            dialog.behavior.setBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        rlBottomActivity.visibility = View.VISIBLE
                        rlFullScreenActivity.visibility = View.GONE
                        (dialog).behavior.peekHeight = 200
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }

            })

            dialog.setOnDismissListener(object : DialogInterface.OnDismissListener {
                override fun onDismiss(p0: DialogInterface?) {
                    p0.apply {

                        rlBottomActivity.visibility = View.VISIBLE
                        rlFullScreenActivity.visibility = View.GONE
                        (dialog).behavior.peekHeight = 200
                        stopPlaying()
                    }
                }

            })

            val mHandler = Handler(Looper.getMainLooper())
            //Seekbar on UI thread

            mainActivity.runOnUiThread(object : Runnable {
                override fun run() {
                    val mCurrentPosition: Int = PlayerInstance().currentPosition / 1000
                    val mCurrentPositionTxt =
                        DateUtils.formatElapsedTime((PlayerInstance().currentPosition / 1000).toLong())
                    seekbarPlay.progress = mCurrentPosition
                    seekbarPlay.max = PlayerInstance().duration / 1000

                    val durationText =
                        DateUtils.formatElapsedTime((PlayerInstance().duration / 1000).toLong()) // converting time in millis to minutes:second format eg 14:15 min


                    tvNextTime.text = durationText.toString()
                    tvPrevTime.text = mCurrentPositionTxt.toString()

                    try {
                        if (PlayerInstance().isPlaying) {
                            pbLoading.visibility = View.GONE
                            ivPlayIcon.visibility = View.VISIBLE
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    if (animationView.isAnimating)
                        animationView.visibility = View.VISIBLE
                    else
                        animationView.visibility = View.GONE
                    mHandler.postDelayed(this, 1000)
                }
            })

            ivPrevTrack.setOnClickListener {
                if (position > 0) {

                    if (isPlayRand)
                        position = (modelDataList.indices).random()
                    else
                        position--
                    showSong(modelDataList[position], modelDataList, position, totalLength, true)
                    return@setOnClickListener
                } else {

                    Toast.makeText(
                        mainActivity,
                        "No previous song available",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener

                }
            }
            if (isPlayLoop) {
                ivLoopSong.setColorFilter(
                    ContextCompat.getColor(mainActivity, R.color.textyellow),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                ivLoopSong.setColorFilter(
                    ContextCompat.getColor(mainActivity, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            if (isPlayRand) {
                ivRandSong.setColorFilter(
                    ContextCompat.getColor(mainActivity, R.color.textyellow),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                ivRandSong.setColorFilter(
                    ContextCompat.getColor(mainActivity, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            ivNextTrack.setOnClickListener {
                if (position < totalLength) {
                    if (nextTrackCount == 1) {
                        Toast.makeText(
                            mainActivity,
                            "Pay for premium version to proceed further",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    } else
                        nextTrackCount++

                    if (isPlayRand)
                        position = (modelDataList.indices).random()
                    else
                        position++
                    showSong(modelDataList[position], modelDataList, position, totalLength, true)
                    return@setOnClickListener
                } else {
                    Toast.makeText(
                        mainActivity,
                        "No next song available",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener


                }
            }

            ivFavourate.setOnClickListener {
                if (isFavourate) {
                    ivFavourate.setImageResource(R.drawable.ic_heart)
                    isFavourate = false
                } else {
                    animationView.playAnimation()
                    animationView.visibility = View.VISIBLE
                    ivFavourate.setImageResource(R.drawable.ic_heart_red)
                    isFavourate = true
                }
                /*Handler(Looper.getMainLooper()).postDelayed({
                    animationView.pauseAnimation()
                    animationView.
                }, 2000)*/
            }
            seekbarPlay.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        PlayerInstance().seekTo(progress * 1000)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            })

            rlBottomActivity.setOnClickListener {
                //Toast.makeText(context, "PRESSED", Toast.LENGTH_SHORT).show()
                rlBottomActivity.visibility = View.GONE
                rlFullScreenActivity.visibility = View.VISIBLE
                //  (dialog).behavior.peekHeight = getScreenWidth(mainActivity as Activity)
                (dialog).behavior.peekHeight = getScreenHeight()
            }


            btnBack.setOnClickListener {
                // Toast.makeText(context, "PRESSED", Toast.LENGTH_SHORT).show()
                rlBottomActivity.visibility = View.VISIBLE
                rlFullScreenActivity.visibility = View.GONE
                (dialog).behavior.peekHeight = 200
            }

            ivPlayPause.setOnClickListener {
                println("SEEK BAR   ${mp!!.currentPosition}")
                PlayerInstance().let { itMP ->

                    vibrate(vib)
                    if (b) {
                        itMP.seekTo(x)
                        itMP.start()
                        ivPlayIcon.background =
                            ContextCompat.getDrawable(mainActivity, R.drawable.ic_pause)
                        ivPlayPause.background =
                            ContextCompat.getDrawable(mainActivity, R.drawable.pause_icon)
                        b = false
                    } else {
                        x = itMP.currentPosition
                        itMP.pause()
                        Log.v("log", "" + x)
                        ivPlayIcon.background =
                            ContextCompat.getDrawable(mainActivity, R.drawable.play)
                        ivPlayPause.background =
                            ContextCompat.getDrawable(mainActivity, R.drawable.play_icon)
                        b = true
                    }

                }
            }




            btnPlayPause.setOnClickListener {
                println("SEEK BAR   ${mp!!.currentPosition}")
                PlayerInstance().let { itMP ->

                    vibrate(vib)
                    if (b) {
                        itMP.seekTo(x)
                        itMP.start()
                        ivPlayIcon.background =
                            ContextCompat.getDrawable(mainActivity, R.drawable.ic_pause)
                        ivPlayPause.background =
                            ContextCompat.getDrawable(mainActivity, R.drawable.pause_icon)
                        b = false
                    } else {
                        x = itMP.currentPosition
                        itMP.pause()
                        Log.v("log", "" + x)
                        ivPlayIcon.background =
                            ContextCompat.getDrawable(mainActivity, R.drawable.play)
                        ivPlayPause.background =
                            ContextCompat.getDrawable(mainActivity, R.drawable.play_icon)
                        b = true
                    }

                }
            }

            ivLoopSong.setOnClickListener {
                if (isPlayLoop) {
                    isPlayLoop = false
                    ivLoopSong.setColorFilter(
                        ContextCompat.getColor(mainActivity, R.color.white),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )

                } else {
                    isPlayLoop = true
                    ivLoopSong.setColorFilter(
                        ContextCompat.getColor(
                            mainActivity,
                            R.color.textyellow
                        ), android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
            }

            ivRandSong.setOnClickListener {

                if (isPlayRand) {
                    isPlayRand = false
                    ivRandSong.setColorFilter(
                        ContextCompat.getColor(mainActivity, R.color.white),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )

                } else {
                    isPlayRand = true
                    ivRandSong.setColorFilter(
                        ContextCompat.getColor(mainActivity, R.color.textyellow),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
            }

            ivPlayPause.background =
                ContextCompat.getDrawable(mainActivity, R.drawable.pause_icon)

            ivPlayIcon.background =
                ContextCompat.getDrawable(mainActivity, R.drawable.ic_pause)

            println(modelData.audio_location)
            playSongs(modelData.audio_location)

            PlayerInstance().setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
                override fun onCompletion(p0: MediaPlayer?) {

                    if (position < totalLength) {

                        if (!isPlayLoop) {
                            if (isPlayRand)
                                position = (modelDataList.indices).random()
                            else
                                position++
                        }

                        showSong(
                            modelDataList[position],
                            modelDataList,
                            position,
                            totalLength,
                            true
                        )

                    }
                }

            })

        }
        // RelativeLayout headerLay = (RelativeLayout) dialog.findViewById(R.id.addTask_dialog_header);

    }

    fun getScreenHeight(): Int {
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
    }

    fun vibrate(vib: Vibrator) {

// Vibrate for 500 milliseconds
// Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            vib.vibrate(200)
        }
    }

    var isPLAYING = false

    var mp: MediaPlayer? = null

    fun PlayerInstance(): MediaPlayer {
        if (mp == null)
            mp = MediaPlayer()
        return mp!!
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

    private fun stopPlaying() {
        if (mp != null && mp!!.isPlaying) {
            mp?.reset()
            mp = null
        }
    }

}