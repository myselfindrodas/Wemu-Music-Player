package com.onlinemusic.wemu.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.responseModel.albumslist.response.DataX
import com.onlinemusic.wemu.session.SessionManager

class MoreAlbumsAdapter (var context: Context) :
    RecyclerView.Adapter<MoreAlbumsAdapter.MyViewHolder>()  {
    var sessionManager: SessionManager? = null
    var modelList : ArrayList<DataX> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v: View = LayoutInflater.from(context).inflate(R.layout.albums_layout, parent, false)
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
        }
        else
        {
            holder.imgTitle.setTextColor(context.getResources().getColor(R.color.black))
        }
        holder.imgTitle.text = modelList[position].title
        holder.btnAlbumdetails.setOnClickListener {

            val bundle = Bundle()
            bundle.putInt("album_id",modelList[position].palbumId.toString().toInt())
            bundle.putString("album_title",modelList[position].title)
            bundle.putString("album_image",modelList[position].thumbnail)
            bundle.putInt("album_category",modelList[position].categoryId.toString().toInt())
            bundle.putString("key","album")
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_album_details,bundle)

            /*val intent = Intent(context, Albumdetails::class.java)
            intent.putExtra("album_id",modelList[position].palbum_id)
            intent.putExtra("album_title",modelList[position].title)
            intent.putExtra("album_image",modelList[position].thumbnail)
            context.startActivity(intent)*/
        }

    }



    fun updateData(mModelData: List<DataX>){
        modelList.clear()
        mModelData.forEach {
            if (!modelList.contains(it)){
                modelList.add(it)
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
        var btnAlbumdetails: LinearLayout


        init {
            imgBanner = view.findViewById(R.id.imgBanner)
            imgTitle = view.findViewById(R.id.imgTitle)
            btnAlbumdetails = view.findViewById(R.id.btnAlbumdetails)


        }
    }



    init {
        this.context = context
    }
}