package com.onlinemusic.wemu.responseModel.searchcategory.response


import com.google.gson.annotations.SerializedName

data class SearchCategoryResponseModel(
    @SerializedName("data")
    val `data`: Data?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("status")
    val status: Boolean?
)