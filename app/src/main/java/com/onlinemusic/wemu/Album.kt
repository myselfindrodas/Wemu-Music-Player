package com.onlinemusic.wemu

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.onlinemusic.wemu.adapter.*
import com.onlinemusic.wemu.databinding.ActivityAlbumBinding

import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.dashboard.response.DashboardResponseModel
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit

class Album : AppCompatActivity() {

    lateinit var activityAlbumBinding: ActivityAlbumBinding
    lateinit var albumAdapter: AlbumsAdapter
    var sessionManager: SessionManager? = null
    var mProgressDialog: ProgressDialog? = null
    var arrayList : ArrayList<NewAlbum> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityAlbumBinding= DataBindingUtil.setContentView(this,R.layout.activity_album)
        sessionManager = SessionManager(this@Album)

       /* activityAlbumBinding.btnAlbumdetails.setOnClickListener {

            val intent = Intent(this, Albumdetails::class.java)
            startActivity(intent)
        }*/
        activityAlbumBinding.btnBack.setOnClickListener {

            onBackPressed()
        }
        getAlbumsData()
    }
    private fun getAlbumsData() {
        Log.d("dynamic","hfdhgchg")
        if (CheckConnectivity.getInstance(this@Album).isOnline) {

            showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<DashboardResponseModel?>? = uploadAPIs.getDashboardData("Bearer "+sessionManager?.getToken())
            mcall?.enqueue(object : Callback<DashboardResponseModel?> {
                override fun onResponse(
                    call: Call<DashboardResponseModel?>,
                    response: retrofit2.Response<DashboardResponseModel?>,
                ) {
                    try {
                        if (response.body() != null) {

                            val result = response.body()!!
                           /* Log.v("responseannouncement-->", result)
                            val mjonsresponse = JSONObject(result)
                            val data = mjonsresponse.getJSONObject("data")*/
                            result.apply {

                                val newAlbumSongsArr = data!!.newAlbum
                                for(i in  newAlbumSongsArr!!)
                                {

                                    var song_id = ""
                                    var title = i.title
                                    var thumbnail = i.thumbnail
                                    var newAlbum =NewAlbum(song_id.toString().toInt(),title!!,thumbnail!!)
                                    arrayList.add(newAlbum)
                                }
                                if(arrayList.size > 0)
                                {
                                   // albumAdapter = AlbumsAdapter(this@Album,arrayList)

                                    val mLayoutManager: RecyclerView.LayoutManager =
                                        GridLayoutManager(this@Album, 3)
                                    activityAlbumBinding.recAlbums.layoutManager = mLayoutManager
                                    activityAlbumBinding.recAlbums.itemAnimator = DefaultItemAnimator()
                                    activityAlbumBinding.recAlbums.setAdapter(albumAdapter)
                                    albumAdapter.notifyDataSetChanged()
                                }

                            }






                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    hideProgressDialog()
                }

                override fun onFailure(call: Call<DashboardResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(this@Album, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }
    fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(this@Album)
            mProgressDialog!!.setMessage("Loading...")
            mProgressDialog!!.isIndeterminate = true
        }
        if(!this.isFinishing)
        mProgressDialog!!.show()
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }
}