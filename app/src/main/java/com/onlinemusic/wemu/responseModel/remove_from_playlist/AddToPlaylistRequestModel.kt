package com.onlinemusic.wemu.responseModel.remove_from_playlist

import com.google.gson.annotations.SerializedName

data class RemoveFromPlayListRequestModel(

    @SerializedName("id")
    val id: Int?
)
