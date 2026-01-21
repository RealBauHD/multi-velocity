package dev.bauhd.multi.plugin;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.bauhd.multi.plugin.command.StatusCommand;
import dev.bauhd.multi.plugin.listener.Listener;
import dev.bauhd.multi.protocol.object.ProxyStatus;
import dev.bauhd.multi.protocol.packet.StatusPacket;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

@Plugin(id = "multi-velocity", authors = "BauHD", version = "1.0.0")
public final class MultiVelocityPlugin {

  private final ProxyServer proxyServer;
  private final Logger logger;
  private final Path directory;
  private Config config;
  private NetworkClient networkClient;

  @Inject
  public MultiVelocityPlugin(
      final ProxyServer proxyServer, final Logger logger, final @DataDirectory Path directory
  ) {
    this.proxyServer = proxyServer;
    this.logger = logger;
    this.directory = directory;
  }

  @Subscribe
  public void handle(final ProxyInitializeEvent event) {
    this.loadConfig();

    this.networkClient = new NetworkClient(this);
    this.networkClient.start(this.config.host(), this.config.port());

    this.proxyServer.getEventManager().register(this, new Listener(this));

    this.proxyServer.getScheduler().buildTask(this, () -> {
          if (this.networkClient.connected()) {
            final var runtime = Runtime.getRuntime();
            final var status = new ProxyStatus(this.proxyServer.getPlayerCount(),
                runtime.maxMemory(), runtime.freeMemory(), runtime.totalMemory());
            this.networkClient.send(new StatusPacket(status));
          }
        })
        .repeat(5, TimeUnit.SECONDS)
        .delay(5, TimeUnit.SECONDS)
        .schedule();

    this.proxyServer.getCommandManager().register(this.proxyServer.getCommandManager()
        .metaBuilder("status").plugin(this).build(), StatusCommand.get(this));
  }

  private void loadConfig() {
    final var path = this.directory.resolve("config.json");
    try (final var reader = Files.newBufferedReader(path)) {
      this.config = new Gson().fromJson(reader, Config.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Subscribe
  public void handle(final ProxyShutdownEvent event) {
    this.networkClient.shutdown();
  }

  public Logger logger() {
    return this.logger;
  }

  public Config config() {
    return this.config;
  }

  public NetworkClient networkClient() {
    return this.networkClient;
  }
}
