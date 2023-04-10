package com.onlinemusic.wemu.responseModel.album.response



import com.onlinemusic.wemu.responseModel.dashboard.response.NewAlbum
import com.google.gson.annotations.SerializedName

data class AlbumResponseModel(
    @SerializedName("data")
    val `data`: DataAlbum?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)

data class DataAlbum(
    @SerializedName("new_album")
    val newAlbum: List<NewAlbum>?
)