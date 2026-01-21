package dev.bauhd.multi.plugin.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import dev.bauhd.multi.plugin.MultiVelocityPlugin;
import dev.bauhd.multi.protocol.object.Proxy;
import dev.bauhd.multi.protocol.packet.RequestProxyNamesPacket;
import dev.bauhd.multi.protocol.packet.RequestProxyPacket;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public final class StatusCommand {

  public static BrigadierCommand get(final MultiVelocityPlugin plugin) {
    return new BrigadierCommand(BrigadierCommand.literalArgumentBuilder("status")
        .requires(source -> source.hasPermission("multi.command.status"))
        .executes(context -> {
          plugin.networkClient().request(new RequestProxyNamesPacket(""))
              .thenAccept(namesPacket -> {
                for (final var proxy : namesPacket.proxies()) {
                  plugin.networkClient().request(new RequestProxyPacket(proxy)).thenAccept(packet ->
                      sendStatus(context.getSource(), packet.proxy()));
                }
              });
          return Command.SINGLE_SUCCESS;
        })
        .then(BrigadierCommand.requiredArgumentBuilder("proxy", StringArgumentType.word())
            .suggests((context, suggestions) ->
                plugin.networkClient().request(
                        new RequestProxyNamesPacket(suggestions.getRemainingLowerCase()))
                    .thenApply(packet -> {
                      for (final var proxy : packet.proxies()) {
                        suggestions.suggest(proxy);
                      }
                      return suggestions.build();
                    }))
            .executes(context -> {
              plugin.networkClient().request(
                      new RequestProxyPacket(context.getArgument("proxy", String.class)))
                  .thenAccept(packet -> {
                    sendStatus(context.getSource(), packet.proxy());
                  });

              return Command.SINGLE_SUCCESS;
            })));
  }

  private static void sendStatus(final CommandSource source, final Proxy proxy) {
    final var color = TextColor.color(0x3AB3FF);
    source.sendMessage(
        Component.textOfChildren(
            Component.text(proxy.name(), color),
            Component.newline(),
            Component.text("Uptime: ", NamedTextColor.GRAY),
            Component.text(formatUptime(
                System.currentTimeMillis() - proxy.startTime()), color),
            Component.newline(),
            Component.text("Players: ", NamedTextColor.GRAY),
            Component.text(proxy.status().playerCount(), color),
            Component.newline(),
            Component.text("Memory: ", NamedTextColor.GRAY),
            Component.text(formatMemory(proxy.status().freeMemory()), color),
            Component.text(" free / ", NamedTextColor.GRAY),
            Component.text(formatMemory(proxy.status().totalMemory()), color),
            Component.text(" used / ", NamedTextColor.GRAY),
            Component.text(formatMemory(proxy.status().maxMemory()), color),
            Component.text(" max", NamedTextColor.GRAY)
        ));
  }

  private static String formatMemory(final long bytes) {
    final var mb = bytes / (1024 * 1024);
    return mb + " MB";
  }

  private static String formatUptime(long millis) {
    var seconds = millis / 1000;
    var minutes = seconds / 60;
    final var hours = minutes / 60;
    seconds %= 60;
    minutes %= 60;
    return hours + "h " + minutes + "m " + seconds + "s";
  }
}
