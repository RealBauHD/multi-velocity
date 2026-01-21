package dev.bauhd.multi.protocol.codec;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

public final class PipelineInitializer extends ChannelInitializer<Channel> {

  private final NetworkChannel networkChannel;

  public PipelineInitializer(final NetworkChannel networkChannel) {
    this.networkChannel = networkChannel;
  }

  @Override
  protected void initChannel(Channel channel) {
    channel.pipeline()
        .addLast("decoder", new PacketDecoder(this.networkChannel.packetHandler()))
        .addLast("encoder", new PacketEncoder(this.networkChannel.packetHandler()))
        .addLast("handler", new ChannelHandler(this.networkChannel));
  }
}
