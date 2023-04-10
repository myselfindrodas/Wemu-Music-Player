package com.onlinemusic.wemu.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    val startSong: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val startSongModel: MutableLiveData<PlayerDataModel> by lazy {
        MutableLiveData<PlayerDataModel>()
    }
    val startSongModel1: MutableLiveData<PlayerDataModel1> by lazy {
        MutableLiveData<PlayerDataModel1>()
    }
    val getCurrentSongId: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
}