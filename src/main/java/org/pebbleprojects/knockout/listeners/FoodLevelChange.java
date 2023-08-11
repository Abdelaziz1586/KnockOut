package org.pebbleprojects.knockout.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;

public class FoodLevelChange implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onFoodLevelChange(final FoodLevelChangeEvent event) {
        if (PlayerDataHandler.INSTANCE.players.contains((Player) event.getEntity())) event.setCancelled(true);
    }

}
