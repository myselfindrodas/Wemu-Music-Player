package com.onlinemusic.wemu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.responseModel.notification.response.DataX
import com.onlinemusic.wemu.session.SessionManager

class NotificationAdapter (var context: Context) :
    RecyclerView.Adapter<NotificationAdapter.MyViewHolder>()  {
    var sessionManager: SessionManager? = null
    var modelList: MutableList<DataX> = arrayListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v: View = LayoutInflater.from(context).inflate(R.layout.notificationlayout, parent, false)
        sessionManager = SessionManager(context)

        return MyViewHolder(v)
    }




    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {


        if (sessionManager?.getTheme().equals("night")){
            holder.songs_title.setTextColor(context.getResources().getColor(R.color.white))
            holder.songs_title.setTextColor(context.getResources().getColor(R.color.white))
        }else{
            holder.songs_title.setTextColor(context.getResources().getColor(R.color.black))
            holder.songs_title.setTextColor(context.getResources().getColor(R.color.black))
        }
        holder.songs_title.text = modelList[position].notificationMsg

        val params =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        if (modelList.size-1==position) {
            params.setMargins(0, 0, 0, 250)
        }else{
            params.setMargins(0, 0, 0, 0)
        }

        holder.llMain.layoutParams=params
    }




    override fun getItemCount(): Int {
        return modelList.size
    }

    fun updateList(mModelList: List<DataX>){

        modelList.clear()
        modelList.addAll(mModelList)
        notifyDataSetChanged()

    }

    fun addToList(mModelList: List<DataX>) {
        val lastIndex: Int = modelList.size
        modelList.addAll(mModelList)
        notifyItemRangeInserted(lastIndex, mModelList.size)
    }


    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        var songs_title: TextView
        var songs_desc: TextView
        var imgBanner: ImageView
        var llMain: LinearLayout



        init {
            songs_title = view.findViewById(R.id.songs_title)
            songs_desc = view.findViewById(R.id.songs_desc)
            imgBanner = view.findViewById(R.id.imgBanner)
            llMain = view.findViewById(R.id.llMain)




        }
    }



    init {
        this.context = context
        this.modelList = modelList
    }
}