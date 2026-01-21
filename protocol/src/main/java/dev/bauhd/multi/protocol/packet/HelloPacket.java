package dev.bauhd.multi.protocol.packet;

import dev.bauhd.multi.protocol.Packet;
import dev.bauhd.multi.protocol.Util;
import io.netty.buffer.ByteBuf;

public final class HelloPacket implements Packet {

  private int version;
  private String name;
  private long startTime;

  public HelloPacket(final int version, final String name, final long startTime) {
    this.version = version;
    this.name = name;
    this.startTime = startTime;
  }

  public HelloPacket() {
  }

  @Override
  public void encode(ByteBuf buf) {
    Util.writeVarInt(buf, this.version);
    Util.writeString(buf, this.name);
    buf.writeLong(this.startTime);
  }

  @Override
  public void decode(ByteBuf buf) {
    this.version = Util.readVarInt(buf);
    this.name = Util.readString(buf);
    this.startTime = buf.readLong();
  }

  public int version() {
    return this.version;
  }

  public String name() {
    return this.name;
  }

  public long startTime() {
    return this.startTime;
  }

  @Override
  public String toString() {
    return "HelloPacket{" +
        "version=" + this.version +
        ", name='" + this.name + '\'' +
        ", startTime=" + this.startTime +
        '}';
  }
}
