package com.onlinemusic.wemu.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.responseModel.CommonDataModel1
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.onlinemusic.wemu.MainActivity
import com.onlinemusic.wemu.session.SessionManager
import com.onlinemusic.wemu.utils.Utilities

class NewMusicAdapter(var context: Context,
                      var onItemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<NewMusicAdapter.MyViewHolder>()  {
    var sessionManager: SessionManager? = null
    var modelList: MutableList<CommonDataModel1> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v: View = LayoutInflater.from(context).inflate(R.layout.new_music_layout_container, parent, false)
        sessionManager = SessionManager(context)
        return MyViewHolder(v)
    }
    // var modelList: MutableList<DemoMusicDataModel> = arrayListOf()
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (sessionManager?.getTheme().equals("night")){
            holder.songs_title.setTextColor(context.getResources().getColor(R.color.white))
            holder.songs_desc.setTextColor(context.getResources().getColor(R.color.white))

        }else{
            holder.songs_title.setTextColor(context.getResources().getColor(R.color.black))
            holder.songs_desc.setTextColor(context.getResources().getColor(R.color.black))
        }
        holder.songs_title.text = modelList[position].title
        holder.songs_desc.text = modelList[position].title
        holder.tvViews.text = modelList[position].views_count.toString()+ " views, " + modelList[position].like_count.toString()+" likes, " + modelList[position].play_count.toString() +" plays"

        //  holder.unique_id.text = (position+1).toString()
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
        /*holder.main_ll.setOnClickListener {
            holder.main_ll.setBackground(ContextCompat.getDrawable(context, R.drawable.selectedbox));
            holder.play_icon.visibility = View.VISIBLE
            showSong()

        }*/
        holder.unique_id.visibility = View.GONE
        if (MainActivity.songId==modelList[position].song_id){
            holder.play_icon.visibility = View.VISIBLE
        }else{
            holder.play_icon.visibility = View.GONE
        }
        /*holder.img_ll.setOnClickListener{
//            var intent = Intent(context,SecondActivity::class.java)
//            context.startActivity(intent)
        }*/
      /*  holder.itemView.rootView.setOnClickListener {
            // holder.main_ll.setBackground(ContextCompat.getDrawable(context, R.drawable.selectedbox));
            //holder.play_icon.visibility = View.VISIBLE
            Log.d("bvmnb",modelList.size.toString())
            Log.d("bvmnb",modelList.toString())
            onItemClickListener.onClick(modelList, position)

            *//*stopPlaying()
            if (!dialog.isShowing)
                showSong(modelList[position], holder.itemView.rootView)
*//*

            //val intent = Intent(this, Player::class.java)
            //startActivity(intent)
        } */
         holder.ll_song.setOnClickListener {

            onItemClickListener.onClick(modelList, position,"Play_Song")
             notifyItemRangeChanged(0,modelList.size)


        }

        holder.optionLl.setOnClickListener {
            showPopupWindow(it, position, modelList,holder)
        }



    }
    fun updateList(mModelList: List<CommonDataModel1>){

        modelList.clear()
        modelList.addAll(mModelList)
        notifyDataSetChanged()

    }

    fun addToList(mModelList: List<CommonDataModel1>) {
        val lastIndex: Int = modelList.size
        modelList.addAll(mModelList)
        notifyItemRangeInserted(lastIndex, mModelList.size)
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
           // popupWindow.showAsDropDown(holder.optionLl);

            /*popupWindow.showAtLocation(
                anchor,
                Gravity.TOP or Gravity.START,
                location[0] - (size.width - anchor.width) / 2,
                location[1] - size.height
            )*/
        }
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


    override fun getItemCount(): Int {
        return modelList.size
    }



    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        /*var main_ll: RelativeLayout
        var img_ll: CardView*/
        var play_icon: ImageView
        var imgBanner: ImageView
        // var unique_id: TextView
        var songs_title: TextView
        var songs_desc: TextView
        var unique_id: TextView
        var optionLl: RelativeLayout
        var ll_song: LinearLayout
        var tvViews: TextView

        init {
            /* main_ll = view.findViewById(R.id.main_ll)
             img_ll = view.findViewById(R.id.img_ll)
             unique_id = view.findViewById(R.id.unique_id)*/
            play_icon = view.findViewById(R.id.play_icon)
            imgBanner = view.findViewById(R.id.ivThumbnail)
            songs_title = view.findViewById(R.id.tvTitle)
            songs_desc = view.findViewById(R.id.tvDetails)
            unique_id = view.findViewById(R.id.unique_id)
            optionLl = view.findViewById(R.id.optionLl)
            ll_song = view.findViewById(R.id.ll_song)
            tvViews = view.findViewById(R.id.tvViews)
        }
    }


    interface OnItemClickListener{
        fun onClick(modelData: List<CommonDataModel1>, position: Int,type:String)
    }

    init {
        this.context = context
        // this.modelList = modelList
    }
}