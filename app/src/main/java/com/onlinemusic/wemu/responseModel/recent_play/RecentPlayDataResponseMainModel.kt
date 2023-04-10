package com.onlinemusic.wemu.responseModel.recent_play


import com.google.gson.annotations.SerializedName
import com.onlinemusic.wemu.responseModel.CommonDataModel1

data class RecentPlayDataResponseMainModel(
    @SerializedName("data")
    val `data`: List<CommonDataModel1>?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)