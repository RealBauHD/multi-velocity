package dev.bauhd.multi.plugin.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import dev.bauhd.multi.plugin.MultiVelocityPlugin;
import dev.bauhd.multi.protocol.packet.PlayerCountPacket;

public final class PingListener {

  private int playerCount;

  public PingListener(final MultiVelocityPlugin plugin) {
    plugin.networkClient().registerPacketListener(PlayerCountPacket.class, (packet, channel) ->
        this.playerCount = packet.playerCount());
  }

  @Subscribe
  public void handle(final ProxyPingEvent event) {
    event.setPing(event.getPing().asBuilder().onlinePlayers(this.playerCount).build());
  }
}
