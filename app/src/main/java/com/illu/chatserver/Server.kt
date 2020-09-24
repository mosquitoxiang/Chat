package com.illu.chatserver

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.group.DefaultChannelGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.DelimiterBasedFrameDecoder
import io.netty.handler.codec.Delimiters
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import io.netty.util.CharsetUtil
import io.netty.util.concurrent.GlobalEventExecutor
import java.util.concurrent.ConcurrentHashMap

fun main() {
    val bossGroup = NioEventLoopGroup()
    val workGroup = NioEventLoopGroup()
    try {
        val bootStrap = ServerBootstrap()
        val channelFuture = bootStrap.group(bossGroup, workGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(ServerInitializer())
            .bind("192.168.31.240", 8899)
            .sync()
        channelFuture.channel().closeFuture().sync()
    } finally {
        bossGroup.shutdownGracefully()
        workGroup.shutdownGracefully()
    }
}

class ServerInitializer : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel?) {
        val pipeline = ch?.pipeline()
        pipeline?.let {
            it.addLast(DelimiterBasedFrameDecoder(4096, *Delimiters.lineDelimiter()))
            it.addLast(StringDecoder(CharsetUtil.UTF_8))
            it.addLast(StringEncoder(CharsetUtil.UTF_8))
            it.addLast(ServerHandler())
        }
    }

}

class ServerHandler : SimpleChannelInboundHandler<String>() {

    companion object {
        private var map = ConcurrentHashMap<String, Channel>()
        private val channelGroup = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)
        const val BIND_ID = "bindId"
        const val CONNECT_ID = "connectId"
        const val CONTENT = "content"
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: String?) {
        println("channelRead0: $msg")
        var content = ""
        if (msg!!.contains(BIND_ID)) {
            content = msg.replace(BIND_ID, "")
            map[content] = ctx?.channel()!!
        } else if (msg.contains(CONNECT_ID) && msg.contains(CONTENT)) {
            var connectId = msg.replace("connectId", "")
            connectId = connectId.substring(0, connectId.indexOf("c"))
            var content = msg.replace("connectId", "")
            content = content.substring(content.indexOf("t"), content.length).replace("tent", "")
            map.forEach {
                if (connectId.equals(it.key)) {
                    println("发送的内容: " + content)
                    it.value.writeAndFlush(content + "\r\n")
                }
            }
        }
    }

    override fun handlerAdded(ctx: ChannelHandlerContext?) {
        channelGroup.add(ctx?.channel())
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        ctx?.close()
        cause?.printStackTrace()
    }

}