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
import com.onlinemusic.wemu.responseModel.artist.response.DataX
import com.onlinemusic.wemu.session.SessionManager


class ArtistAdapter (var context: Context

) :
    RecyclerView.Adapter<ArtistAdapter.MyViewHolder>()  {
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
            holder.title.setTextColor(context.getResources().getColor(R.color.white))
        }else{
            holder.title.setTextColor(context.getResources().getColor(R.color.black))
        }
        holder.title.text = modelList[position].name

        Glide.with(context)
            .load(modelList[position].avatar)
            .timeout(6000)
            .error(R.drawable.logo)
            .placeholder(R.drawable.logo)
            .into(holder.imgBanner)
        holder.imgBanner.setOnClickListener {
            /*  val bundle = Bundle()
              bundle.putInt("album_id",modelList[position].id.toString().toInt())
              bundle.putString("album_title",modelList[position].cateogryName)
              bundle.putString("album_image",modelList[position].backgroundThumb)
              bundle.putString("key","genre")
              val navController = Navigation.findNavController(it)
              navController.navigate(R.id.nav_album_details,bundle)*/
            val bundle = Bundle()
            bundle.putInt("album_id",modelList[position].id.toString().toInt())
            bundle.putString("album_title",modelList[position].name)
            bundle.putString("album_image",modelList[position].avatar)
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_artist_songs,bundle)
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



    init {
        this.context = context
        // this.modelList = modelList
    }
}