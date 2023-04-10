package com.onlinemusic.wemu.responseModel.playlist.responce


import com.google.gson.annotations.SerializedName

data class PlaylistResponseMainModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)