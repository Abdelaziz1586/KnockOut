package org.pebbleprojects.knockout.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.pebbleprojects.knockout.handlers.Handler;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class EntityDamage implements Listener {

    private final HashMap<UUID, Player> lastDamage;
    private final List<UUID> cooldown = new ArrayList<>();

    public EntityDamage() {
        lastDamage = new HashMap<>();
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent event) {
        final Entity entity = event.getEntity();

        if (entity instanceof Player) {
            final Player player = (Player) entity;

            if (event instanceof EntityDamageByEntityEvent) {
                final Entity damager = ((EntityDamageByEntityEvent) event).getDamager();

                if (damager instanceof Player) {
                    if (!PlayerDataHandler.INSTANCE.players.contains((Player) damager)) {
                        event.setCancelled(true);
                        return;
                    }

                    lastDamage.put(entity.getUniqueId(), (Player) damager);
                    event.setDamage(0);
                }
                return;
            }

            if (PlayerDataHandler.INSTANCE.players.contains(player)) {
                if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID) && !cooldown.contains(player.getUniqueId())) {
                    event.setCancelled(true);

                    PlayerDataHandler.INSTANCE.death(player, lastDamage.getOrDefault(player.getUniqueId(), null));

                    lastDamage.remove(player.getUniqueId());

                    player.setFallDistance(0);

                    cooldown.add(player.getUniqueId());

                    final int i = cooldown.size()-1;

                    Handler.INSTANCE.runTaskLater(() -> cooldown.remove(i), 1);
                    return;
                }

                if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) event.setCancelled(true);
            }
        }
    }

}
