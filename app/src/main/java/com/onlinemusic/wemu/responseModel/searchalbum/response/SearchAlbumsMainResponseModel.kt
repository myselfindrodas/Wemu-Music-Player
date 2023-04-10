package com.onlinemusic.wemu.responseModel.searchalbum.response


import com.google.gson.annotations.SerializedName

data class SearchAlbumsMainResponseModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)