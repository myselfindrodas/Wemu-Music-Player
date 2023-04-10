package com.onlinemusic.wemu.responseModel.playlistitems.response


import com.google.gson.annotations.SerializedName

data class DataX(
    @SerializedName("created_at")
    val createdAt: Any?,
    @SerializedName("deleted_at")
    val deletedAt: Any?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("privacy")
    val privacy: Int?,
    @SerializedName("thumbnail")
    val thumbnail: String?,
    @SerializedName("time")
    val time: Int?,
    @SerializedName("uid")
    val uid: String?,
    @SerializedName("updated_at")
    val updatedAt: Any?,
    @SerializedName("user_id")
    val userId: Int?
)