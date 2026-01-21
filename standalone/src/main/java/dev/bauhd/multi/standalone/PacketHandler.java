package dev.bauhd.multi.standalone;

import dev.bauhd.multi.protocol.object.Proxy;
import dev.bauhd.multi.protocol.packet.HelloPacket;
import dev.bauhd.multi.protocol.packet.PlayerCountPacket;
import dev.bauhd.multi.protocol.packet.ProxyNamesResponsePacket;
import dev.bauhd.multi.protocol.packet.ProxyResponsePacket;
import dev.bauhd.multi.protocol.packet.RequestProxyNamesPacket;
import dev.bauhd.multi.protocol.packet.RequestProxyPacket;
import dev.bauhd.multi.protocol.packet.StatusPacket;
import io.netty.channel.Channel;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class PacketHandler {

  private static final Logger LOGGER = LogManager.getLogger("PacketHandler");

  private final NetworkServer networkServer;
  private int cachedPlayerCount;

  public PacketHandler(final NetworkServer networkServer) {
    this.networkServer = networkServer;

    this.networkServer.registerPacketListener(HelloPacket.class, this::handleHello);
    this.networkServer.registerPacketListener(StatusPacket.class, this::handleStatus);
    this.networkServer.registerPacketListener(RequestProxyNamesPacket.class, this::handleProxyNamesRequest);
    this.networkServer.registerPacketListener(RequestProxyPacket.class, this::handleProxyRequest);
  }

  private void handleHello(final HelloPacket packet, final Channel channel) {
    if (this.networkServer.proxiesByName().containsKey(packet.name())) {
      LOGGER.warn("{} tried to connect as {}, but a proxy with this name is already connected.",
          channel.remoteAddress(), packet.name());
      channel.close();
      return;
    }
    final var proxy = new Proxy(packet.name(), packet.startTime());
    this.networkServer.proxiesByName().put(packet.name(), proxy);
    this.networkServer.proxiesByChannel().put(channel, proxy);
    LOGGER.info("{} connected. ({})", packet.name(), channel.remoteAddress());
    channel.writeAndFlush(new PlayerCountPacket(this.cachedPlayerCount));
  }

  private void handleStatus(final StatusPacket packet, final Channel channel) {
    this.networkServer.proxiesByChannel().get(channel).setStatus(packet.status());

    var playerCount = 0;
    for (final var proxy : this.networkServer.proxiesByName().values()) {
      playerCount += proxy.status().playerCount();
    }
    if (this.cachedPlayerCount != playerCount) {
      this.networkServer.send(new PlayerCountPacket(playerCount));
      this.cachedPlayerCount = playerCount;
    }
  }

  private void handleProxyNamesRequest(final RequestProxyNamesPacket packet, final Channel channel) {
    final var proxies = new ArrayList<String>();
    for (final var name : this.networkServer.proxiesByName().keySet()) {
      if (name.toLowerCase().startsWith(packet.remaining())) {
        proxies.add(name);
      }
    }
    channel.writeAndFlush(new ProxyNamesResponsePacket(packet.id(), proxies));
  }

  private void handleProxyRequest(final RequestProxyPacket packet, final Channel channel) {
    final var proxy = this.networkServer.proxiesByName().get(packet.proxy());
    if (proxy != null) {
      channel.writeAndFlush(new ProxyResponsePacket(packet.id(), proxy));
    }
  }
}
