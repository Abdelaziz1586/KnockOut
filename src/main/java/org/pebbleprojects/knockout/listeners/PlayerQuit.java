package org.pebbleprojects.knockout.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;

public class PlayerQuit implements Listener {

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent e) {
        final Player player = e.getPlayer();
        if (PlayerDataHandler.INSTANCE.players.contains(player)) PlayerDataHandler.INSTANCE.leave(player);
    }
}
