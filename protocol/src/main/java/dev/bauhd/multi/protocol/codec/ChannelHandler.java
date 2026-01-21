package dev.bauhd.multi.protocol.codec;

import dev.bauhd.multi.protocol.Packet;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@Sharable
final class ChannelHandler extends SimpleChannelInboundHandler<Packet> {

  private final NetworkChannel networkChannel;

  public ChannelHandler(final NetworkChannel networkChannel) {
    this.networkChannel = networkChannel;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext context, Packet packet) {
    this.networkChannel.handlePacket(context.channel(), packet);
  }

  @Override
  public void channelActive(ChannelHandlerContext context) {
    this.networkChannel.handleConnect(context.channel());
  }

  @Override
  public void channelInactive(ChannelHandlerContext context) {
    this.networkChannel.handleDisconnect(context.channel());
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
      cause.printStackTrace();
  }
}
