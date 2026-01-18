package dev.jointg.listener;

import dev.jointg.telegram.TelegramNotifier;
import java.time.Duration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public final class PluginListener implements Listener {
  private final Plugin plugin;
  private final TelegramNotifier notifier;
  private final String titleTemplate;
  private final String subtitleTemplate;
  private final Title.Times times;

  public PluginListener(Plugin plugin, TelegramNotifier notifier, String titleTemplate, String subtitleTemplate,
      int fadeInTicks, int stayTicks, int fadeOutTicks) {
    this.plugin = plugin;
    this.notifier = notifier;
    this.titleTemplate = titleTemplate;
    this.subtitleTemplate = subtitleTemplate;
    this.times = Title.Times.times(
        Duration.ofMillis(fadeInTicks * 50L),
        Duration.ofMillis(stayTicks * 50L),
        Duration.ofMillis(fadeOutTicks * 50L));
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    event.joinMessage(null);

    MiniMessage mm = MiniMessage.miniMessage();
    Component title = mm.deserialize(titleTemplate, Placeholder.unparsed("player", event.getPlayer().getName()));
    Component subtitle = mm.deserialize(subtitleTemplate, Placeholder.unparsed("player", event.getPlayer().getName()));

    event.getPlayer().showTitle(Title.title(title, subtitle, times));

    notifier.notifyJoin(plugin, event.getPlayer().getName());
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
      reason = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
          .serialize(deathMessageComponent);
    }

    var loc = event.getEntity().getLocation();
    String world = "unknown";
    if (loc.getWorld() != null) {
      world = loc.getWorld().getName();
    }
    int x = loc.getBlockX();
    int y = loc.getBlockY();
    int z = loc.getBlockZ();

    notifier.notifyDeath(plugin, playerName, reason, world, x, y, z);
  }
}
