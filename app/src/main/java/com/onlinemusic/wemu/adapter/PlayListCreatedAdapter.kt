package com.onlinemusic.wemu.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.onlinemusic.wemu.R

import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.responseModel.playlistitems.response.DataX

class PlayListCreatedAdapter (var isFromPlayList:Boolean=false,var context: Context,  var onItemClickListener: OnPlaylistItemClickListener

) :
    RecyclerView.Adapter<PlayListCreatedAdapter.MyViewHolder>()  {
    var modelList: MutableList<DataX> = arrayListOf()
    var sessionManager: SessionManager? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v: View = LayoutInflater.from(context).inflate(R.layout.artistlayout, parent, false)
        sessionManager = SessionManager(context)
        return MyViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (sessionManager?.getTheme().equals("night")){
            holder.title.setTextColor(context.resources.getColor(R.color.white))
        }else{
            holder.title.setTextColor(context.resources.getColor(R.color.black))
        }
        holder.title.text = modelList[position].name

        Glide.with(context)
            .load(modelList[position].thumbnail)
            .timeout(6000)
            .error(R.drawable.logo)
            .placeholder(R.drawable.logo)
            .into(holder.imgBanner)
        holder.imgBanner.setOnClickListener {
            if (isFromPlayList) {
                val bundle = Bundle()
                bundle.putInt("playlist_id", modelList[position].id!!.toInt())
                bundle.putString("playlist_name", modelList[position].name!!)
                bundle.putString("playlist_image_url", modelList[position].thumbnail!!)
                val navController = Navigation.findNavController(it)
                navController.navigate(R.id.nav_playlistDetails, bundle)
            }else{
                onItemClickListener.onClick(modelList,position)
            }
        }



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





    override fun getItemCount(): Int {
        return modelList.size
    }



    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        var imgBanner: ImageView
        var title: TextView


        init {
            imgBanner = view.findViewById(R.id.imgBanner)
            title = view.findViewById(R.id.title)



        }
    }



    interface OnPlaylistItemClickListener{
        fun onClick(modelData: List<DataX>, position: Int)
    }


    init {
        this.context = context
        // this.modelList = modelList
    }
}