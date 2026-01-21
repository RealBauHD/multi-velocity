package dev.bauhd.multi.protocol.codec;

import dev.bauhd.multi.protocol.Packet;
import dev.bauhd.multi.protocol.PacketRegistry;
import io.netty.channel.Channel;
import io.netty.channel.IoHandlerFactory;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.nio.NioIoHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class NetworkChannel {

  private final PacketRegistry packetRegistry = new PacketRegistry();
  private final Map<Class<?>, BiConsumer<Packet, Channel>> listener = new HashMap<>();

  public PacketRegistry packetHandler() {
    return this.packetRegistry;
  }

  @SuppressWarnings("unchecked")
  public <T> void registerPacketListener(
      final Class<T> clazz, final BiConsumer<T, Channel> packet
  ) {
    this.listener.put(clazz, (BiConsumer<Packet, Channel>) packet);
  }

  public void handlePacket(final Channel channel, final Packet packet) {
    final var listener = this.listener.get(packet.getClass());
    if (listener != null) {
        listener.accept(packet, channel);
    }
  }

  public IoHandlerFactory factory() {
    return Epoll.isAvailable() ? EpollIoHandler.newFactory() : NioIoHandler.newFactory();
  }

  public abstract void start(final String host, final int port);

  public abstract void handleConnect(final Channel channel);

  public abstract void handleDisconnect(final Channel channel);

  public abstract void shutdown();
}
