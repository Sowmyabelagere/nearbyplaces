package com.example.foursquareplaces.presentation.placeslist

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.foursquareplaces.R
import com.example.foursquareplaces.databinding.AdapterPlacesItemLayoutBinding
import com.example.foursquareplaces.model.Place

class PlacesRecyclerAdapter(private val placesList: List<Place>) :
    RecyclerView.Adapter<PlacesRecyclerAdapter.PlacesViewHolder>() {

    inner class PlacesViewHolder(private val adapterBinding: AdapterPlacesItemLayoutBinding) :
        RecyclerView.ViewHolder(adapterBinding.root) {
        fun bindItem(place: Place) {
            adapterBinding.place = place
            adapterBinding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesViewHolder {
        val adapterBinding = AdapterPlacesItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlacesViewHolder(adapterBinding)
    }

    override fun getItemCount(): Int {
        return placesList.size
    }

    override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) {
        holder.bindItem(placesList[position])
    }

    companion object{
        @JvmStatic
        @BindingAdapter("imageUrl")
        fun setImageUrl(imageView: ImageView, url:String?){
            url?.let {
                imageView.load(it) {
                    crossfade(true)
                    placeholder(R.drawable.background_oval_rounded)
                    transformations(CircleCropTransformation())
                }
            }
        }
    }
}