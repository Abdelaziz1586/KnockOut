package org.pebbleprojects.knockout.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;
import org.pebbleprojects.knockout.npc.NPCHandler;

public class PlayerChangedWorld implements Listener {

    @EventHandler
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
        new Thread(() -> {
            if (PlayerDataHandler.INSTANCE.players.contains(event.getPlayer())) {
                if (event.getPlayer().getWorld() == PlayerDataHandler.INSTANCE.spawn.getWorld()) {
                    NPCHandler.INSTANCE.unloadNPCs(event.getPlayer());
                    NPCHandler.INSTANCE.loadNPCs(event.getPlayer());
                    return;
                }

                PlayerDataHandler.INSTANCE.leave(event.getPlayer());
            }
        }).start();
    }

}
