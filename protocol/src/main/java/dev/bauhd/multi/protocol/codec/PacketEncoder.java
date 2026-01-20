package dev.bauhd.multi.protocol.codec;

import dev.bauhd.multi.protocol.Packet;
import dev.bauhd.multi.protocol.Util;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public final class PacketEncoder extends MessageToByteEncoder<Packet> {

  @Override
  protected void encode(ChannelHandlerContext context, Packet packet, ByteBuf buf) {
    Util.writeVarInt(buf, packet.id());
    packet.encode(buf);
  }
}
