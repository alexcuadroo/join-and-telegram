package dev.jointg.integration;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Provides a player's chat prefix for the join/chat messages.
 *
 * <p>Implementations must not be referenced from classes that are always loaded: only
 * {@link #createIfAvailable(Plugin)} knows about the concrete provider, so that optional
 * integrations (like LuckPerms) never trigger a {@link NoClassDefFoundError} when absent.
 */
public interface PrefixProvider {

    /**
     * Resolves the prefix for the given player. Returns an empty string when the player has no prefix
     * or the integration cannot be loaded.
     */
    String resolve(Player player);

    /**
     * Creates a {@link PrefixProvider} for the first available integration, or {@code null} when none is
     * installed. Referencing this method is safe even when the optional plugin is missing.
     */
    static PrefixProvider createIfAvailable(Plugin plugin) {
        if (plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            try {
                return new LuckPermsPrefixProvider(plugin);
            } catch (Throwable t) {
                plugin.getComponentLogger().warn(
                        "LuckPerms is installed but its API could not be loaded; prefixes will not be shown.", t);
            }
        }
        return null;
    }
}
