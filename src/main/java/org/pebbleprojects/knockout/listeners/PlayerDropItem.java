package org.pebbleprojects.knockout.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;

public class PlayerDropItem implements Listener {

    @EventHandler
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        if (PlayerDataHandler.INSTANCE.players.contains(event.getPlayer())) event.setCancelled(true);
    }
}
