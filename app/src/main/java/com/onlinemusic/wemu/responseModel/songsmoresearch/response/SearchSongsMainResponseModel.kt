package com.onlinemusic.wemu.responseModel.songsmoresearch.response


import com.google.gson.annotations.SerializedName

data class SearchSongsMainResponseModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)