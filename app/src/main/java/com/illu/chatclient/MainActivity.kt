package com.illu.chatclient

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.util.CharsetUtil
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: MsgAdapter
    private var msgList = mutableListOf<Msg>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.getDefault().register(this)
        msgList = ArrayList()

        adapter = MsgAdapter().apply {
            bindToRecyclerView(rv)
        }

        thread {
            Client("192.168.31.240", 8899).start()
        }

        btnBind.setOnClickListener {
            val bindId = etSelfId.text.toString().trim()
            if (bindId.isNotEmpty()) {
                Client().bind(bindId)
            }
        }

        btnConnect.setOnClickListener {
            val connectId = etConnectId.text.toString().trim()
            if (connectId.isNotEmpty()) {
                Client().connect(connectId)
            }
        }

        btnSend.setOnClickListener {
            val connectId = etConnectId.text.toString().trim()
            val msg = etContent.text.toString().trim()
            if (msg.isNotEmpty() && connectId.isNotEmpty()) {
                Client().sendMsg(connectId, msg, object : ResultCallback{
                    override fun send(isSuccess: Boolean) {
                        val msg = Msg(msg, Msg.TYPE_SEND)
                        msgList.add(msg)
                        adapter.setNewData(msgList)
                        rv.smoothScrollToPosition(msgList.size-1)
                    }
                })
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(msg: String) {
        val msg = Msg(msg, Msg.TYPE_RECEIVE)
        msgList.add(msg)
        adapter.setNewData(msgList)
        rv.smoothScrollToPosition(msgList.size-1)
    }
}