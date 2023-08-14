package org.pebbleprojects.knockout.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.pebbleprojects.knockout.handlers.Handler;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntityDamage implements Listener {

    private final List<UUID> cooldown;

    public EntityDamage() {
        cooldown = new ArrayList<>();
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent event) {
        final Entity entity = event.getEntity();

        if (entity instanceof Player) {
            final Player player = (Player) entity;

            if (PlayerDataHandler.INSTANCE.players.contains(player)) {

                event.setDamage(0);

                if (event instanceof EntityDamageByEntityEvent) {
                    Entity damager = ((EntityDamageByEntityEvent) event).getDamager();

                    if (damager instanceof Projectile) damager = (Entity) ((Projectile) damager).getShooter();

                    if (damager instanceof Player) {
                        if (!PlayerDataHandler.INSTANCE.players.contains((Player) damager)) {
                            event.setCancelled(true);
                            return;
                        }

                        if (damager != player) PlayerDataHandler.INSTANCE.lastDamage.put(player.getUniqueId(), (Player) damager);
                    }
                    return;
                }

                if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID) && !cooldown.contains(player.getUniqueId())) {
                    event.setCancelled(true);

                    PlayerDataHandler.INSTANCE.death(player, PlayerDataHandler.INSTANCE.lastDamage.getOrDefault(player.getUniqueId(), null));

                    PlayerDataHandler.INSTANCE.lastDamage.remove(player.getUniqueId());

                    Handler.INSTANCE.runTaskLater(() -> player.setFallDistance(0), 1);

                    cooldown.add(player.getUniqueId());

                    final int i = cooldown.size() - 1;

                    Handler.INSTANCE.runTaskLater(() -> cooldown.remove(i), 1);
                    return;
                }

                if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) event.setCancelled(true);
            }
        }
    }

}
