package com.onlinemusic.wemu.responseModel.albumslist.response


import com.google.gson.annotations.SerializedName

data class DataX(
    @SerializedName("category_id")
    val categoryId: Int?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("palbum_id")
    val palbumId: Int?,
    @SerializedName("thumbnail")
    val thumbnail: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("user_id")
    val userId: Int?
)