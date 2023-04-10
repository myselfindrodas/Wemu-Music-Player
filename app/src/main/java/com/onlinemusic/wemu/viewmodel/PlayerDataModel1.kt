package com.onlinemusic.wemu.viewmodel

import com.onlinemusic.wemu.responseModel.CommonDataModel1

data class PlayerDataModel1(val isPlaying:Int, val position:Int,
                            val playerModel: List<CommonDataModel1>)
