package org.pebbleprojects.knockout.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;

public class InventoryClick implements Listener {

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (PlayerDataHandler.INSTANCE.players.contains((Player) event.getWhoClicked()) && event.getInventory().getHolder() == null) event.setCancelled(true);
    }
}
