package com.onlinemusic.wemu.responseModel.version_check.version_response


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("device")
    val device: Int?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("update_type")
    val updateType: Int?,
    @SerializedName("updated_at")
    val updatedAt: Any?,
    @SerializedName("version")
    val version: String?
)