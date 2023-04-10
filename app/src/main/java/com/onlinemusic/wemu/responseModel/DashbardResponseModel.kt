package com.onlinemusic.wemu



data class DashbardResponseModel(var status: Boolean,var message:String,var data:DashboardResponse)
data class DashboardResponse(var popular:ArrayList<PopularModelResponse>,var recently_played:ArrayList<RecentlyPlayednew>,
var new_release : ArrayList<NewRelease>,var trending_now :ArrayList<TrendingNow>,var new_album:ArrayList<NewAlbum>,
var recomended:ArrayList<Recomended>,var popular_week: ArrayList<Popular_Week>)

data class  PopularModelResponse(var song_id:Int,var title: String,var thumbnail:String)
data class  RecentlyPlayednew(var song_id:Int,var title: String,var thumbnail:String)
data class  NewRelease(var song_id:Int,var title: String,var thumbnail:String)
data class  TrendingNow(var song_id:Int,var title: String,var thumbnail:String)
data class  NewAlbum(var palbum_id:Int,var title: String,var thumbnail:String)
data class  Recomended(var song_id:Int,var title: String,var thumbnail:String)
data class  Popular_Week(var song_id:Int,var title: String,var thumbnail:String)
