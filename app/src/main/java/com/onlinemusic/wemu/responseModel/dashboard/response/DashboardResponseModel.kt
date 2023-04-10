package com.onlinemusic.wemu.responseModel.dashboard.response


import com.google.gson.annotations.SerializedName

data class DashboardResponseModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)