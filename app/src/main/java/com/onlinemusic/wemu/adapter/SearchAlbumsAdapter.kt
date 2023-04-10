package com.onlinemusic.wemu.adapter

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
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
import com.onlinemusic.wemu.fragment.SearchFragment
import com.onlinemusic.wemu.responseModel.searchcategory.response.Album

import com.onlinemusic.wemu.session.SessionManager
import java.util.regex.Matcher
import java.util.regex.Pattern

class SearchAlbumsAdapter (var context: Context,var modelList: List<Album>) :
    RecyclerView.Adapter<SearchAlbumsAdapter.MyViewHolder>()  {
    var sessionManager: SessionManager? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v: View = LayoutInflater.from(context).inflate(R.layout.searchsongmorelayout, parent, false)
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

        val notes = modelList[position].title
        val sb = SpannableStringBuilder(notes)
        val p: Pattern = Pattern.compile(SearchFragment.search_string, Pattern.CASE_INSENSITIVE)
        val m: Matcher = p.matcher(notes)
        while (m.find()) {
            //String word = m.group();
            //String word1 = notes.substring(m.start(), m.end());
            sb.setSpan(
                ForegroundColorSpan(Color.BLUE),
                m.start(),
                m.end(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        Glide.with(context)
            .load(modelList[position].thumbnail)
            .timeout(6000)
            .error(R.drawable.logo)
            .placeholder(R.drawable.logo)
            .into(holder.imgBanner)

        if (sessionManager?.getTheme().equals("night")){
            holder.imgTitle.setTextColor(context.getResources().getColor(R.color.white))
            holder.title_singer.setTextColor(context.getResources().getColor(R.color.white))
        }else{
            holder.imgTitle.setTextColor(context.getResources().getColor(R.color.black))
            holder.title_singer.setTextColor(context.getResources().getColor(R.color.black))
        }


        holder.imgTitle.text = sb
        holder.title_singer.text = modelList[position].description
        holder.songsLL.setOnClickListener {

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




    override fun getItemCount(): Int {
        if(modelList.size > 5)
        {
            return 5
        }
        else {
            return modelList.size
        }
    }



    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        var imgTitle: TextView
        var title_singer: TextView
        var unique_id: TextView
        var imgBanner: ImageView
        var optionLl: ImageView
        var songsLL: LinearLayout


        init {
            imgBanner = view.findViewById(R.id.imgBanner)
            imgTitle = view.findViewById(R.id.imgTitle)
            title_singer = view.findViewById(R.id.title_singer)
            unique_id = view.findViewById(R.id.unique_id)
            optionLl = view.findViewById(R.id.optionLl)
            songsLL = view.findViewById(R.id.songsLL)


        }
    }



    init {
        this.context = context
        this.modelList = modelList
    }
}