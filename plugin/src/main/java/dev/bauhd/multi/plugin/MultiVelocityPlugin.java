package dev.bauhd.multi.plugin;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.bauhd.multi.plugin.listener.PingListener;
import dev.bauhd.multi.protocol.PacketHandler;
import dev.bauhd.multi.protocol.Util;
import dev.bauhd.multi.protocol.codec.PipelineInitializer;
import dev.bauhd.multi.protocol.packet.HelloPacket;
import dev.bauhd.multi.protocol.packet.StatusPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
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
  private EventLoopGroup eventGroup;
  private PacketHandler packetHandler;
  private Channel channel;
  private boolean connected;

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
    this.startClient();

    this.proxyServer.getEventManager().register(this, new PingListener(this));

    this.proxyServer.getScheduler().buildTask(this, () ->
            this.channel.eventLoop().execute(() ->
                this.channel.writeAndFlush(new StatusPacket(this.proxyServer.getPlayerCount()))))
        .repeat(5, TimeUnit.SECONDS)
        .delay(5, TimeUnit.SECONDS)
        .schedule();
  }

  private void loadConfig() {
    final var path = this.directory.resolve("config.json");
    try (final var reader = Files.newBufferedReader(path)) {
      this.config = new Gson().fromJson(reader, Config.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void startClient() {
    final boolean epoll = Epoll.isAvailable();
    final var factory = epoll ? EpollIoHandler.newFactory() : NioIoHandler.newFactory();
    this.eventGroup = new MultiThreadIoEventLoopGroup(1, factory);
    this.packetHandler = new PacketHandler();
    new Bootstrap()
        .group(this.eventGroup)
        .channel(epoll ? EpollSocketChannel.class : NioSocketChannel.class)
        .handler(new PipelineInitializer(packetHandler))
        .option(ChannelOption.TCP_NODELAY, true)
        .connect(this.config.host(), this.config.port())
        .addListener((ChannelFutureListener) future -> {
          if (future.isSuccess()) {
            this.channel = future.channel();
            this.logger.info("Connected to server: {}", this.channel.remoteAddress());
            this.channel.writeAndFlush(new HelloPacket(Util.VERSION, this.config.name()));
            this.connected = true;
          } else {
            this.logger.error("Connection failed: ", future.cause());
          }
        });
  }

  @Subscribe
  public void handle(final ProxyShutdownEvent event) {
    this.eventGroup.shutdownGracefully();
  }

  public PacketHandler packetHandler() {
    return this.packetHandler;
  }

  public boolean connected() {
    return this.connected;
  }
}
