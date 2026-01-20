package dev.bauhd.multi.protocol.packet;

import dev.bauhd.multi.protocol.Packet;
import dev.bauhd.multi.protocol.Util;
import io.netty.buffer.ByteBuf;

public final class HelloPacket implements Packet {

  private int version;
  private String name;

  public HelloPacket(final int version, final String name) {
    this.version = version;
    this.name = name;
  }

  public HelloPacket() {
  }

  @Override
  public void encode(ByteBuf buf) {
    Util.writeVarInt(buf, this.version);
    Util.writeString(buf, this.name);
  }

  @Override
  public void decode(ByteBuf buf) {
    this.version = Util.readVarInt(buf);
    this.name = Util.readString(buf);
  }

  @Override
  public int id() {
    return 0;
  }

  public int version() {
    return this.version;
  }

  public String name() {
    return this.name;
  }

  @Override
  public String toString() {
    return "HelloPacket{" +
        "version=" + this.version +
        ", name='" + this.name + '\'' +
        '}';
  }
}
