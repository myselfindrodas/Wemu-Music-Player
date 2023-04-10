package com.onlinemusic.wemu.responseModel.artist.response


import com.google.gson.annotations.SerializedName

data class ArtistMainResponseModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)