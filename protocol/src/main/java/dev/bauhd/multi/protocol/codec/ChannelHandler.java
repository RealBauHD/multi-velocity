package dev.bauhd.multi.protocol.codec;

import dev.bauhd.multi.protocol.Packet;
import dev.bauhd.multi.protocol.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

final class ChannelHandler extends SimpleChannelInboundHandler<Packet> {

  private final PacketHandler packetHandler;

  public ChannelHandler(final PacketHandler packetHandler) {
    this.packetHandler = packetHandler;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext context, Packet packet) {
    this.packetHandler.handle(context.channel(), packet);
  }

  @Override
  public void channelActive(ChannelHandlerContext context) {
    System.out.println("Client connected: " + context.channel().remoteAddress());
  }

  @Override
  public void channelInactive(ChannelHandlerContext context) {
    System.out.println("Client disconnected: " + context.channel().remoteAddress());
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
      cause.printStackTrace();
  }
}
