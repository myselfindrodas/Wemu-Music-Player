package com.onlinemusic.wemu.responseModel.favourites.response


import com.google.gson.annotations.SerializedName

data class FavouritesAddResponseModel(
    @SerializedName("status")
    val status: Boolean?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val `data`: DataFavourites?


)
