package dev.bauhd.multi.protocol.codec;

import dev.bauhd.multi.protocol.Packet;
import dev.bauhd.multi.protocol.PacketRegistry;
import dev.bauhd.multi.protocol.Util;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@Sharable
final class PacketEncoder extends MessageToByteEncoder<Packet> {

  private final PacketRegistry packetRegistry;

  public PacketEncoder(final PacketRegistry packetRegistry) {
    this.packetRegistry = packetRegistry;
  }

  @Override
  protected void encode(ChannelHandlerContext context, Packet packet, ByteBuf buf) {
    Util.writeVarInt(buf, this.packetRegistry.packetId(packet.getClass()));
    packet.encode(buf);
  }
}
