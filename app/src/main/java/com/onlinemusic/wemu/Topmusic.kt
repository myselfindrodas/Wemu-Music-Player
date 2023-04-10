package com.onlinemusic.wemu

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onlinemusic.wemu.adapter.TopmusicplayedAdapter
import com.onlinemusic.wemu.databinding.ActivityTopmusicBinding
import com.onlinemusic.wemu.responseModel.DemoMusicDataModel

class Topmusic : AppCompatActivity() {
    lateinit var topmusicBinding: ActivityTopmusicBinding

    var btnMusicPlayer: LinearLayout?=null

     val list :   ArrayList<DemoMusicDataModel> = arrayListOf()
    var mLayoutManager: RecyclerView.LayoutManager?=null
    lateinit var playedAdapter: TopmusicplayedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_topmusic)
        topmusicBinding = DataBindingUtil.setContentView(this, R.layout.activity_topmusic)

        /*topmusicBinding.btnMusicPlayer.setOnClickListener {

            val intent = Intent(this, Player::class.java)
            startActivity(intent)
        }*/
       // playedAdapter=TopmusicplayedAdapter(this)
        topmusicBinding.btnBack.setOnClickListener {
            onBackPressed()
        }

        setupRecyclewrView()
    }

    /*fun listRaw() {
        val fields: Array<Field> = R.raw::class.java.fields
        for (count in fields.indices) {
            Log.i("Raw Asset: ", fields[count].getName())
            loadThumbnail("android.resource://$packageName/" +fields[count].name)?.let {
                list.add(DemoMusicDataModel(fields[count].name,it))
            }
        }
        list.let {

        }
    }*/

    fun setupRecyclewrView(){
        with(topmusicBinding){
            mLayoutManager = GridLayoutManager(this@Topmusic, 1)
            songsList.layoutManager = mLayoutManager
            songsList.itemAnimator = DefaultItemAnimator()
            songsList.adapter = playedAdapter
        }
    }
    fun loadThumbnail(absolutePath: String?): Bitmap? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(absolutePath)
        val data: ByteArray? = retriever.embeddedPicture
        retriever.release()
        return if (data == null) {
            null
        } else BitmapFactory.decodeByteArray(data, 0, data.size)
    }
}