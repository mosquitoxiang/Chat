package com.illu.chatclient

import android.net.Uri
import android.os.Environment
import android.view.Gravity
import android.view.View
import com.bumptech.glide.Glide
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
                    Luban.with(context)
                        .load(UriToPathUtil.getImageAbsolutePath(context, item.picUri))
                        .ignoreBy(100)
                        .setTargetDir(context.cacheDir.absolutePath)
                        .setCompressListener(object : OnCompressListener{
                            override fun onSuccess(file: File?) {
                                Glide.with(context).load(file).into(imgSelect)
                            }

                            override fun onError(e: Throwable?) {
                                
                            }

                            override fun onStart() {
                                
                            }

                        })
                }
                if (item.type == TYPE_SEND) {
                    tvRight.setText(item.content)
                    tvLeft.visibility = View.GONE
                } else if (item.type == TYPE_RECEIVE) {
                    tvLeft.setText(item.content)
                    tvRight.visibility = View.GONE
                }
            }
        }
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