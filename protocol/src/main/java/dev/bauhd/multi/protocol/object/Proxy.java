package dev.bauhd.multi.protocol.object;

import dev.bauhd.multi.protocol.Serializable;
import dev.bauhd.multi.protocol.Util;
import io.netty.buffer.ByteBuf;

public final class Proxy implements Serializable {

  private String name;
  private long startTime;
  private ProxyStatus status;

  public Proxy(final String name, final long startTime) {
    this.name = name;
    this.startTime = startTime;
    this.status = new ProxyStatus();
  }

  public Proxy() {
  }

  @Override
  public void encode(ByteBuf buf) {
    Util.writeString(buf, this.name);
    buf.writeLong(this.startTime);
    this.status.encode(buf);
  }

  @Override
  public void decode(ByteBuf buf) {
    this.name = Util.readString(buf);
    this.startTime = buf.readLong();
    this.status = new ProxyStatus();
    this.status.decode(buf);
  }

  public String name() {
    return this.name;
  }

  public long startTime() {
    return this.startTime;
  }

  public ProxyStatus status() {
    return this.status;
  }

  public void setStatus(final ProxyStatus status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "Proxy{" +
        "name='" + this.name + '\'' +
        ", startTime=" + this.startTime +
        ", status=" + this.status +
        '}';
  }
}
