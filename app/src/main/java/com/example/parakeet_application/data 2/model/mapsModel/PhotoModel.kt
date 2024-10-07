package com.example.parakeet_application.data.model.mapsModel

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

data class PhotoModel(
    val height: Int?,
    val htmlAttributions: List<String>?,
    val photoReference: String?,
    val width: Int?
){
    companion object {
        @JvmStatic
        @BindingAdapter("loadImage")
        fun loadImage(view: ImageView, image: String?) {
            Glide.with(view.context).load(image).into(view)
        }
    }
}