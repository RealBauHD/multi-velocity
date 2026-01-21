package dev.bauhd.multi.protocol.packet;

import dev.bauhd.multi.protocol.ResponsePacket;
import dev.bauhd.multi.protocol.object.Proxy;
import io.netty.buffer.ByteBuf;
import java.util.UUID;

public final class ProxyResponsePacket extends ResponsePacket {

  private Proxy proxy;

  public ProxyResponsePacket(final UUID id, final Proxy proxy) {
    super(id);
    this.proxy = proxy;
  }

  public ProxyResponsePacket() {
    super(null);
  }

  @Override
  public void encode(ByteBuf buf) {
    super.encode(buf);
    this.proxy.encode(buf);
  }

  @Override
  public void decode(ByteBuf buf) {
    super.decode(buf);
    this.proxy = new Proxy();
    this.proxy.decode(buf);
  }

  public Proxy proxy() {
    return this.proxy;
  }

  @Override
  public String toString() {
    return "ProxyResponsePacket{" +
        "proxy=" + this.proxy +
        '}';
  }
}
