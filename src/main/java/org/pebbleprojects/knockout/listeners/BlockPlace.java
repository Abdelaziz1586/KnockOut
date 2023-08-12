package org.pebbleprojects.knockout.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.pebbleprojects.knockout.handlers.Handler;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;

public class BlockPlace implements Listener {


    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        Handler.INSTANCE.runTask(() -> {
            if (PlayerDataHandler.INSTANCE.players.contains(event.getPlayer())) {
                PlayerDataHandler.INSTANCE.placeTemp(event.getBlock());

                event.getPlayer().getInventory().setItemInHand(event.getPlayer().getEquipment().getItemInHand());
            }
        });
    }

}
