package com.onlinemusic.wemu.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.onlinemusic.wemu.responseModel.albumslist.response.DataX
import com.onlinemusic.wemu.session.SessionManager

class NewAlbumsAdapter(var context: Context, var onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<NewAlbumsAdapter.MyViewHolder>()  {
    var sessionManager: SessionManager? = null
     var modelList: ArrayList<DataX> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v: View = LayoutInflater.from(context).inflate(R.layout.popular_layout, parent, false)
        sessionManager = SessionManager(context)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context)
            .load(modelList[position].thumbnail)
            .timeout(6000)
            .error(R.drawable.logo)
            .placeholder(R.drawable.logo)
            .into(holder.imgBanner)
        if (sessionManager?.getTheme().equals("night")){
            holder.imgTitle.setTextColor(context.getResources().getColor(R.color.white))
        }else{
            holder.imgTitle.setTextColor(context.getResources().getColor(R.color.black))
        }
        holder.imgTitle.text = modelList[position].title
        holder.menu_option.visibility = View.GONE
        holder.play_icon.visibility = View.GONE

        holder.itemView.rootView.setOnClickListener {

               // onItemClickListener.onClick(modelList, position)
            val bundle = Bundle()
            bundle.putInt("album_id",modelList[position].palbumId.toString().toInt())
            bundle.putString("album_title",modelList[position].title)
            bundle.putString("album_image",modelList[position].thumbnail)
            bundle.putInt("album_category",modelList[position].categoryId.toString().toInt())
            bundle.putString("key","album")
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_album_details,bundle)
        }

    }


    fun updateData(mModelData: List<DataX>){
        modelList!!.clear()
        mModelData.forEach {
            if (!modelList!!.contains(it)){
                modelList!!.add(it)
            }
        }
        // modelList!!.addAll(mModelData)
        notifyDataSetChanged()
    }



    override fun getItemCount(): Int {
        return modelList.size
    }



    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        var imgTitle: TextView
        var imgBanner: ImageView
        var menu_option: RelativeLayout
        var play_icon: ImageView

        init {
            imgBanner = view.findViewById(R.id.imgBanner)
            imgTitle = view.findViewById(R.id.imgTitle)
            menu_option = view.findViewById(R.id.menu_option)
            play_icon = view.findViewById(R.id.play_icon)
        }
    }


    interface OnItemClickListener {
        fun onClick(modelData: List<CommonDataModel1>, position: Int,type:String)
    }


    init {
        this.context = context
        this.modelList = modelList
    }
}