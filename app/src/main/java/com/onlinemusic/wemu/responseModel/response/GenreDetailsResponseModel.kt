package com.onlinemusic.wemu.responseModel.genredetails.response


import com.google.gson.annotations.SerializedName

data class GenreDetailsResponseModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)