package com.onlinemusic.wemu.adapter


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.onlinemusic.wemu.R
import com.onlinemusic.wemu.responseModel.dashboard.response.BannerData


class SliderAdapter(

    var viewPager2: ViewPager2,
    var context: Context
) :
    RecyclerView.Adapter<SliderAdapter.SliderViewHolder>() {
   /* var context: Context
    private val viewPager2: ViewPager2
    private val sliderItems: List<PopularModelResponse>*/

     inner class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgBanner: ImageView
        var imgTitle: TextView

        init {
            imgBanner = itemView.findViewById(R.id.imgBanner)
            imgTitle = itemView.findViewById(R.id.imgTitle)
        }
    }
    var sliderItems: ArrayList<BannerData> = ArrayList()
    private val runnable = Runnable {
        sliderItems.addAll(sliderItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        return SliderViewHolder(
            LayoutInflater.from(parent.context).inflate(
               R.layout.banner_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        //holder.imgTitle.text = sliderItems[position].
        Glide.with(context)
            .load(sliderItems[position].banner_image)
            .timeout(6000)
            .error(R.drawable.logo)
            .placeholder(R.drawable.logo)
            .into(holder.imgBanner)
        //holder.imgBanner.setBackground(ContextCompat.getDrawable(context, sliderItems[position].thumbnail()));
        if (position == sliderItems.size - 2) {
            viewPager2.post(runnable)
        }
        holder.imgBanner.setOnClickListener {
            if(sliderItems[position].song_id != null)
            {
                val bundle = Bundle()
                bundle.putString("search_key", sliderItems[position].song_id)
                bundle.putString("key", "songs")
                bundle.putString("type", "banner")

                val navController = Navigation.findNavController(it)
                navController.navigate(R.id.nav_search_more, bundle)
            }
            else if(sliderItems[position].album_id != null)
            {
                val bundle = Bundle()
                bundle.putString("search_key", sliderItems[position].album_id)
                bundle.putString("key", "album")
                bundle.putString("type", "banner")

                val navController = Navigation.findNavController(it)
                navController.navigate(R.id.nav_search_more, bundle)
            }
            else if(sliderItems[position].url != null)
            {
                    if(URLUtil.isValidUrl(sliderItems[position].url)) {
                        var url = sliderItems[position].url;
                        val browserIntent =
                            Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(browserIntent)
                    }
                else{
                        Toast.makeText(context, "Url is invalid", Toast.LENGTH_SHORT).show()
                    }


            }
            else
            {

            }
        }

    }

    fun updateData(mSliderItems: ArrayList<BannerData>){
        sliderItems.clear()
        sliderItems.addAll(mSliderItems)
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
        return sliderItems.size
    }

    //  private Integer [] images = {R.drawable.bannerone,R.drawable.bannertwo,R.drawable.bannerthree,R.drawable.bannerfour};
    /*init {
        this.sliderItems = sliderItems
        this.viewPager2 = viewPager2
        this.context = context
    }*/
}