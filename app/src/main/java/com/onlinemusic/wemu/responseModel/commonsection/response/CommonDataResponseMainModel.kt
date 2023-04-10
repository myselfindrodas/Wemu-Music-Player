package com.onlinemusic.wemu.responseModel.commonsection.response


import com.google.gson.annotations.SerializedName

data class CommonDataResponseMainModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)