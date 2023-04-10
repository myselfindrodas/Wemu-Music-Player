package com.onlinemusic.wemu

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onlinemusic.wemu.adapter.SearchResultAdapter
import com.onlinemusic.wemu.databinding.ActivitySearchBinding
import com.onlinemusic.wemu.internet.CheckConnectivity
import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.onlinemusic.wemu.responseModel.search.response.SearchResponseModel
import com.onlinemusic.wemu.retrofit.ApiClient
import com.onlinemusic.wemu.retrofit.ApiInterface
import com.onlinemusic.wemu.session.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit

class SearchActivity : AppCompatActivity(), SearchResultAdapter.OnItemClickListener {


    lateinit var binding: ActivitySearchBinding
    var mProgressDialog: ProgressDialog? = null
    var sessionManager: SessionManager? = null
    var mLayoutManager: RecyclerView.LayoutManager?=null
    lateinit var searchAdapter: SearchResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_search)

        sessionManager = SessionManager(this@SearchActivity)
        searchAdapter = SearchResultAdapter(this@SearchActivity,this@SearchActivity)

        setupRecyclewrView()
        binding.btnBack.setOnClickListener {

            onBackPressed()
        }
        binding.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                getSearchData(newText.toString())
                return false
            }

        })


    }
    fun setupRecyclewrView(){
        with(binding){

            mLayoutManager = GridLayoutManager(this@SearchActivity, 1)
            recSearch.layoutManager = mLayoutManager
           // recSearch.addOnScrollListener(recyclerOnScroll)
            recSearch.itemAnimator = DefaultItemAnimator()
            recSearch.adapter = searchAdapter
        }
    }
    private fun getSearchData(keyword:String) {
        Log.d("dynamic","hfdhgchg")
        if (CheckConnectivity.getInstance(this@SearchActivity).isOnline) {

           // showProgressDialog()

            val retrofit: Retrofit = ApiClient.retrofitInstance!!
            val uploadAPIs: ApiInterface = retrofit.create(ApiInterface::class.java)
            val mcall: Call<SearchResponseModel?>? = uploadAPIs.getSearchData("Bearer "+sessionManager?.getToken(),keyword)
            mcall?.enqueue(object : Callback<SearchResponseModel?> {
                override fun onResponse(
                    call: Call<SearchResponseModel?>,
                    response: retrofit2.Response<SearchResponseModel?>,
                ) {
                    try {
                        if (response.body() != null) {
                            val result = response.body()!!

                            result.apply {

                                searchAdapter.updateList(data?.data!!)
                            }
                           // Log.v("responseannouncement-->", result)
                           /* val mjonsresponse = JSONObject(result)
                            val data = mjonsresponse.getJSONObject("data")
                            val dataArray = data.getJSONArray("data")*/

                         /*   for(i in 0 until dataArray.length())
                            {
                                var j = i+1
                                var song_id = dataArray.getJSONObject(i).getInt("song_id")
                                var title = dataArray.getJSONObject(i).getString("title")
                                var description = dataArray.getJSONObject(i).getString("description")
                                var thumbnail = dataArray.getJSONObject(i).getString("thumbnail")
                                var albumDetailsResponceModel =AlbumDetailsResponceModel(j,song_id.toString().toInt(),title,description,thumbnail)
                                arrayList.add(albumDetailsResponceModel)
                            }
                            if(arrayList.size > 0)
                            {

                                recentlyplayedAdapter.updateList(arrayList)
                            }*/






                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                   // hideProgressDialog()
                }

                override fun onFailure(call: Call<SearchResponseModel?>, t: Throwable) {
                    Log.v("onFailure", t.message!!)
                }
            })
        } else {
            Toast.makeText(this@SearchActivity, "OOPS! No Internet Connection", Toast.LENGTH_SHORT).show()
        }
    }


    fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(this@SearchActivity)
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

    override fun onClick(modelData: List<CommonDataModel1>, position: Int,type:String) {

    }
}