package com.onlinemusic.wemu.responseModel.searchartist.response


import com.google.gson.annotations.SerializedName

data class SingerList(
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("cover")
    val cover: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?
)