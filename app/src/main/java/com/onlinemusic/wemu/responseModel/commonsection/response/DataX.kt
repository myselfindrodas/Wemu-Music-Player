package com.onlinemusic.wemu.responseModel.commonsection.response


import com.google.gson.annotations.SerializedName

data class DataX(
    @SerializedName("artist_id")
    val artistId: String?,
    @SerializedName("artists_name")
    val artistsName: List<Any?>?,
    @SerializedName("audio_location")
    val audioLocation: String?,
    @SerializedName("category")
    val category: Category?,
    @SerializedName("category_id")
    val categoryId: Int?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("like_count")
    val likeCount: Int?,
    @SerializedName("play_count")
    val playCount: Int?,
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
    val users: Users?,
    @SerializedName("views_count")
    val viewsCount: Int?
)