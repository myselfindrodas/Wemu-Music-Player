package com.onlinemusic.wemu.responseModel.artist.songsdetails.response


import com.google.gson.annotations.SerializedName

data class ArtistAlbumResponseModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)