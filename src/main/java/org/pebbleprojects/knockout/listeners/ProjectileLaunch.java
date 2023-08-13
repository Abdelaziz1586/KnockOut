package org.pebbleprojects.knockout.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.pebbleprojects.knockout.handlers.Handler;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;

import java.util.ArrayList;
import java.util.List;

public class ProjectileLaunch implements Listener {

    @EventHandler
    public void onProjectileLaunch(final ProjectileLaunchEvent event) {
        final Projectile projectile = event.getEntity();

        final ProjectileSource source = projectile.getShooter();

        if (source instanceof Player) {
            final Player player = (Player) source;

            if (PlayerDataHandler.INSTANCE.players.contains(player)) {

                final List<Projectile> projectiles = PlayerDataHandler.INSTANCE.projectiles.getOrDefault(player.getUniqueId(), new ArrayList<>());

                projectiles.add(projectile);

                PlayerDataHandler.INSTANCE.projectiles.put(player.getUniqueId(), projectiles);

                if (projectile.getType() == EntityType.ARROW) {
                    Handler.INSTANCE.runTaskLater(() -> {
                        if (!PlayerDataHandler.INSTANCE.players.contains(player) || player.getInventory().contains(Material.ARROW))
                            return;

                        final Object o = Handler.INSTANCE.getData("players." + player.getUniqueId() + ".savedInventory.arrow");
                        if (o instanceof Integer) {
                            final ItemStack itemStack = player.getInventory().getItem((Integer) o);

                            if (itemStack == null || itemStack.getType() == Material.AIR) {
                                player.getInventory().setItem((Integer) o, PlayerDataHandler.INSTANCE.arrow);
                                return;
                            }
                        }
                        player.getInventory().addItem(PlayerDataHandler.INSTANCE.arrow);
                    }, 100);
                }
            }
        }
    }

}
