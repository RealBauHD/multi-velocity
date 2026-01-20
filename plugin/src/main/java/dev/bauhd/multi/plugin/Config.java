package dev.bauhd.multi.plugin;

public final class Config {

  private String name;
  private String host;
  private int port;

  public String name() {
    return this.name;
  }

  public String host() {
    return this.host;
  }

  public int port() {
    return this.port;
  }
}
