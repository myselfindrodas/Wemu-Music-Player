package com.onlinemusic.wemu.responseModel.genres.response


import com.google.gson.annotations.SerializedName

data class GenresDataResponseModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)