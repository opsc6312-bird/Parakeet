package com.example.parakeet_application.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.parakeet_application.R
import com.example.parakeet_application.data.interfaces.NearLocationInterface
import com.example.parakeet_application.data.model.mapsModel.GooglePlaceModel
import com.example.parakeet_application.databinding.PlaceItemLayoutBinding

class GooglePlaceAdapter(private val nearLocationInterface: NearLocationInterface):
    RecyclerView.Adapter<GooglePlaceAdapter.ViewHolder>() {
    private var googlePlaceModels:List<GooglePlaceModel>?=null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: PlaceItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.place_item_layout,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return if (googlePlaceModels != null) googlePlaceModels!!.size else 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (googlePlaceModels!=null){
            val placeModel = googlePlaceModels!![position]
            holder.binding.googlePlaceModel = placeModel
            holder.binding.listener = nearLocationInterface
        }
    }
    fun setGooglePlaces(googlePlaceModel: List<GooglePlaceModel>){
        googlePlaceModels = googlePlaceModel
        notifyItemRangeChanged(0, googlePlaceModel.size)
    }
    class ViewHolder(val binding: PlaceItemLayoutBinding): RecyclerView.ViewHolder(binding.root)
}