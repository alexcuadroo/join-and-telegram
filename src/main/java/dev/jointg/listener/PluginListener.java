package dev.jointg.listener;

import dev.jointg.integration.PrefixProvider;
import dev.jointg.telegram.TelegramNotifier;
import java.time.Duration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public final class PluginListener implements Listener {
  private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

  private final Plugin plugin;
  private final TelegramNotifier notifier;
  /** Null when no prefix integration (e.g. LuckPerms) is installed. */
  private final PrefixProvider prefixProvider;
  private final String titleTemplate;
  private final String subtitleTemplate;
  private final Title.Times times;
  private final String chatJoinMessage;

  public PluginListener(Plugin plugin, TelegramNotifier notifier, PrefixProvider prefixProvider,
      String titleTemplate, String subtitleTemplate, int fadeInTicks, int stayTicks, int fadeOutTicks,
      String chatJoinMessage) {
    this.plugin = plugin;
    this.notifier = notifier;
    this.prefixProvider = prefixProvider;
    this.titleTemplate = titleTemplate;
    this.subtitleTemplate = subtitleTemplate;
    this.times = Title.Times.times(
        Duration.ofMillis(fadeInTicks * 50L),
        Duration.ofMillis(stayTicks * 50L),
        Duration.ofMillis(fadeOutTicks * 50L));
    this.chatJoinMessage = chatJoinMessage;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    // Oculta el mensaje de join por defecto
    event.joinMessage(null);

    Player player = event.getPlayer();
    String playerName = player.getName();

    String prefix = prefixProvider != null ? prefixProvider.resolve(player) : "";

    Component title = MINI_MESSAGE.deserialize(titleTemplate, Placeholder.unparsed("player", playerName));
    Component subtitle = MINI_MESSAGE.deserialize(subtitleTemplate, Placeholder.unparsed("player", playerName));

    player.showTitle(Title.title(title, subtitle, times));

    // Mensaje de bienvenida en el chat global si está configurado
    if (chatJoinMessage != null && !chatJoinMessage.isBlank()) {
      String msg = chatJoinMessage.replace("<player>", playerName).replace("<prefix>", prefix);
      player.getServer().sendMessage(MINI_MESSAGE.deserialize(msg));
    }

    notifier.notifyJoin(plugin, playerName);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    notifier.notifyQuit(plugin, event.getPlayer().getName());
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    String playerName = event.getEntity().getName();

    Component deathMessageComponent = event.deathMessage();
    String reason = "died";
    if (deathMessageComponent != null) {
      reason = PlainTextComponentSerializer.plainText().serialize(deathMessageComponent);
    }

    var loc = event.getEntity().getLocation();
    String world = loc.getWorld() != null ? loc.getWorld().getName() : "unknown";

    notifier.notifyDeath(plugin, playerName, reason, world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
  }
}
