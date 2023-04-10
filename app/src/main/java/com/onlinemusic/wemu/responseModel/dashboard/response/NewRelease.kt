package com.onlinemusic.wemu.responseModel.dashboard.response


import com.google.gson.annotations.SerializedName

data class NewRelease(
    @SerializedName("audio_location")
    val audioLocation: String?,
    @SerializedName("category")
    val category: Category?,
    @SerializedName("category_id")
    val categoryId: Int?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("size")
    val size: Int?,
    @SerializedName("song_id")
    val songId: Int?,
    @SerializedName("tags")
    val tags: String?,
    @SerializedName("thumbnail")
    val thumbnail: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("user_id")
    val userId: Int?,
    @SerializedName("users")
    val users: Users?
)