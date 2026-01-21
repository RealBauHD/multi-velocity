package dev.bauhd.multi.plugin.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent.PreLoginComponentResult;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing.Version;
import dev.bauhd.multi.plugin.MultiVelocityPlugin;
import dev.bauhd.multi.protocol.packet.PlayerCountPacket;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Listener {

  private final MultiVelocityPlugin plugin;
  private int playerCount;

  public Listener(final MultiVelocityPlugin plugin) {
    this.plugin = plugin;
    plugin.networkClient().registerPacketListener(PlayerCountPacket.class, (packet, channel) ->
        this.playerCount = packet.playerCount());
  }

  @Subscribe
  public void handle(final ProxyPingEvent event) {
    if (this.plugin.networkClient().connected()) {
      event.setPing(event.getPing().asBuilder().onlinePlayers(this.playerCount).build());
    } else {
      event.setPing(event.getPing().asBuilder()
          .version(new Version(-1, "multi-velocity"))
          .description(this.noConnection())
          .build());
    }
  }

  @Subscribe
  public void handle(final PreLoginEvent event) {
    if (!this.plugin.networkClient().connected()) {
      event.setResult(PreLoginComponentResult.denied(this.noConnection()));
    }
  }

  private Component noConnection() {
    return Component.text(
        this.plugin.config().name() + " is not connected to the multi-velocity backend.",
        NamedTextColor.RED);
  }
}
