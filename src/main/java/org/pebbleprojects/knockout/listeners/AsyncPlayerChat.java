package org.pebbleprojects.knockout.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.pebbleprojects.knockout.handlers.Handler;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;

public class AsyncPlayerChat implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        if (PlayerDataHandler.INSTANCE.players.contains(player)) {
            event.setCancelled(true);
            new Thread(() -> {
                if (Boolean.parseBoolean(Handler.INSTANCE.getConfig("chat-format.enabled", false).toString())) {
                    PlayerDataHandler.INSTANCE.broadcast(PlayerDataHandler.INSTANCE.replaceStringWithData(player.getUniqueId(), Handler.INSTANCE.getConfig("chat-format.format", true).toString().replace("%player%", player.getDisplayName()).replace("%message%", event.getMessage())));
                }
            }).start();
            return;
        }
        new Thread(() -> PlayerDataHandler.INSTANCE.players.forEach(event.getRecipients()::remove)).start();
    }

}
