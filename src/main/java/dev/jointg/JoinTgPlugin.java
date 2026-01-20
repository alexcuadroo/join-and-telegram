package dev.jointg;

import dev.jointg.listener.PluginListener;
import dev.jointg.telegram.TelegramNotifier;
import org.bukkit.plugin.java.JavaPlugin;

public final class JoinTgPlugin extends JavaPlugin {

    private TelegramNotifier notifier;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String botToken = getConfig().getString("bot-token");
        String chatId = getConfig().getString("chat-id");

        if (botToken == null) botToken = "";
        if (chatId == null) chatId = "";

        botToken = botToken.trim();
        chatId = chatId.trim();

        String joinMessage = getConfig().getString(
            "join-message",
            getConfig().getString("message-template", "Player %player% joined")
        );
        String quitMessage = getConfig().getString(
            "quit-message",
            "Player %player% left"
        );
        String deathMessage = getConfig().getString(
            "death-message",
            "Player %player% died because of %reason% at %world% (%x%, %y%, %z%)"
        );
        String startMessage = getConfig().getString(
            "start-message",
            "Server started!"
        );

        if (botToken.isEmpty() || chatId.isEmpty()) {
            getComponentLogger().warn(
                "Telegram bot token or chat ID is missing; Telegram notifications disabled."
            );
            notifier = TelegramNotifier.disabled(getComponentLogger());
        } else {
            notifier = TelegramNotifier.enabled(
                getComponentLogger(),
                botToken,
                chatId,
                joinMessage,
                quitMessage,
                deathMessage,
                startMessage
            );
        }

        String title = getConfig().getString(
            "join-title",
            "<gradient:green:blue>Welcome <player>!"
        );
        String subtitle = getConfig().getString(
            "join-subtitle",
            "<gray>Enjoy your stay"
        );

        int fadeIn = getConfig().getInt("title-fade-in", 10);
        int stay = getConfig().getInt("title-stay", 40);
        int fadeOut = getConfig().getInt("title-fade-out", 20);

        getServer()
            .getPluginManager()
            .registerEvents(
                new PluginListener(
                    this,
                    notifier,
                    title,
                    subtitle,
                    fadeIn,
                    stay,
                    fadeOut
                ),
                this
            );

        notifier.notifyStart(this);

        getComponentLogger().info("JoinTgPlugin enabled");
    }

    @Override
    public void onDisable() {
        if (notifier != null) {
            notifier.close();
        }
        getComponentLogger().info("JoinTgPlugin disabled");
    }
}
