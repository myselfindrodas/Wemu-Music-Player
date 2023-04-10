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
import com.onlinemusic.wemu.responseModel.searchartist.response.SingerList
import com.onlinemusic.wemu.session.SessionManager
import java.util.regex.Matcher
import java.util.regex.Pattern

class ArtistSearchItemsAdapter (var context: Context, ) :
    RecyclerView.Adapter<ArtistSearchItemsAdapter.MyViewHolder>()  {
    var sessionManager: SessionManager? = null
    var modelList: MutableList<SingerList> = arrayListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v: View = LayoutInflater.from(context).inflate(R.layout.searchsongmorelayout, parent, false)
        sessionManager = SessionManager(context)
        return MyViewHolder(v)
    }





    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val notes = modelList[position].name
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
            .load(modelList[position].avatar)
            .timeout(6000)
            .error(R.drawable.logo)
            .placeholder(R.drawable.logo)
            .into(holder.imgBanner)

        if (sessionManager?.getTheme().equals("night")){
            holder.imgTitle.setTextColor(context.getResources().getColor(R.color.white))
        }else{
            holder.imgTitle.setTextColor(context.getResources().getColor(R.color.black))
        }
        holder.imgTitle.text = sb
        holder.title_singer.visibility = View.GONE
        holder.songsLL.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("album_id",modelList[position].id.toString().toInt())
            bundle.putString("album_title",modelList[position].name)
            bundle.putString("album_image",modelList[position].avatar)
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.nav_artist_songs,bundle)
        }

    }


    fun updateList(mModelList: List<SingerList>){

        modelList.clear()
        modelList.addAll(mModelList)
        notifyDataSetChanged()

    }

    fun addToList(mModelList: List<SingerList>) {
        val lastIndex: Int = modelList.size
        modelList.addAll(mModelList)
        notifyItemRangeInserted(lastIndex, mModelList.size)
    }


    override fun getItemCount(): Int {
        return modelList.size
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