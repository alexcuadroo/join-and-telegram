package dev.jointg;

import dev.jointg.integration.PrefixProvider;
import dev.jointg.listener.PluginListener;
import dev.jointg.telegram.TelegramNotifier;
import dev.jointg.update.GitHubUpdateChecker;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class JoinTgPlugin extends JavaPlugin {

    private static final String GITHUB_OWNER = "alexcuadroo";
    private static final String GITHUB_REPOSITORY = "join-and-telegram";
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private TelegramNotifier notifier;
    private PluginListener listenerInstance;
    private PrefixProvider prefixProvider;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        registerReloadCommand();

        // Integración opcional: solo se carga si LuckPerms está presente.
        prefixProvider = PrefixProvider.createIfAvailable(this);
        if (prefixProvider == null) {
            getComponentLogger().info("LuckPerms not found or not enabled; join prefixes will be empty.");
        }

        GitHubUpdateChecker.checkForUpdates(this, GITHUB_OWNER, GITHUB_REPOSITORY);

        // On the initial enable only, announce the server start to Telegram.
        reloadPluginConfig(true);

        getComponentLogger().info("JoinTgPlugin enabled");
    }

    private void registerReloadCommand() {
        PluginCommand command = getCommand("jointg");
        if (command == null) {
            getComponentLogger().warn("Command 'jointg' is not defined in plugin.yml; /jointg reload will be unavailable.");
            return;
        }

        command.setExecutor((sender, cmd, label, args) -> {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                reloadPluginConfig(false);
                sender.sendMessage(MINI_MESSAGE.deserialize("<green>[JoinTgPlugin] Configuración recargada correctamente."));
                return true;
            }
            sender.sendMessage(MINI_MESSAGE.deserialize("<yellow>Uso: /jointg reload"));
            return true;
        });

        command.setTabCompleter((sender, cmd, alias, args) -> {
            if (args.length == 1 && "reload".startsWith(args[0].toLowerCase())) {
                return List.of("reload");
            }
            return Collections.emptyList();
        });
    }

    private void reloadPluginConfig(boolean announceStart) {
        if (listenerInstance != null) {
            HandlerList.unregisterAll(listenerInstance);
            listenerInstance = null;
        }
        if (notifier != null) {
            notifier.close();
            notifier = null;
        }

        String botToken = getConfig().getString("bot-token", "").trim();
        String chatId = getConfig().getString("chat-id", "").trim();

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
                prefixProvider,
                title,
                subtitle,
                fadeIn,
                stay,
                fadeOut,
                chatJoinMessage);

        getServer()
                .getPluginManager()
                .registerEvents(listenerInstance, this);

        if (announceStart) {
            notifier.notifyStart(this);
        }
    }

    @Override
    public void onDisable() {
        if (listenerInstance != null) {
            HandlerList.unregisterAll(listenerInstance);
            listenerInstance = null;
        }
        if (notifier != null) {
            notifier.close();
            notifier = null;
        }
        getComponentLogger().info("JoinTgPlugin disabled");
    }
}
