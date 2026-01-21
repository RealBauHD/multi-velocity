package dev.bauhd.multi.protocol.packet;

import dev.bauhd.multi.protocol.Packet;
import dev.bauhd.multi.protocol.object.ProxyStatus;
import io.netty.buffer.ByteBuf;

public final class StatusPacket implements Packet {

  private ProxyStatus status;

  public StatusPacket(final ProxyStatus status) {
    this.status = status;
  }

  public StatusPacket() {
  }

  @Override
  public void encode(ByteBuf buf) {
    this.status.encode(buf);
  }

  @Override
  public void decode(ByteBuf buf) {
    this.status = new ProxyStatus();
    this.status.decode(buf);
  }

  public ProxyStatus status() {
    return this.status;
  }

  @Override
  public String toString() {
    return "StatusPacket{" +
        "status=" + this.status +
        '}';
  }
}
