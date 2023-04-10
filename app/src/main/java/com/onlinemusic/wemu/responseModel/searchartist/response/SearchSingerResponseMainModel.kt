package com.onlinemusic.wemu.responseModel.searchartist.response


import com.google.gson.annotations.SerializedName

data class SearchSingerResponseMainModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)