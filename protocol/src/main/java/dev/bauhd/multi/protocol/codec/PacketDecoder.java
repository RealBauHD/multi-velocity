package dev.bauhd.multi.protocol.codec;

import dev.bauhd.multi.protocol.PacketRegistry;
import dev.bauhd.multi.protocol.Util;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

final class PacketDecoder extends ByteToMessageDecoder {

  private final PacketRegistry packetRegistry;

  public PacketDecoder(final PacketRegistry packetRegistry) {
    this.packetRegistry = packetRegistry;
  }

  @Override
  protected void decode(ChannelHandlerContext context, ByteBuf buf, List<Object> list) {
    final var id = Util.readVarInt(buf);
    final var packet = this.packetRegistry.packet(id);
    packet.decode(buf);
    list.add(packet);
  }
}
