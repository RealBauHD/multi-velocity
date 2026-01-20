package dev.bauhd.multi.protocol.codec;

import dev.bauhd.multi.protocol.PacketHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

public final class PipelineInitializer extends ChannelInitializer<Channel> {

  private final PacketHandler packetHandler;

  public PipelineInitializer(final PacketHandler packetHandler) {
    this.packetHandler = packetHandler;
  }

  @Override
  protected void initChannel(Channel channel) {
    channel.pipeline()
        .addLast("decoder", new PacketDecoder(this.packetHandler))
        .addLast("encoder", new PacketEncoder())
        .addLast("handler", new ChannelHandler(this.packetHandler));
  }
}
