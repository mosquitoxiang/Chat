package com.illu.chatclient

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.netty.bootstrap.Bootstrap
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
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnConnect.setOnClickListener {
            val userId = etUserid.text.toString().trim()
            if (userId.isNotEmpty()) {
                thread {
                    Client("192.168.0.94", 8899, userId).start()
                }
            }
        }
    }
}

class Client(private val host: String, private val port: Int, private val userId: String) {

    fun start() {
        val group = NioEventLoopGroup()
        try {
            val bootStrap = Bootstrap()
            val channelFuture = bootStrap.group(group)
                .channel(NioSocketChannel::class.java)
                .remoteAddress(host, port)
                .handler(ClientInitializer(userId))
                .connect()
                .sync()
            channelFuture.channel().closeFuture().sync()
        } finally {
            group.shutdownGracefully()
        }
    }
}

class ClientInitializer(private val userId: String) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel?) {
        val pipeline = ch?.pipeline()
        pipeline?.let {
            it.addLast(
                LengthFieldBasedFrameDecoder(
                    Int.MAX_VALUE,
                    0, 4, 0, 4
                )
            )
            it.addLast(LengthFieldPrepender(4))
//            it.addLast(LineBasedFrameDecoder(1024))
            it.addLast(StringDecoder(CharsetUtil.UTF_8))
            it.addLast(StringEncoder(CharsetUtil.UTF_8))
            it.addLast(ClientHandler(userId))
        }
    }
}

class ClientHandler(private val userId: String) : SimpleChannelInboundHandler<String>() {

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: String?) {
        ctx?.writeAndFlush("hello")
    }

    override fun handlerAdded(ctx: ChannelHandlerContext?) {
        println("handlerAdded ")
        ctx?.writeAndFlush("userId")
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        cause?.printStackTrace()
        ctx?.close()
    }

}