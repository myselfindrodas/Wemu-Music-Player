package com.onlinemusic.wemu.responseModel.searchcategory.response


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("albums")
    val albums: List<Album>?,
    @SerializedName("artists")
    val artists: List<Artist>?,
    @SerializedName("songs")
    val songs: Songs?
)