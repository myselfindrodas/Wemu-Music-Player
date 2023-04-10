package com.onlinemusic.wemu.responseModel.playlist_details_response


import com.google.gson.annotations.SerializedName

data class PlayListDetailsResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)