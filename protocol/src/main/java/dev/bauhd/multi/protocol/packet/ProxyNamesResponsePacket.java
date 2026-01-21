package dev.bauhd.multi.protocol.packet;

import dev.bauhd.multi.protocol.ResponsePacket;
import dev.bauhd.multi.protocol.Util;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ProxyNamesResponsePacket extends ResponsePacket {

  private List<String> proxies;

  public ProxyNamesResponsePacket(final UUID id, final List<String> proxies) {
    super(id);
    this.proxies = proxies;
  }

  public ProxyNamesResponsePacket() {
    super(null);
  }

  @Override
  public void encode(ByteBuf buf) {
    super.encode(buf);
    Util.writeVarInt(buf, this.proxies.size());
    for (final var proxy : this.proxies) {
      Util.writeString(buf, proxy);
    }
  }

  @Override
  public void decode(ByteBuf buf) {
    super.decode(buf);
    final var size = Util.readVarInt(buf);
    this.proxies = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      this.proxies.add(Util.readString(buf));
    }
  }

  public List<String> proxies() {
    return this.proxies;
  }

  @Override
  public String toString() {
    return "ProxyNamesResponsePacket{" +
        "proxies=" + this.proxies +
        '}';
  }
}
