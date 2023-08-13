package org.pebbleprojects.knockout.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.pebbleprojects.knockout.handlers.Handler;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerMove implements Listener {

    public final List<UUID> cooldown;

    public PlayerMove() {
        cooldown = new ArrayList<>();
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();

        if (PlayerDataHandler.INSTANCE.players.contains(player)) {
            if (player.getWorld().getBlockAt(player.getLocation()).getType() == Material.GOLD_PLATE) {
                if (!cooldown.contains(player.getUniqueId())) {
                    player.setVelocity(player.getLocation().getDirection().multiply(3).setY(2));

                    cooldown.add(player.getUniqueId());

                    Handler.INSTANCE.runTaskLater(() -> cooldown.remove(player.getUniqueId()), 3);
                }
            }
        }
    }

}
