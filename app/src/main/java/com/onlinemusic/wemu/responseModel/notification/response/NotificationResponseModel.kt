package com.onlinemusic.wemu.responseModel.notification.response


import com.google.gson.annotations.SerializedName

data class NotificationResponseModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)