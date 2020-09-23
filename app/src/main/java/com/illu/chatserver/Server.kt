package com.illu.chatserver

import io.netty.bootstrap.ServerBootstrap
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

fun main() {
    val bossGroup = NioEventLoopGroup()
    val workGroup = NioEventLoopGroup()
    try {
        val bootStrap = ServerBootstrap()
        val channelFuture = bootStrap.group(bossGroup, workGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(ServerInitializer())
            .bind("192.168.0.94", 8899)
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
            it.addLast(
                LengthFieldBasedFrameDecoder(
                    Int.MAX_VALUE,
                    0, 4, 0, 4
                )
            )
            it.addLast(LengthFieldPrepender(4))
//            it.addLast(DelimiterBasedFrameDecoder(4096, *Delimiters.lineDelimiter()))
            it.addLast(StringDecoder(CharsetUtil.UTF_8))
            it.addLast(StringEncoder(CharsetUtil.UTF_8))
            it.addLast(ServerHandler())
        }
    }

}

class ServerHandler : SimpleChannelInboundHandler<String>() {

    companion object {
        private val channelGroup = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: String?) {
        println("channelRead0: $msg")
    }

    override fun handlerAdded(ctx: ChannelHandlerContext?) {
        println("handlerAdded")
        channelGroup.add(ctx?.channel())
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        ctx?.close()
        cause?.printStackTrace()
    }

}