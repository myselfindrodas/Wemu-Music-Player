package com.onlinemusic.wemu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.onlinemusic.wemu.MainActivity
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.utils.Utilities

class Popular_week_Adapter(var context: Context,
                           var onItemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<Popular_week_Adapter.MyViewHolder>()  {
    var sessionManager: SessionManager? = null
    var modelList: ArrayList<CommonDataModel1> = ArrayList()
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


        Glide.with(context)
            .load(R.drawable.sound_bar_animation_final)
            .timeout(6000)
            .error(R.drawable.logo)
            .placeholder(R.drawable.logo)
            .into(holder.play_icon)

        if (sessionManager?.getTheme().equals("night")){
            holder.imgTitle.setTextColor(context.getResources().getColor(R.color.white))
        }else{
            holder.imgTitle.setTextColor(context.getResources().getColor(R.color.black))
        }
        holder.imgTitle.text = modelList[position].title

        if (MainActivity.songId==modelList[position].song_id){
            holder.play_icon.visibility = View.VISIBLE
        }else{
            holder.play_icon.visibility = View.GONE
        }

        holder.itemView.rootView.setOnClickListener {


            onItemClickListener.onClick(modelList!!, position,"Play_Song")
            notifyItemRangeChanged(0,modelList.size)

        }

        holder.menu_option.setOnClickListener {
            showPopupWindow(it, position, modelList!!)
        }


    }


    private fun showPopupWindow(
        anchor: View,
        position: Int,
        modelList: List<CommonDataModel1>
    ) {
        PopupWindow(anchor.context).apply {
            isOutsideTouchable = true
            val inflater = LayoutInflater.from(anchor.context)
            contentView = inflater.inflate(R.layout.popup_layout, null).apply {
                measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                val tvPlayList = this.findViewById<TextView>(R.id.tvPlayList)
                val tvShare = this.findViewById<TextView>(R.id.tvShare)
                val view1 = this.findViewById<View>(R.id.view1)
                val likeSong = this.findViewById<TextView>(R.id.likeSong)

                var subscribeStatus = sessionManager?.getSubscribed()
                if(subscribeStatus == true)
                {
                    tvPlayList.visibility = View.VISIBLE
                    view1.visibility = View.VISIBLE
                }
                else
                {
                    tvPlayList.visibility = View.GONE
                    view1.visibility = View.GONE
                }


                if (modelList[position].is_liked==1){
                    likeSong.text="Unlike Song"
                }else{
                    likeSong.text="Like Song"
                }

                likeSong.setOnClickListener {
                    onItemClickListener.onClick(modelList, position,"Like_Song")
                    dismiss()
                }

                tvPlayList.setOnClickListener {
                    // listener.onItemClickAction(it, position, "edit", catId)
                    onItemClickListener.onClick(modelList, position,"Add_To_Playlist")
                    dismiss()

                }
                tvShare.setOnClickListener {
                    // listener.onItemClickAction(it, position, "delete", catId)
                    onItemClickListener.onClick(modelList, position,"Share_Song")
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

            /*  popupWindow.setBackgroundDrawable(

                      anchor.context,
                      R.drawable.shadow_rectangle_white

              )*/


            popupWindow.setBackgroundDrawable(
                Utilities.getDrawable(
                    anchor.context,
                    R.drawable.shadow_rectangle_white
                )
            )
            popupWindow.showAsDropDown(anchor, 0, 0)
            //popupWindow.showAsDropDown(holder.menu_option);

            /*popupWindow.showAtLocation(
                anchor,
                Gravity.TOP or Gravity.START,
                location[0] - (size.width - anchor.width) / 2,
                location[1] - size.height
            )*/
        }
    }


    fun updateData(mModelData: List<CommonDataModel1>){
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