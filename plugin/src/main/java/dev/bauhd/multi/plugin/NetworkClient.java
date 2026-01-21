package dev.bauhd.multi.plugin;

import dev.bauhd.multi.protocol.Packet;
import dev.bauhd.multi.protocol.RequestPacket;
import dev.bauhd.multi.protocol.ResponsePacket;
import dev.bauhd.multi.protocol.Util;
import dev.bauhd.multi.protocol.codec.NetworkChannel;
import dev.bauhd.multi.protocol.codec.PipelineInitializer;
import dev.bauhd.multi.protocol.packet.HelloPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class NetworkClient extends NetworkChannel {

  private final MultiVelocityPlugin plugin;
  private final EventLoopGroup eventGroup;
  private final Map<UUID, CompletableFuture<ResponsePacket>> responseFutures;
  private Channel channel;
  private boolean connected;
  private boolean reconnecting;

  public NetworkClient(final MultiVelocityPlugin plugin) {
    this.plugin = plugin;
    this.eventGroup = new MultiThreadIoEventLoopGroup(1, this.factory());
    this.responseFutures = new HashMap<>();
  }

  @Override
  public void start(final String host, final int port) {
    new Bootstrap()
        .group(this.eventGroup)
        .channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
        .handler(new PipelineInitializer(this))
        .option(ChannelOption.TCP_NODELAY, true)
        .connect(host, port)
        .addListener((ChannelFutureListener) future -> {
          if (future.isSuccess()) {
            this.channel = future.channel();
            this.plugin.logger().info("Connected to server: {}", this.channel.remoteAddress());
            this.channel.writeAndFlush(
                new HelloPacket(Util.VERSION, this.plugin.config().name(), System.currentTimeMillis()));
            this.connected = true;
          } else {
            if (!this.reconnecting) {
              this.plugin.logger().error("Connection failed: ", future.cause());
            }
            future.channel().close();
          }
        });
  }

  @Override
  public void handleConnect(Channel channel) {
  }

  @Override
  public void handleDisconnect(Channel channel) {
    this.plugin.logger().warn("Connection to backend system lost ({})", channel.remoteAddress());
    this.channel = null;
    this.connected = false;
    this.reconnecting = true;
    this.attemptReconnect();
  }

  private void attemptReconnect() {
    this.eventGroup.schedule(() -> {
      if (this.connected) {
        this.reconnecting = false;
        return;
      }

      this.plugin.logger().info("Trying to reconnect...");
      this.start(this.plugin.config().host(), this.plugin.config().port());
      this.attemptReconnect();
    }, 3, TimeUnit.SECONDS);
  }

  public void send(final Packet packet) {
    if (this.channel.eventLoop().inEventLoop()) {
      this.channel.writeAndFlush(packet);
    } else {
      this.channel.eventLoop().execute(() -> this.channel.writeAndFlush(packet));
    }
  }

  public boolean connected() {
    return this.connected;
  }

  @SuppressWarnings("unchecked")
  public <R extends ResponsePacket> CompletableFuture<R> request(final RequestPacket<R> packet) {
    final var id = UUID.randomUUID();
    final CompletableFuture<R> future = new CompletableFuture<>();
    this.responseFutures.put(id, (CompletableFuture<ResponsePacket>) future);
    packet.setId(id);
    this.send(packet);
    return future;
  }

  @Override
  public void handle(Channel channel, Packet packet) {
    this.plugin.logger().info("handle {}", packet);
    if (packet instanceof ResponsePacket response) {
      this.responseFutures.remove(response.id()).complete(response);
    } else {
      super.handle(channel, packet);
    }
  }

  @Override
  public void shutdown() {
    this.eventGroup.shutdownGracefully();
  }
}
