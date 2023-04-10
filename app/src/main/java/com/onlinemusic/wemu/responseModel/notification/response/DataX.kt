package com.onlinemusic.wemu.responseModel.notification.response


import com.google.gson.annotations.SerializedName

data class DataX(
    @SerializedName("comment_id")
    val commentId: Int?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("notification_msg")
    val notificationMsg: String?,
    @SerializedName("notifier_id")
    val notifierId: Int?,
    @SerializedName("notify_by")
    val notifyBy: String?,
    @SerializedName("recipient_id")
    val recipientId: Int?,
    @SerializedName("seen")
    val seen: String?,
    @SerializedName("sent_push")
    val sentPush: Int?,
    @SerializedName("text")
    val text: Any?,
    @SerializedName("time")
    val time: String?,
    @SerializedName("track_id")
    val trackId: Int?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("url")
    val url: String?
)