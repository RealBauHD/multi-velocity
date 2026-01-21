package dev.bauhd.multi.protocol;

import io.netty.buffer.ByteBuf;

public interface Serializable {

  void encode(ByteBuf buf);

  void decode(ByteBuf buf);
}
