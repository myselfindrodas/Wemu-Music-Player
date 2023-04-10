package com.onlinemusic.wemu.responseModel.genres.response


import com.google.gson.annotations.SerializedName

data class DataX(
    @SerializedName("background_thumb")
    val backgroundThumb: String? ="",
     @SerializedName("thumbnail")
    val thumbnail: String?="",


    @SerializedName("cateogry_name")
    val cateogryName: String?,
    @SerializedName("color")
    val color: String?,
    @SerializedName("created_at")
    val createdAt: Any?,
    @SerializedName("deleted_at")
    val deletedAt: Any?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("time")
    val time: Int?,
    @SerializedName("tracks")
    val tracks: Int?,
    @SerializedName("updated_at")
    val updatedAt: Any?
)