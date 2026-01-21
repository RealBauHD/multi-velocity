package dev.bauhd.multi.protocol.packet;

import dev.bauhd.multi.protocol.RequestPacket;
import dev.bauhd.multi.protocol.Util;
import io.netty.buffer.ByteBuf;

public final class RequestProxyNamesPacket extends RequestPacket<ProxyNamesResponsePacket> {

  private String remaining;

  public RequestProxyNamesPacket(final String remaining) {
    this.remaining = remaining;
  }

  public RequestProxyNamesPacket() {
  }

  @Override
  public void encode(ByteBuf buf) {
    super.encode(buf);
    Util.writeString(buf, this.remaining);
  }

  @Override
  public void decode(ByteBuf buf) {
    super.decode(buf);
    this.remaining = Util.readString(buf);
  }

  public String remaining() {
    return this.remaining;
  }

  @Override
  public String toString() {
    return "RequestProxyNamesPacket{" +
        "remaining='" + this.remaining + '\'' +
        '}';
  }
}
