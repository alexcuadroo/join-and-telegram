package dev.jointg.integration;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * {@link PrefixProvider} backed by LuckPerms.
 *
 * <p>This class is only loaded when LuckPerms is present, so its direct references to the LuckPerms API
 * are safe.
 */
final class LuckPermsPrefixProvider implements PrefixProvider {

    private final Plugin plugin;

    LuckPermsPrefixProvider(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String resolve(Player player) {
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                return "";
            }
            String rawPrefix = user.getCachedData().getMetaData(QueryOptions.defaultContextualOptions()).getPrefix();
            if (rawPrefix == null || rawPrefix.isEmpty()) {
                return "";
            }
            // Convertir &-codes legacy a un componente y luego a MiniMessage
            Component prefixComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(rawPrefix);
            return MiniMessage.miniMessage().serialize(prefixComponent);
        } catch (Exception e) {
            plugin.getComponentLogger().debug(
                    "Could not resolve LuckPerms prefix for player " + player.getName() + ".", e);
            return "";
        }
    }
}
