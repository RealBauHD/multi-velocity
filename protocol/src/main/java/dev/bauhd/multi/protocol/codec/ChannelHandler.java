package dev.bauhd.multi.protocol.codec;

import dev.bauhd.multi.protocol.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

final class ChannelHandler extends SimpleChannelInboundHandler<Packet> {

  private final NetworkChannel networkChannel;

  public ChannelHandler(final NetworkChannel networkChannel) {
    this.networkChannel = networkChannel;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext context, Packet packet) {
    this.networkChannel.handle(context.channel(), packet);
  }

  @Override
  public void channelActive(ChannelHandlerContext context) {
    System.out.println("Client connected: " + context.channel().remoteAddress());
  }

  @Override
  public void channelInactive(ChannelHandlerContext context) {
    System.out.println("Client disconnected: " + context.channel().remoteAddress());
    this.networkChannel.handleDisconnect(context.channel());
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
      cause.printStackTrace();
  }
}
