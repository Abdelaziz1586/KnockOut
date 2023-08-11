package org.pebbleprojects.knockout.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;

public class PlayerDeath implements Listener {

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {
        if (PlayerDataHandler.INSTANCE.players.contains(event.getEntity())) event.setDeathMessage("");
    }

}
