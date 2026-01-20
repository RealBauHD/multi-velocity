package dev.bauhd.multi.standalone;

public final class Proxy {

  private final String name;
  private int playerCount;

  public Proxy(final String name) {
    this.name = name;
  }

  public String name() {
    return this.name;
  }

  public int playerCount() {
    return this.playerCount;
  }

  public void playerCount(final int playerCount) {
    this.playerCount = playerCount;
  }
}
