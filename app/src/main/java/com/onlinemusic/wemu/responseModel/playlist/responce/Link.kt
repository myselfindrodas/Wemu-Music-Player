package com.onlinemusic.wemu.responseModel.playlist.responce


import com.google.gson.annotations.SerializedName

data class Link(
    @SerializedName("active")
    val active: Boolean?,
    @SerializedName("label")
    val label: String?,
    @SerializedName("url")
    val url: String?
)