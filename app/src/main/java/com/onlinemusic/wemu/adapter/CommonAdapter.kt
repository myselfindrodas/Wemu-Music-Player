package com.onlinemusic.wemu.adapter

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


class CommonAdapter(
    var context: Context,

    var onItemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<CommonAdapter.MyViewHolder>() {
    var sessionManager: SessionManager? = null
    var modelList: MutableList<CommonDataModel1> = arrayListOf()
    var count = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v: View =
            LayoutInflater.from(context).inflate(R.layout.recently_common_layout, parent, false)
        sessionManager = SessionManager(context)
        return MyViewHolder(v)
    }

    companion object {

    }
    fun updateList(mModelList: List<CommonDataModel1>){

        modelList.clear()
        mModelList.forEach { itList->
            if (itList.audio_location!=null){

                modelList.add(itList)
            }
        }
        notifyDataSetChanged()

    }
    fun getAllList():ArrayList<CommonDataModel1>{
        return modelList as ArrayList<CommonDataModel1>
    }

    fun addToList(mModelList: List<CommonDataModel1>) {
        val lastIndex: Int = modelList.size
        mModelList.forEach { itList->
            if (itList.audio_location!=null){

                modelList.add(itList)
            }
        }
       // modelList.addAll(mModelList)
        notifyItemRangeInserted(lastIndex, mModelList.size)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //count += 1


        if (sessionManager?.getTheme().equals("night")){
            holder.imgTitle.setTextColor(context.getResources().getColor(R.color.white))
            holder.title_singer.setTextColor(context.getResources().getColor(R.color.white))
            holder.unique_id.setTextColor(context.getResources().getColor(R.color.white))
        }else{
            holder.imgTitle.setTextColor(context.getResources().getColor(R.color.black))
            holder.title_singer.setTextColor(context.getResources().getColor(R.color.black))
            holder.unique_id.setTextColor(context.getResources().getColor(R.color.black))
        }
        Glide.with(context)
            .load(modelList[position].thumbnail)
            .timeout(6000)
            .error(R.drawable.logo)
            .placeholder(R.drawable.logo)
            .into(holder.imgBanner)


        Glide.with(context)
            .load(R.drawable.sound_bar_animation_final)
            .timeout(6000)
            .placeholder(R.drawable.logo)
            .into(holder.play_icon)
        holder.imgTitle.text = modelList[position].title
        holder.title_singer.text = modelList[position].description
        holder.unique_id.visibility = View.GONE
        holder.tvViews.text = modelList[position].views_count.toString()+ " views, " + modelList[position].like_count.toString()+" likes, " + modelList[position].play_count.toString() +" plays"
        val params =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        if (modelList.size-1==position) {
            params.setMargins(0, 0, 0, 250)
        }else{
            params.setMargins(0, 0, 0, 0)
        }

        holder.llMain.layoutParams=params
       /* holder.itemView.rootView.setOnClickListener {
            // holder.main_ll.setBackground(ContextCompat.getDrawable(context, R.drawable.selectedbox));
            //holder.play_icon.visibility = View.VISIBLE
            onItemClickListener.onClick(modelList, position)

            *//*stopPlaying()
            if (!dialog.isShowing)
                showSong(modelList[position], holder.itemView.rootView)
*//*

            //val intent = Intent(this, Player::class.java)
            //startActivity(intent)
        }*/

        if (MainActivity.songId==modelList[position].song_id){
            holder.play_icon.visibility = View.VISIBLE
        }else{
            holder.play_icon.visibility = View.GONE
        }
         holder.songsLL.setOnClickListener {
            // holder.main_ll.setBackground(ContextCompat.getDrawable(context, R.drawable.selectedbox));
            //holder.play_icon.visibility = View.VISIBLE
            onItemClickListener.onClick(modelList, position,"Play_Song")
             notifyItemRangeChanged(0,modelList.size)

            /*stopPlaying()
            if (!dialog.isShowing)
                showSong(modelList[position], holder.itemView.rootView)
*/

            //val intent = Intent(this, Player::class.java)
            //startActivity(intent)
        }


        holder.optionLl.setOnClickListener {
            showPopupWindow(it, position, modelList,holder)
        }
    }

    private fun showPopupWindow(
        anchor: View,
        position: Int,
        modelList: List<CommonDataModel1>,
        holder: CommonAdapter.MyViewHolder
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

            popupWindow.setBackgroundDrawable(
                Utilities.getDrawable(
                    anchor.context,
                    R.drawable.shadow_rectangle_white
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



    override fun getItemCount(): Int {
        return modelList.size
    }


    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        var imgTitle: TextView
        var title_singer: TextView
        var unique_id: TextView
        var tvViews: TextView
        var imgBanner: ImageView
        var play_icon: ImageView
        var optionLl: RelativeLayout
        var songsLL: LinearLayout
        var llMain: LinearLayout


        init {
            imgBanner = view.findViewById(R.id.imgBanner)
            play_icon = view.findViewById(R.id.play_icon)
            imgTitle = view.findViewById(R.id.imgTitle)
            title_singer = view.findViewById(R.id.title_singer)
            unique_id = view.findViewById(R.id.unique_id)
            tvViews = view.findViewById(R.id.tvViews)
            optionLl = view.findViewById(R.id.menu_option)
            songsLL = view.findViewById(R.id.songsLL)
            llMain = view.findViewById(R.id.llMain)


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