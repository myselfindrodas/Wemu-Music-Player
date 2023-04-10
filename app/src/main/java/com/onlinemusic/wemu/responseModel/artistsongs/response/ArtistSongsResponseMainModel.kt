package com.onlinemusic.wemu.responseModel.artistsongs.response


import com.google.gson.annotations.SerializedName

data class ArtistSongsResponseMainModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)