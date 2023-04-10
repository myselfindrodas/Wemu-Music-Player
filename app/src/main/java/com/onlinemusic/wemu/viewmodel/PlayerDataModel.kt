package com.onlinemusic.wemu.viewmodel

import com.onlinemusic.wemu.responseModel.CommonDataModel

data class PlayerDataModel(val isPlaying:Int, val position:Int,
                           val playerModel: List<CommonDataModel>)
