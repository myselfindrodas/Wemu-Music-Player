package com.onlinemusic.wemu.responseModel.albumdetails.response


import com.google.gson.annotations.SerializedName

data class AlbumDetailsResponseModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)