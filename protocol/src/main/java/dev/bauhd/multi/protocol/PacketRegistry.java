package dev.bauhd.multi.protocol;

import dev.bauhd.multi.protocol.packet.HelloPacket;
import dev.bauhd.multi.protocol.packet.PlayerCountPacket;
import dev.bauhd.multi.protocol.packet.ProxyNamesResponsePacket;
import dev.bauhd.multi.protocol.packet.ProxyResponsePacket;
import dev.bauhd.multi.protocol.packet.RequestProxyNamesPacket;
import dev.bauhd.multi.protocol.packet.RequestProxyPacket;
import dev.bauhd.multi.protocol.packet.StatusPacket;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.function.Supplier;

public final class PacketRegistry {

  private final IntObjectMap<Supplier<Packet>> packetSupplier = new IntObjectHashMap<>();
  private final Object2IntMap<Class<Packet>> packetIds = new Object2IntOpenHashMap<>();

  public PacketRegistry() {
    this.register(0x00, HelloPacket.class, HelloPacket::new);
    this.register(0x01, StatusPacket.class, StatusPacket::new);
    this.register(0x02, PlayerCountPacket.class, PlayerCountPacket::new);
    this.register(0x03, RequestProxyNamesPacket.class, RequestProxyNamesPacket::new);
    this.register(0x04, ProxyNamesResponsePacket.class, ProxyNamesResponsePacket::new);
    this.register(0x05, RequestProxyPacket.class, RequestProxyPacket::new);
    this.register(0x06, ProxyResponsePacket.class, ProxyResponsePacket::new);
  }

  @SuppressWarnings("unchecked")
  private <P extends Packet> void register(
      final int id, final Class<P> clazz, final Supplier<P> supplier
  ) {
    this.packetSupplier.put(id, (Supplier<Packet>) supplier);
    this.packetIds.put((Class<Packet>) clazz, id);
  }

  public Packet packet(final int id) {
    return this.packetSupplier.get(id).get();
  }

  public int packetId(final Class<?> clazz) {
    return this.packetIds.getInt(clazz);
  }
}
