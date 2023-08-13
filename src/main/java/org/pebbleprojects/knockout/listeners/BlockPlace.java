package org.pebbleprojects.knockout.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.pebbleprojects.knockout.handlers.Handler;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;

public class BlockPlace implements Listener {

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Player player = event.getPlayer();

        if (PlayerDataHandler.INSTANCE.players.contains(player)) {
            
            final Block block = event.getBlock();
            
            if (block.getType() != Material.SANDSTONE && PlayerDataHandler.INSTANCE.tempBlocks.contains(block.getWorld().getBlockAt(block.getLocation().subtract(0, 1, 0)))) {
                event.setCancelled(true);
                return;
            }

            final boolean b = block.getType() == Material.GOLD_PLATE;

            PlayerDataHandler.INSTANCE.placeTemp(block, b);

            if (b) {
                Handler.INSTANCE.runTaskLater(() -> {
                    if (!PlayerDataHandler.INSTANCE.players.contains(player) || player.getInventory().contains(Material.GOLD_PLATE)) return;

                    final Object o = Handler.INSTANCE.getData("players." + player.getUniqueId() + ".savedInventory.launchpad");
                    if (o instanceof Integer) {
                        final ItemStack itemStack = player.getInventory().getItem((Integer) o);

                        if (itemStack == null || itemStack.getType() == Material.AIR) {
                            player.getInventory().setItem((Integer) o, PlayerDataHandler.INSTANCE.launchpad);
                            return;
                        }
                    }

                    player.getInventory().addItem(PlayerDataHandler.INSTANCE.launchpad);
                }, 100);
                return;
            }

            player.getInventory().setItemInHand(player.getEquipment().getItemInHand());
        }
    }

}
