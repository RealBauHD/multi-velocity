package dev.bauhd.multi.standalone;

import dev.bauhd.multi.protocol.Packet;
import dev.bauhd.multi.protocol.PacketHandler;
import dev.bauhd.multi.protocol.codec.PipelineInitializer;
import dev.bauhd.multi.protocol.packet.HelloPacket;
import dev.bauhd.multi.protocol.packet.PlayerCountPacket;
import dev.bauhd.multi.protocol.packet.StatusPacket;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Standalone {

  private final Map<String, Proxy> proxiesByName = new ConcurrentHashMap<>();
  private final Map<Channel, Proxy> proxiesByChannel = new ConcurrentHashMap<>();
  private final EventLoopGroup bossGroup;
  private final EventLoopGroup workerGroup;
  private Channel channel;
  private int cachedPlayerCount;

  public Standalone() {
    final var epoll = Epoll.isAvailable();
    final var factory = epoll ? EpollIoHandler.newFactory() : NioIoHandler.newFactory();
    this.bossGroup = new MultiThreadIoEventLoopGroup(1, factory);
    this.workerGroup = new MultiThreadIoEventLoopGroup(factory);
    final var packetHandler = new PacketHandler();

    new ServerBootstrap()
        .channelFactory(epoll ? EpollServerSocketChannel::new : NioServerSocketChannel::new)
        .group(this.bossGroup, this.workerGroup)
        .childHandler(new PipelineInitializer(packetHandler))
        .childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.IP_TOS, 24)
        .bind("127.0.0.1", 2000)
        .addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
        .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
        .addListener((ChannelFutureListener) future -> {
          this.channel = future.channel();
          if (future.isSuccess()) {
            System.out.println("Listening on " + this.channel.localAddress());
          } else {
            System.err.println("Can not bind to " + this.channel.localAddress() + future.cause());
          }
        });

    packetHandler.registerListener(HelloPacket.class, (packet, channel) -> {
      if (this.proxiesByName.containsKey(packet.name())) {
        System.out.println(channel.remoteAddress() + " tried to connect as " + packet.name()
            + ", but a proxy with this name is already connected.");
        channel.close();
        return;
      }
      final var proxy = new Proxy(packet.name());
      this.proxiesByName.put(packet.name(), proxy);
      this.proxiesByChannel.put(channel, proxy);
      System.out.println(packet.name() + " connected. (" + channel.remoteAddress() + ")");
    });

    packetHandler.registerListener(StatusPacket.class, (packet, channel) -> {
      this.proxiesByChannel.get(channel).playerCount(packet.playerCount());

      var playerCount = 0;
      for (final var proxy : this.proxiesByName.values()) {
        playerCount += proxy.playerCount();
      }
      if (this.cachedPlayerCount != playerCount) {
        this.send(new PlayerCountPacket(playerCount));
        this.cachedPlayerCount = playerCount;
      }
    });
  }

  public void send(final Packet packet) {
    for (final var channel : this.proxiesByChannel.keySet()) {
      channel.eventLoop().execute(() -> channel.writeAndFlush(packet));
    }
  }

  public void shutdown() {
    this.bossGroup.shutdownGracefully();
    this.workerGroup.shutdownGracefully();
  }

  public static void main(String[] args) {
    final var standalone = new Standalone();

    Runtime.getRuntime().addShutdownHook(new Thread(standalone::shutdown, "Shutdown Thread"));
  }
}
