package com.onlinemusic.wemu.responseModel.search.response


import com.google.gson.annotations.SerializedName

data class SearchResponseModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)