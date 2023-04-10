package com.onlinemusic.wemu.responseModel.version_check.version_response


import com.google.gson.annotations.SerializedName

data class VersionCheckResponse(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)