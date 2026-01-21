package dev.bauhd.multi.protocol;

import io.netty.buffer.ByteBuf;
import java.util.UUID;

public abstract class ResponsePacket implements Packet {

  private UUID id;

  public ResponsePacket(final UUID id) {
    this.id = id;
  }

  @Override
  public void encode(ByteBuf buf) {
    Util.writeUniqueId(buf, this.id);
  }

  @Override
  public void decode(ByteBuf buf) {
    this.id = Util.readUniqueId(buf);
  }

  public UUID id() {
    return this.id;
  }
}
