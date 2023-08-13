package org.pebbleprojects.knockout.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.pebbleprojects.knockout.handlers.Handler;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;

public class PlayerInteract implements Listener {

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (PlayerDataHandler.INSTANCE.players.contains(player)) {
            final Action action = event.getAction();

            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                final ItemStack[] itemStack = {event.getItem()};

                if (itemStack[0] != null && itemStack[0].getType() != Material.AIR) {
                    if (itemStack[0].getType() == Material.FEATHER) {
                        player.setWalkSpeed(0.4F);

                        player.getInventory().remove(Material.FEATHER);

                        Handler.INSTANCE.runTaskLater(() -> {
                            if (!PlayerDataHandler.INSTANCE.players.contains(player) || player.getInventory().contains(Material.FEATHER)) return;

                            if (player.getWalkSpeed() == 0.4F) {
                                player.setWalkSpeed(0.2F);

                                final Object o = Handler.INSTANCE.getData("players." + player.getUniqueId() + ".savedInventory.speed");
                                if (o instanceof Integer) {
                                    itemStack[0] = player.getInventory().getItem((Integer) o);

                                    if (itemStack[0] == null || itemStack[0].getType() == Material.AIR) {
                                        player.getInventory().setItem((Integer) o, PlayerDataHandler.INSTANCE.speed);
                                        return;
                                    }
                                }

                                player.getInventory().addItem(PlayerDataHandler.INSTANCE.speed);
                            }
                        }, 200);
                    }
                }
            }
        }
    }

}
