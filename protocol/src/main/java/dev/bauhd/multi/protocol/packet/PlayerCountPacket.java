package dev.bauhd.multi.protocol.packet;

import dev.bauhd.multi.protocol.Packet;
import dev.bauhd.multi.protocol.Util;
import io.netty.buffer.ByteBuf;

public final class PlayerCountPacket implements Packet {

  private int playerCount;

  public PlayerCountPacket(final int playerCount) {
    this.playerCount = playerCount;
  }

  public PlayerCountPacket() {
  }

  @Override
  public void encode(ByteBuf buf) {
    Util.writeVarInt(buf, this.playerCount);
  }

  @Override
  public void decode(ByteBuf buf) {
    this.playerCount = Util.readVarInt(buf);
  }

  public int playerCount() {
    return this.playerCount;
  }

  @Override
  public String toString() {
    return "PlayerCountPacket{" +
        "playerCount=" + this.playerCount +
        '}';
  }
}
