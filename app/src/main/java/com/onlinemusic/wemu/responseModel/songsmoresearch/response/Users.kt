package com.onlinemusic.wemu.responseModel.songsmoresearch.response


import com.google.gson.annotations.SerializedName

data class Users(
    @SerializedName("email")
    val email: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("username")
    val username: String?
)