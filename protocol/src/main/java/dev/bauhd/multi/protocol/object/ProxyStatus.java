package dev.bauhd.multi.protocol.object;

import dev.bauhd.multi.protocol.Serializable;
import dev.bauhd.multi.protocol.Util;
import io.netty.buffer.ByteBuf;

public final class ProxyStatus implements Serializable {

  private int playerCount;
  private long maxMemory;
  private long freeMemory;
  private long totalMemory;

  public ProxyStatus(
      final int playerCount, final long maxMemory, final long freeMemory, final long totalMemory
  ) {
    this.playerCount = playerCount;
    this.maxMemory = maxMemory;
    this.freeMemory = freeMemory;
    this.totalMemory = totalMemory;
  }

  public ProxyStatus() {
  }

  @Override
  public void encode(ByteBuf buf) {
    Util.writeVarInt(buf, this.playerCount);
    buf.writeLong(this.maxMemory);
    buf.writeLong(this.freeMemory);
    buf.writeLong(this.totalMemory);
  }

  @Override
  public void decode(ByteBuf buf) {
    this.playerCount = Util.readVarInt(buf);
    this.maxMemory = buf.readLong();
    this.freeMemory = buf.readLong();
    this.totalMemory = buf.readLong();
  }

  public int playerCount() {
    return this.playerCount;
  }

  public long maxMemory() {
    return this.maxMemory;
  }

  public long freeMemory() {
    return this.freeMemory;
  }

  public long totalMemory() {
    return this.totalMemory;
  }

  @Override
  public String toString() {
    return "ProxyStatus{" +
        "playerCount=" + this.playerCount +
        ", maxMemory=" + this.maxMemory +
        ", freeMemory=" + this.freeMemory +
        ", totalMemory=" + this.totalMemory +
        '}';
  }
}
