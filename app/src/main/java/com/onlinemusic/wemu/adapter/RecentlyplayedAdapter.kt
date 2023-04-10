package com.onlinemusic.wemu.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.onlinemusic.wemu.MainActivity
import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.utils.Utilities
import com.squareup.picasso.Picasso

class RecentlyplayedAdapter(var context: Context,
                            var onItemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<RecentlyplayedAdapter.MyViewHolder>()  {
    var sessionManager: SessionManager? = null
    var modelList: MutableList<CommonDataModel1> = arrayListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v: View = LayoutInflater.from(context).inflate(R.layout.rv_recentplaylistlayout, parent, false)
        sessionManager = SessionManager(context)
        return MyViewHolder(v)
    }

    @SuppressLint("SetTextI18n", "SuspiciousIndentation")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        if (sessionManager?.getTheme().equals("night"))
        {
            holder.songs_title.setTextColor(context.getResources().getColor(R.color.white))
            holder.songs_desc.setTextColor(context.getResources().getColor(R.color.white))
            holder.unique_id.setTextColor(context.getResources().getColor(R.color.white))
            holder.main_ll.setBackgroundColor(context.resources.getColor(R.color.black))
            holder.viewLL.setBackgroundColor(context.resources.getColor(R.color.white))
            holder.option.setColorFilter(ContextCompat.getColor(context, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY)

        }
        else
        {
            holder.songs_title.setTextColor(context.getResources().getColor(R.color.black))
            holder.songs_desc.setTextColor(context.getResources().getColor(R.color.black))
            holder.unique_id.setTextColor(context.getResources().getColor(R.color.black))
            holder.main_ll.setBackgroundColor(context.resources.getColor(R.color.orange))
            holder.viewLL.setBackgroundColor(context.resources.getColor(R.color.black))
            holder.option.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY)

        }
        holder.songs_title.text = modelList[position].title
        holder.songs_desc.text = modelList[position].description
        holder.unique_id.text = (position+1).toString()
        holder.unique_id.visibility=View.GONE
      /*  Glide.with(context)
            .load(modelList[position].thumbnail)
            .into(holder.imgBanner)*/

        holder.tvViews.text = modelList[position].views_count.toString()+ " views, " + modelList[position].like_count.toString()+" likes, " + modelList[position].play_count.toString() +" plays"

        if (MainActivity.songId==modelList[position].song_id){
            holder.play_icon.visibility = View.VISIBLE
        }else{
            holder.play_icon.visibility = View.GONE
        }
        Picasso.get()
            .load(modelList[position].thumbnail)
            .error(R.drawable.logo)
            .placeholder(R.drawable.logo)
            .into(holder.imgBanner);

        Glide.with(context)
            .load(R.drawable.sound_bar_animation_final)
            .timeout(6000)
            .error(R.drawable.logo)
            .placeholder(R.drawable.logo)
            .into(holder.play_icon)
       /* val params =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        if (modelList.size-1==position) {
            params.setMargins(0, 0, 0, 250)
        }else{
            params.setMargins(0, 0, 0, 0)
        }

        holder.main_ll.layoutParams=params*/

        holder.songs_ll.setOnClickListener {
            /*holder.main_ll.setBackground(ContextCompat.getDrawable(context, R.drawable.selectedbox));
            holder.play_icon.visibility = View.VISIBLE*/
           // showSong()
          //  holder.play_icon.visibility = View.VISIBLE

                onItemClickListener.onClick(modelList, position,"Play_Song")

            notifyItemRangeChanged(0,modelList.size)

        }

        holder.img_ll.setOnClickListener {
//            var intent = Intent(context,SecondActivity::class.java)
//            context.startActivity(intent)
        }
        holder.menu_option.setOnClickListener {
            showPopupWindow(it, position, modelList,holder)
        }


    }
    private fun showPopupWindow(
        anchor: View,
        position: Int,
        modelList: List<CommonDataModel1>,
        holder: RecentlyplayedAdapter.MyViewHolder
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
    fun getFullList() : List<CommonDataModel1> = modelList
    fun updateList(mModelList: List<CommonDataModel1>){

        modelList.clear()
        mModelList.forEach {
            if (!modelList.contains(it)){
                modelList.add(it)
            }
        }
       // modelList.addAll(mModelList)
        notifyDataSetChanged()

    }

    fun addToList(mModelList: List<CommonDataModel1>) {
        val lastIndex: Int = modelList.size
       /* mModelList.forEach {
            if (!modelList.contains(it)){
                modelList.add(it)
            }
        }*/
        modelList.addAll(mModelList)
        notifyItemRangeInserted(lastIndex, mModelList.size)
    }

    private fun showSong() {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout: View = layoutInflater.inflate(R.layout.songs_bottomsheet, null, false)
        val dialog = BottomSheetDialog(context)
        (dialog).behavior.peekHeight = 200
        dialog.setContentView(layout)
        dialog.show()



        // RelativeLayout headerLay = (RelativeLayout) dialog.findViewById(R.id.addTask_dialog_header);





    }

    interface OnItemClickListener {
        fun onClick(modelData: List<CommonDataModel1>, position: Int,type:String)
    }

    override fun getItemCount(): Int {
        return modelList.size
    }



    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var main_ll: RelativeLayout
        var img_ll: CardView
        var play_icon: ImageView
        var imgBanner: ImageView
        var option: ImageView
        var menu_option: RelativeLayout
        var songs_ll: RelativeLayout
        var unique_id: TextView
        var songs_title: TextView
        var songs_desc: TextView
        var tvViews: TextView
        var viewLL: View


        init {
            main_ll = view.findViewById(R.id.main_ll)
            img_ll = view.findViewById(R.id.img_ll)
            play_icon = view.findViewById(R.id.play_icon)
            unique_id = view.findViewById(R.id.unique_id)
            option = view.findViewById(R.id.option)
            imgBanner = view.findViewById(R.id.imgBanner)
            songs_title = view.findViewById(R.id.songs_title)
            songs_desc = view.findViewById(R.id.songs_desc)
            menu_option = view.findViewById(R.id.menu_option)
            songs_ll = view.findViewById(R.id.songs_ll)
            viewLL = view.findViewById(R.id.viewLL)
            tvViews = view.findViewById(R.id.tvViews)

        }
    }



    init {
        this.context = context
       // this.modelList = modelList
    }
}