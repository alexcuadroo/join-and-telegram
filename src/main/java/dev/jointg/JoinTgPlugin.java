package dev.jointg;

import dev.jointg.listener.PluginListener;
import dev.jointg.telegram.TelegramNotifier;
import dev.jointg.update.GitHubUpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class JoinTgPlugin extends JavaPlugin {

    private static final String GITHUB_OWNER = "alexcuadroo";
    private static final String GITHUB_REPOSITORY = "join-and-telegram";

    private TelegramNotifier notifier;
    private PluginListener listenerInstance;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadPluginConfig();

        // Registrar comando /jointg reload
        getCommand("jointg").setExecutor((sender, command, label, args) -> {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                reloadPluginConfig();
                sender.sendMessage("§a[JoinTgPlugin] Configuración recargada correctamente.");
                return true;
            }
            sender.sendMessage("§eUso: /jointg reload");
            return true;
        });

        getCommand("jointg").setTabCompleter((sender, command, alias, args) -> {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                if ("reload".startsWith(args[0].toLowerCase())) {
                    completions.add("reload");
                }
                return completions;
            }
            return Collections.emptyList();
        });

        if (getServer().getPluginManager().getPlugin("LuckPerms") == null) {
            getComponentLogger().warn("LuckPerms not found! Install LuckPerms to display prefixes in join messages.");
        }

        GitHubUpdateChecker.checkForUpdates(this, GITHUB_OWNER, GITHUB_REPOSITORY);

        getComponentLogger().info("JoinTgPlugin enabled");
    }

    private void reloadPluginConfig() {
        if (listenerInstance != null) {
            HandlerList.unregisterAll(listenerInstance);
        }
        if (notifier != null) {
            notifier.close();
            notifier = null;
        }
        String botToken = getConfig().getString("bot-token");
        String chatId = getConfig().getString("chat-id");

        if (botToken == null)
            botToken = "";
        if (chatId == null)
            chatId = "";

        botToken = botToken.trim();
        chatId = chatId.trim();

        boolean telegramEnabled = getConfig().getBoolean("telegram-enabled", false);

        String joinMessage = getConfig().getString(
                "join-message",
                getConfig().getString("message-template", "Player %player% joined"));
        String quitMessage = getConfig().getString(
                "quit-message",
                "Player %player% left");
        String deathMessage = getConfig().getString(
                "death-message",
                "Player %player% died because of %reason% at %world% (%x%, %y%, %z%)");
        String startMessage = getConfig().getString(
                "start-message",
                "Server started!");

        if (!telegramEnabled) {
            getComponentLogger().info("Telegram notifications disabled in config (telegram-enabled=false).");
            notifier = TelegramNotifier.disabled(getComponentLogger());
        } else if (botToken.isEmpty() || chatId.isEmpty()) {
            getComponentLogger().warn(
                    "Telegram bot token or chat ID is missing; Telegram notifications disabled.");
            notifier = TelegramNotifier.disabled(getComponentLogger());
        } else {
            notifier = TelegramNotifier.enabled(
                    getComponentLogger(),
                    botToken,
                    chatId,
                    joinMessage,
                    quitMessage,
                    deathMessage,
                    startMessage);
        }

        String title = getConfig().getString(
                "join-title",
                "<gradient:green:blue>Welcome <player>!");
        String subtitle = getConfig().getString(
                "join-subtitle",
                "<gray>Enjoy your stay");

        int fadeIn = getConfig().getInt("title-fade-in", 10);
        int stay = getConfig().getInt("title-stay", 40);
        int fadeOut = getConfig().getInt("title-fade-out", 20);

        String chatJoinMessage = getConfig().getString("chat-join-message", "");

        listenerInstance = new PluginListener(
                this,
                notifier,
                title,
                subtitle,
                fadeIn,
                stay,
                fadeOut,
                chatJoinMessage);

        getServer()
                .getPluginManager()
                .registerEvents(listenerInstance, this);

        if (notifier != null) {
            notifier.notifyStart(this);
        }
    }

    @Override
    public void onDisable() {
        if (notifier != null) {
            notifier.close();
        }
        getComponentLogger().info("JoinTgPlugin disabled");
    }
}
