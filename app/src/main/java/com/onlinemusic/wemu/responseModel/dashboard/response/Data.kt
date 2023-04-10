package com.onlinemusic.wemu.responseModel.dashboard.response

import com.onlinemusic.wemu.responseModel.albumslist.response.DataX
import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.google.gson.annotations.SerializedName

data class Data(
  /*  @SerializedName("new_album")
    val newAlbum: List<CommonDataModel1>?,*/

     @SerializedName("is_pro")
    val is_pro: String,

     @SerializedName("new_album")
    val newAlbum: List<DataX>?,


    @SerializedName("new_release")
    val newRelease: List<CommonDataModel1>?,
    @SerializedName("popular")
    val popular: List<CommonDataModel1>?,
    @SerializedName("popular_week")
    val popularWeek: List<CommonDataModel1>?,
    @SerializedName("recently_played")
    val recentlyPlayed: List<CommonDataModel1>?,
    @SerializedName("recomended")
    val recomended: List<CommonDataModel1>?,
    @SerializedName("trending_now")
    val trendingNow: List<CommonDataModel1>?,
    @SerializedName("newBanner")
    val bannerData: List<BannerData>?

)
data class BannerData(var id:Int = 0,var banner_image:String ="",var song_id:String = "",
                      var album_id:String = "",var url :String ="")
