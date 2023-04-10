package com.onlinemusic.wemu.musicplayerdemo

import com.onlinemusic.wemu.MainActivity

data class MusicSetGet(var songsTitle:String,var songsUrl:String)
fun setSongPosition(increment:Boolean)
{
    if(increment)
    {
        if(MainActivity.arrayList.size -1 == MainActivity.itemPosition)
        {
            MainActivity.itemPosition = 0
        }
        else
        {
            MainActivity.itemPosition.inc()
        }
        MainActivity.songUrl = MainActivity.arrayList[MainActivity.itemPosition].audio_location
        MainActivity.name = MainActivity.arrayList[MainActivity.itemPosition].title
    }
    else
    {
        if(0 == MainActivity.itemPosition)
        {
            MainActivity.itemPosition = MainActivity.arrayList.size - 1

        }
        else
        {
            MainActivity.itemPosition.dec()
        }
        MainActivity.songUrl = MainActivity.arrayList[MainActivity.itemPosition].audio_location
        MainActivity.name = MainActivity.arrayList[MainActivity.itemPosition].title
    }

}