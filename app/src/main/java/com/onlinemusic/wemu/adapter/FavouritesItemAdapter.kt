package com.onlinemusic.wemu.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.onlinemusic.wemu.MainActivity
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.utils.Utilities

class FavouritesItemAdapter(
    var context: Context,
    var onItemClickListener: DeleteItem
) :
    RecyclerView.Adapter<FavouritesItemAdapter.MyViewHolder>() {
    var modelList: MutableList<CommonDataModel1> = arrayListOf()
    var sessionManager: SessionManager? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v: View =
            LayoutInflater.from(context).inflate(R.layout.my_favourate_container, parent, false)
        sessionManager = SessionManager(context)
        return MyViewHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (sessionManager?.getTheme().equals("night")) {
            holder.songs_title.setTextColor(context.getResources().getColor(R.color.white))
            holder.songs_desc.setTextColor(context.getResources().getColor(R.color.white))
            holder.unique_id.setTextColor(context.getResources().getColor(R.color.white))
        } else {
            holder.songs_title.setTextColor(context.getResources().getColor(R.color.black))
            holder.songs_desc.setTextColor(context.getResources().getColor(R.color.black))
            holder.unique_id.setTextColor(context.getResources().getColor(R.color.black))
        }

        holder.songs_title.text = modelList[position].title
        holder.songs_desc.text = modelList[position].description
        holder.unique_id.text = (position + 1).toString()

        holder.unique_id.visibility=View.GONE
        val params =
            RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)

        println(modelList.size)
        if (position==modelList.size-1) {
            params.setMargins(0, 0, 0, 250)
        }else{
            params.setMargins(0, 0, 0, 0)
        }

        holder.llMain.layoutParams=params
        Glide.with(context)
            .load(modelList[position].thumbnail)
            .timeout(6000)
            .error(R.drawable.logo)
            .placeholder(R.drawable.logo)
            .into(holder.imgBanner)

        Glide.with(context)
            .load(R.drawable.sound_bar_animation_final)
            .timeout(6000)
            .error(R.drawable.logo)
            .placeholder(R.drawable.logo)
            .into(holder.play_icon)
        if (MainActivity.songId==modelList[position].song_id){
            holder.play_icon.visibility = View.VISIBLE
        }else{
            holder.play_icon.visibility = View.GONE
        }
        holder.llMain.setOnClickListener {
            onItemClickListener.onClickSelectedItem(modelList,position)
            notifyItemRangeChanged(0,modelList.size)
        }

        holder.menu_option.setOnClickListener {

            showPopupWindow(it, position, modelList,holder)
        }
    }

    private fun showPopupWindow(
        anchor: View,
        position: Int,
        modelList: List<CommonDataModel1>,
        holder: MyViewHolder
    ) {
        PopupWindow(anchor.context).apply {
            isOutsideTouchable = true
            val inflater = LayoutInflater.from(anchor.context)
            contentView = inflater.inflate(R.layout.playlistalertlayout, null).apply {
                measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                val tvRemove = this.findViewById<TextView>(R.id.tvRemove)
                tvRemove.setOnClickListener {
                    // listener.onItemClickAction(it, position, "edit", catId)
                    onItemClickListener.deleteSelectedItem(modelList[position].song_id.toString())
                    dismiss()

                }

            }
        }.also { popupWindow ->
            // Absolute location of the anchor view
            /* val location = IntArray(2).apply {
                 anchor.getLocationOnScreen(this)
             }
             val size = Size(
                 popupWindow.contentView.measuredWidth,
                 popupWindow.contentView.measuredHeight
             )*/

            popupWindow.setBackgroundDrawable(
                Utilities.getDrawable(
                    anchor.context,
                    R.drawable.alert_album
                )
            )
            popupWindow.showAsDropDown(anchor, 0, 0);



            // popupWindow.showAsDropDown(holder.optionLl);

            /*popupWindow.showAtLocation(
                anchor,
                Gravity.TOP or Gravity.START,
                location[0] - (size.width - anchor.width) / 2,
                location[1] - size.height
            )*/
        }
    }
    fun updateList(mModelList: List<CommonDataModel1>) {

        modelList.clear()
        modelList.addAll(mModelList)
        notifyDataSetChanged()

    }

    fun addToList(mModelList: List<CommonDataModel1>) {
        val lastIndex: Int = modelList.size
        modelList.addAll(mModelList)
        notifyItemRangeInserted(lastIndex, mModelList.size)
    }


    override fun getItemCount(): Int {
        return modelList.size
    }


    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        var imgBanner: ImageView
        var unique_id: TextView
        var songs_title: TextView
        var songs_desc: TextView
        var llMain: LinearLayout
        var menu_option: RelativeLayout
        var play_icon: ImageView


        init {

            llMain = view.findViewById(R.id.llMain)
            unique_id = view.findViewById(R.id.unique_id)
            imgBanner = view.findViewById(R.id.imgBanner)
            songs_title = view.findViewById(R.id.songs_title)
            songs_desc = view.findViewById(R.id.songs_desc)
            play_icon = view.findViewById(R.id.play_icon)
            menu_option = view.findViewById(R.id.menu_option)

        }
    }
    interface DeleteItem
    {
        fun deleteSelectedItem(songId:String)
        fun onClickSelectedItem(modelList: List<CommonDataModel1>,position: Int)
    }

    init {
        this.context = context
        // this.modelList = modelList
    }
}