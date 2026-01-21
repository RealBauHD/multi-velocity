package dev.bauhd.multi.protocol.codec;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

public final class PipelineInitializer extends ChannelInitializer<Channel> {

  private final NetworkChannel networkChannel;
  private final PacketEncoder packetEncoder;
  private final ChannelHandler channelHandler;

  public PipelineInitializer(final NetworkChannel networkChannel) {
    this.networkChannel = networkChannel;
    this.packetEncoder = new PacketEncoder(this.networkChannel.packetHandler());
    this.channelHandler = new ChannelHandler(this.networkChannel);
  }

  @Override
  protected void initChannel(Channel channel) {
    channel.pipeline()
        .addLast("decoder", new PacketDecoder(this.networkChannel.packetHandler()))
        .addLast("encoder", this.packetEncoder)
        .addLast("handler", this.channelHandler);
  }
}
