package dev.bauhd.multi.standalone;

import dev.bauhd.multi.protocol.Packet;
import dev.bauhd.multi.protocol.codec.NetworkChannel;
import dev.bauhd.multi.protocol.codec.PipelineInitializer;
import dev.bauhd.multi.protocol.object.Proxy;
import dev.bauhd.multi.protocol.packet.HelloPacket;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class NetworkServer extends NetworkChannel {

  private static final Logger LOGGER = LogManager.getLogger("NetworkServer");

  private final Map<String, Proxy> proxiesByName = new ConcurrentHashMap<>();
  private final Map<Channel, Proxy> proxiesByChannel = new ConcurrentHashMap<>();
  private final EventLoopGroup bossGroup;
  private final EventLoopGroup workerGroup;
  private Channel channel;

  public NetworkServer() {
    final var factory = this.factory();
    this.bossGroup = new MultiThreadIoEventLoopGroup(1, factory);
    this.workerGroup = new MultiThreadIoEventLoopGroup(factory);

    this.registerPacketListener(HelloPacket.class, (packet, channel) -> {
      if (this.proxiesByName.containsKey(packet.name())) {
        LOGGER.warn("{} tried to connect as {}, but a proxy with this name is already connected.",
            channel.remoteAddress(), packet.name());
        channel.close();
        return;
      }
      final var proxy = new Proxy(packet.name(), packet.startTime());
      this.proxiesByName.put(packet.name(), proxy);
      this.proxiesByChannel.put(channel, proxy);
      LOGGER.info("{} connected. ({})", packet.name(), channel.remoteAddress());
    });
  }

  @Override
  public void start(final String host, final int port) {
    new ServerBootstrap()
        .channelFactory(
            Epoll.isAvailable() ? EpollServerSocketChannel::new : NioServerSocketChannel::new)
        .group(this.bossGroup, this.workerGroup)
        .childHandler(new PipelineInitializer(this))
        .childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.IP_TOS, 24)
        .bind(host, port)
        .addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
        .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
        .addListener((ChannelFutureListener) future -> {
          this.channel = future.channel();
          if (future.isSuccess()) {
            LOGGER.info("Listening on {}", this.channel.localAddress());
          } else {
            LOGGER.error("Can not bind to {}", this.channel.localAddress(), future.cause());
          }
        });
  }

  @Override
  public void handleDisconnect(Channel channel) {
    final var proxy = this.proxiesByChannel.remove(channel);
    if (proxy != null) {
      this.proxiesByName.remove(proxy.name());
    }
  }

  @Override
  public void shutdown() {
    this.bossGroup.shutdownGracefully();
    this.workerGroup.shutdownGracefully();
  }

  public void send(final Packet packet) {
    for (final var channel : this.proxiesByChannel.keySet()) {
      channel.eventLoop().execute(() -> channel.writeAndFlush(packet));
    }
  }

  public Map<String, Proxy> proxiesByName() {
    return this.proxiesByName;
  }

  public Map<Channel, Proxy> proxiesByChannel() {
    return this.proxiesByChannel;
  }
}
