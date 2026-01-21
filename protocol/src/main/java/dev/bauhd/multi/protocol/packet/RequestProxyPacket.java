package dev.bauhd.multi.protocol.packet;

import dev.bauhd.multi.protocol.RequestPacket;
import dev.bauhd.multi.protocol.Util;
import io.netty.buffer.ByteBuf;

public final class RequestProxyPacket extends RequestPacket<ProxyResponsePacket> {

  private String proxy;

  public RequestProxyPacket(final String proxy) {
    this.proxy = proxy;
  }

  public RequestProxyPacket() {
  }

  @Override
  public void encode(ByteBuf buf) {
    super.encode(buf);
    Util.writeString(buf, this.proxy);
  }

  @Override
  public void decode(ByteBuf buf) {
    super.decode(buf);
    this.proxy = Util.readString(buf);
  }

  public String proxy() {
    return this.proxy;
  }

  @Override
  public String toString() {
    return "RequestProxyPacket{" +
        "proxy='" + this.proxy + '\'' +
        '}';
  }
}
