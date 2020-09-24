package com.illu.chatclient

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.util.CharsetUtil
import org.greenrobot.eventbus.EventBus

class Client {

    private var host: String = ""
    private var port: Int = 0

    companion object {
        private var channelFuture: ChannelFuture? = null
    }

    constructor()

    constructor(host: String, port: Int) {
        this.host = host
        this.port = port
    }

    fun start() {
        val group = NioEventLoopGroup()
        try {
            val bootStrap = Bootstrap()
            channelFuture = bootStrap.group(group)
                .channel(NioSocketChannel::class.java)
                .remoteAddress(host, port)
                .handler(ClientInitializer())
                .connect()
                .sync()
            channelFuture?.channel()?.closeFuture()?.sync()
        } finally {
            group.shutdownGracefully()
        }
    }

    fun bind(bindId: String) {
        channelFuture?.channel()?.writeAndFlush("bindId" + bindId + "\r\n")
    }

    fun connect(connectId: String) {
        channelFuture?.channel()?.writeAndFlush("connectId" + connectId + "\r\n")
    }

    fun sendMsg(connectId: String, msg: String, callback: ResultCallback) {
        channelFuture?.channel()?.writeAndFlush("connectId" + connectId+ "content" + msg + "\r\n")
        channelFuture?.isSuccess
        callback.send(true)
    }
}

class ClientInitializer : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel?) {
        val pipeline = ch?.pipeline()
        pipeline?.let {
            it.addLast(LineBasedFrameDecoder(1024))
            it.addLast(StringDecoder(CharsetUtil.UTF_8))
            it.addLast(StringEncoder(CharsetUtil.UTF_8))
            it.addLast(ClientHandler())
        }
    }
}

class ClientHandler : SimpleChannelInboundHandler<String>() {

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: String?) {
        println("收到的内容: " + msg)
        EventBus.getDefault().post(msg)
    }

    override fun handlerAdded(ctx: ChannelHandlerContext?) {
        println("handlerAdded")
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        cause?.printStackTrace()
        ctx?.close()
    }

}