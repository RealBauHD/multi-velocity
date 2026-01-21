package dev.bauhd.multi.standalone;

import org.apache.logging.log4j.LogManager;

public final class Standalone {

  private final NetworkServer networkServer;

  public Standalone() {
    this.networkServer = new NetworkServer();
    this.networkServer.start("127.0.0.1", 2000);

    new PacketHandler(this.networkServer);
  }

  public void shutdown() {
    this.networkServer.shutdown();

    LogManager.shutdown(false);
  }

  public static void main(String[] args) {
    final var standalone = new Standalone();

    Runtime.getRuntime().addShutdownHook(new Thread(standalone::shutdown, "Shutdown Thread"));
  }
}
