package com.onlinemusic.wemu.responseModel.favourites.response

import com.google.gson.annotations.SerializedName
import com.onlinemusic.wemu.responseModel.CommonDataModel1

data class DataFavourites(
    @SerializedName("current_page")
    val currentPage: Int?,
    @SerializedName("data")
    val `data`: List<CommonDataModel1>?,

    @SerializedName("last_page")
    val lastPage: Int?,
    @SerializedName("last_page_url")
    val lastPageUrl: String?,

    @SerializedName("next_page_url")
    val nextPageUrl: Any?,
    @SerializedName("path")
    val path: String?,
    @SerializedName("per_page")
    val perPage: Int?,
    @SerializedName("prev_page_url")
    val prevPageUrl: Any?,

    @SerializedName("total")
    val total: Int?
)
