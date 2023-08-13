package org.pebbleprojects.knockout.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;

public class ProjectileHit implements Listener {

    @EventHandler
    public void onProjectileHit(final ProjectileHitEvent event) {
        final ProjectileSource source = event.getEntity().getShooter();

        if (source instanceof Player && PlayerDataHandler.INSTANCE.players.contains((Player) source)) {
            event.getEntity().remove();
        }
    }

}
