package com.onlinemusic.wemu.responseModel.add_to_playlist

import com.google.gson.annotations.SerializedName

data class AddToPlaylistRequestModel(

    @SerializedName("track_id")
    val track_id: Int?,
    @SerializedName("playlist_id")
    val playlist_id: Int?
)
