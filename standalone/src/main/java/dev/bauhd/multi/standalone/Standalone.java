package dev.bauhd.multi.standalone;

import dev.bauhd.multi.protocol.packet.PlayerCountPacket;
import dev.bauhd.multi.protocol.packet.ProxyNamesResponsePacket;
import dev.bauhd.multi.protocol.packet.ProxyResponsePacket;
import dev.bauhd.multi.protocol.packet.RequestProxyNamesPacket;
import dev.bauhd.multi.protocol.packet.RequestProxyPacket;
import dev.bauhd.multi.protocol.packet.StatusPacket;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;

public final class Standalone {

  private final NetworkServer networkServer;
  private int cachedPlayerCount;

  public Standalone() {
    this.networkServer = new NetworkServer();
    this.networkServer.start("127.0.0.1", 2000);

    this.networkServer.registerPacketListener(StatusPacket.class, (packet, channel) -> {
      this.networkServer.proxiesByChannel().get(channel).setStatus(packet.status());

      var playerCount = 0;
      for (final var proxy : this.networkServer.proxiesByName().values()) {
        playerCount += proxy.status().playerCount();
      }
      if (this.cachedPlayerCount != playerCount) {
        this.networkServer.send(new PlayerCountPacket(playerCount));
        this.cachedPlayerCount = playerCount;
      }
    });

    this.networkServer.registerPacketListener(RequestProxyNamesPacket.class, (packet, channel) -> {
      final var proxies = new ArrayList<String>();
      for (final var name : this.networkServer.proxiesByName().keySet()) {
        if (name.toLowerCase().startsWith(packet.remaining())) {
          proxies.add(name);
        }
      }
      channel.writeAndFlush(new ProxyNamesResponsePacket(packet.id(), proxies));
    });

    this.networkServer.registerPacketListener(RequestProxyPacket.class, (packet, channel) -> {
      final var proxy = this.networkServer.proxiesByName().get(packet.proxy());
      // TODO: do something if the proxy does not exist :)
      channel.writeAndFlush(new ProxyResponsePacket(packet.id(), proxy));
    });
  }

  public void shutdown() {
    this.networkServer.shutdown();

    LogManager.shutdown(false);
  }

  public static void main(String[] args) {
    final var standalone = new Standalone();

    Runtime.getRuntime().addShutdownHook(new Thread(standalone::shutdown, "Shutdown Thread"));
  }
}
