package com.onlinemusic.wemu.responseModel

data class CommonDataModel(var id:Int,var song_id:Int,var title:String,var description:String,var thumbnail:String,var audioLocation:String)
data class CommonDataModel1(var id:Int = 0,var song_id:Int = 0,var title:String ="",var description:String="",var thumbnail:String="",var audio_location:String=""
                            ,var views_count:Int = 0,var like_count:Int = 0,var play_count:Int = 0,var artists_name :List<String> = emptyList<String>(),var playlistId :String= "",var request_id :String= "",var is_liked :Int= 0)
data class ArtistName(var name : String)
