package dev.bauhd.multi.protocol;

import io.netty.buffer.ByteBuf;

public interface Packet {

  void encode(ByteBuf buf);

  void decode(ByteBuf buf);

  int id();
}
