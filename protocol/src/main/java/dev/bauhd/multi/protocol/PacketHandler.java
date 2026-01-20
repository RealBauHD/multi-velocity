package dev.bauhd.multi.protocol;

import dev.bauhd.multi.protocol.packet.HelloPacket;
import dev.bauhd.multi.protocol.packet.PlayerCountPacket;
import dev.bauhd.multi.protocol.packet.StatusPacket;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class PacketHandler {

  private final Map<Integer, Supplier<Packet>> packetSupplier = new HashMap<>();
  private final Map<Class<?>, List<BiConsumer<Packet, Channel>>> listener = new HashMap<>();

  public PacketHandler() {
    this.packetSupplier.put(0, HelloPacket::new);
    this.packetSupplier.put(1, StatusPacket::new);
    this.packetSupplier.put(2, PlayerCountPacket::new);
  }

  public Packet packetId(final int id) {
    return this.packetSupplier.get(id).get();
  }

  @SuppressWarnings("unchecked")
  public <T> void registerListener(final Class<T> clazz, final BiConsumer<T, Channel> packet) {
    final var listeners = this.listener.computeIfAbsent(clazz, aClass -> new ArrayList<>());
    listeners.add((BiConsumer<Packet, Channel>) packet);
  }

  public void handle(final Channel channel, final Packet packet) {
    System.out.println("handle packet " + packet);
    final var listeners = this.listener.get(packet.getClass());
    if (listeners != null) {
      for (final var consumer : listeners) {
        consumer.accept(packet, channel);
      }
    }
  }
}
