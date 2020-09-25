package com.illu.chatclient

import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.request.target.ImageViewTarget

class TransformationUtils(private val target: ImageView) :
    ImageViewTarget<Bitmap>(target) {
    override fun setResource(resource: Bitmap?) {
        view.setImageBitmap(resource)

        //获取原图的宽高
        val width = resource?.width
        val height = resource?.height

        //获取imageView的宽
        val imageViewWidth = target.width

        //计算缩放比例
        val sy =
            (imageViewWidth * 0.1).toFloat() / (width?.times(0.1))!!.toFloat()

        //计算图片等比例放大后的高
        val imageViewHeight = (height?.times(sy))!!.toInt()
        val params = target.layoutParams
        params.height = imageViewHeight
        target.layoutParams = params
    }

}