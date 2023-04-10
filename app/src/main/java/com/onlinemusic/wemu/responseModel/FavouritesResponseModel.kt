package com.onlinemusic.wemu.responseModel

import com.google.gson.annotations.SerializedName

data class FavouritesResponseModel(

    @SerializedName("status")
    val status: Boolean?,
    @SerializedName("message")
    val message: String?,
)
