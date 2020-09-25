package com.illu.chatclient

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.illu.chatclient.Msg.Companion.TYPE_RECEIVE
import com.illu.chatclient.Msg.Companion.TYPE_SEND
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.item_msg.view.*
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File

class MsgAdapter(layoutId: Int = R.layout.item_msg) :
    BaseQuickAdapter<Msg, BaseViewHolder>(layoutId) {

    override fun convert(holder: BaseViewHolder, item: Msg) {
        holder.run {
            itemView.run {
                if (item.picUri != null) {
                    if (item.type == TYPE_SEND) {
                        imgRight.visibility = View.VISIBLE
                        loadImg(context, item, imgRight)
                    } else if (item.type == TYPE_RECEIVE) {
                        imgLeft.visibility = View.VISIBLE
                        loadImg(context, item, imgLeft)
                    }
                } else {
                    if (item.type == TYPE_SEND) {
                        tvRight.setText(item.content)
                        tvRight.visibility = View.VISIBLE
                    } else if (item.type == TYPE_RECEIVE) {
                        tvLeft.setText(item.content)
                        tvLeft.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    fun loadImg(context: Context, item: Msg, imgView: ImageView) {
        Glide.with(context).load(item.picUri).into(imgView)
    }
}

data class Msg(
    val content: String? = "",
    val type: Int = 0,
    val picUri: Uri? = null
) {

    companion object {
        val TYPE_SEND = 1
        val TYPE_RECEIVE = 2
    }
}