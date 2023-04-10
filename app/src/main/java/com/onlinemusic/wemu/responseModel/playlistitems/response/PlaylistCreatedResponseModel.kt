package com.onlinemusic.wemu.responseModel.playlistitems.response


import com.google.gson.annotations.SerializedName

data class PlaylistCreatedResponseModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)