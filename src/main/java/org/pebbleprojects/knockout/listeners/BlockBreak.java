package org.pebbleprojects.knockout.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;

public class BlockBreak implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        if (PlayerDataHandler.INSTANCE.players.contains(event.getPlayer())) event.setCancelled(true);
    }

}
