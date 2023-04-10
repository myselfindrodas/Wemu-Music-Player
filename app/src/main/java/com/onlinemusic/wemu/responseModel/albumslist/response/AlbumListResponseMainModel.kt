package com.onlinemusic.wemu.responseModel.albumslist.response


import com.google.gson.annotations.SerializedName

data class AlbumListResponseMainModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)