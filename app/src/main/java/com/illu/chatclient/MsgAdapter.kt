package com.illu.chatclient

import android.view.Gravity
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.illu.chatclient.Msg.Companion.TYPE_RECEIVE
import com.illu.chatclient.Msg.Companion.TYPE_SEND
import kotlinx.android.synthetic.main.item_msg.view.*

class MsgAdapter(layoutId: Int = R.layout.item_msg) :
    BaseQuickAdapter<Msg, BaseViewHolder>(layoutId) {

    override fun convert(holder: BaseViewHolder, item: Msg) {
        holder.run {
            itemView.run {
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
    val type: Int = 0
) {

    companion object {
        val TYPE_SEND = 1
        val TYPE_RECEIVE = 2
    }
}