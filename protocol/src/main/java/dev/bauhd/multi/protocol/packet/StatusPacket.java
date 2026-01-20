package dev.bauhd.multi.protocol.packet;

import dev.bauhd.multi.protocol.Packet;
import dev.bauhd.multi.protocol.Util;
import io.netty.buffer.ByteBuf;

public final class StatusPacket implements Packet {

  private int playerCount;

  public StatusPacket(final int playerCount) {
    this.playerCount = playerCount;
  }

  public StatusPacket() {
  }

  @Override
  public void encode(ByteBuf buf) {
    Util.writeVarInt(buf, this.playerCount);
  }

  @Override
  public void decode(ByteBuf buf) {
    this.playerCount = Util.readVarInt(buf);
  }

  @Override
  public int id() {
    return 1;
  }

  public int playerCount() {
    return this.playerCount;
  }

  @Override
  public String toString() {
    return "StatusPacket{" +
        "playerCount=" + this.playerCount +
        '}';
  }
}
