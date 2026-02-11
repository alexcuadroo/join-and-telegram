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
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.entity.Player;

public final class PluginListener implements Listener {
  private final Plugin plugin;
  private final TelegramNotifier notifier;
  private final String titleTemplate;
  private final String subtitleTemplate;
  private final Title.Times times;
  private final String chatJoinMessage;

  public PluginListener(Plugin plugin, TelegramNotifier notifier, String titleTemplate, String subtitleTemplate,
      int fadeInTicks, int stayTicks, int fadeOutTicks, String chatJoinMessage) {
    this.plugin = plugin;
    this.notifier = notifier;
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

    MiniMessage mm = MiniMessage.miniMessage();
    Player player = event.getPlayer();
    String playerName = player.getName();

    // Obtener prefix LuckPerms y convertir colores legacy a MiniMessage
    String prefix = "";
    try {
      LuckPerms luckPerms = LuckPermsProvider.get();
      User user = luckPerms.getUserManager().getUser(player.getUniqueId());
      if (user != null) {
        String rawPrefix = user.getCachedData().getMetaData(QueryOptions.defaultContextualOptions()).getPrefix();
        if (rawPrefix != null) {
          // Convertir &-codes a componente y luego a MiniMessage
          Component prefixComponent = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
              .legacyAmpersand().deserialize(rawPrefix);
          prefix = MiniMessage.miniMessage().serialize(prefixComponent);
        }
      }
    } catch (Exception e) {
      prefix = "";
    }

    Component title = mm.deserialize(titleTemplate, Placeholder.unparsed("player", playerName));
    Component subtitle = mm.deserialize(subtitleTemplate, Placeholder.unparsed("player", playerName));

    player.showTitle(Title.title(title, subtitle, times));

    // Mensaje de bienvenida en el chat global si está configurado
    if (chatJoinMessage != null && !chatJoinMessage.isBlank()) {
      String msg = chatJoinMessage.replace("<player>", playerName).replace("<prefix>", prefix);
      Component joinComponent = mm.deserialize(msg);
      player.getServer().sendMessage(joinComponent);
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
