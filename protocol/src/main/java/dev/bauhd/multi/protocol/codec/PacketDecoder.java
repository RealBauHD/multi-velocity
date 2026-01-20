package dev.bauhd.multi.protocol.codec;

import dev.bauhd.multi.protocol.PacketHandler;
import dev.bauhd.multi.protocol.Util;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

public final class PacketDecoder extends ByteToMessageDecoder {

  private final PacketHandler packetHandler;

  public PacketDecoder(final PacketHandler packetHandler) {
    this.packetHandler = packetHandler;
  }

  @Override
  protected void decode(ChannelHandlerContext context, ByteBuf buf, List<Object> list) {
    final var id = Util.readVarInt(buf);
    final var packet = this.packetHandler.packetId(id);
    packet.decode(buf);
    list.add(packet);
  }
}
