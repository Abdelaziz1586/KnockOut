package org.pebbleprojects.knockout.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;

public class PlayerChangedWorld implements Listener {

    @EventHandler
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
        new Thread(() -> {
            if (PlayerDataHandler.INSTANCE.players.contains(event.getPlayer()) && event.getPlayer().getWorld() != PlayerDataHandler.INSTANCE.spawn.getWorld())
                PlayerDataHandler.INSTANCE.leave(event.getPlayer());
        }).start();
    }

}
