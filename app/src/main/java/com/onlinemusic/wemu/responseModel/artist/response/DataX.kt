package com.onlinemusic.wemu.responseModel.artist.response


import com.google.gson.annotations.SerializedName

data class DataX(
    @SerializedName("about")
    val about: String?,
    @SerializedName("active")
    val active: Int?,
    @SerializedName("admin")
    val admin: Int?,
    @SerializedName("age")
    val age: Int?,
    @SerializedName("android_device_id")
    val androidDeviceId: String?,
    @SerializedName("artist")
    val artist: Int?,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("balance")
    val balance: String?,
    @SerializedName("country_id")
    val countryId: Int?,
    @SerializedName("cover")
    val cover: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("deleted_at")
    val deletedAt: Any?,
    @SerializedName("dob")
    val dob: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("email_code")
    val emailCode: String?,
    @SerializedName("email_verified_at")
    val emailVerifiedAt: Any?,
    @SerializedName("facebook")
    val facebook: String?,
    @SerializedName("gender")
    val gender: String?,
    @SerializedName("gendernew")
    val gendernew: Any?,
    @SerializedName("google")
    val google: String?,
    @SerializedName("id")
    val id: Int?,
    @SerializedName("instagram")
    val instagram: String?,
    @SerializedName("ios_device_id")
    val iosDeviceId: String?,
    @SerializedName("ip_address")
    val ipAddress: String?,
    @SerializedName("is_pro")
    val isPro: Int?,
    @SerializedName("language")
    val language: String?,
    @SerializedName("last_active")
    val lastActive: Int?,
    @SerializedName("last_follow_id")
    val lastFollowId: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("partners")
    val partners: Int?,
    @SerializedName("payment_status")
    val paymentStatus: String?,
    @SerializedName("pro_time")
    val proTime: Int?,
    @SerializedName("registered")
    val registered: String?,
    @SerializedName("src")
    val src: String?,
    @SerializedName("subscription")
    val subscription: String?,
    @SerializedName("twitter")
    val twitter: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("uploads")
    val uploads: Int?,
    @SerializedName("username")
    val username: String?,
    @SerializedName("verified")
    val verified: Int?,
    @SerializedName("wallet")
    val wallet: String?,
    @SerializedName("web_device_id")
    val webDeviceId: String?,
    @SerializedName("website")
    val website: String?
)