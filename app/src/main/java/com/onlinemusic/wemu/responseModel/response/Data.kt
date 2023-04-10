package com.onlinemusic.wemu.responseModel.genredetails.response


import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("albums")
    val albums: List<Any?>?,
    @SerializedName("genre")
    val genre: Genre?,
   /* @SerializedName("songs")
    val songs: List<Song?>?*/
     @SerializedName("songs")
    val songs: List<CommonDataModel1?>?

)