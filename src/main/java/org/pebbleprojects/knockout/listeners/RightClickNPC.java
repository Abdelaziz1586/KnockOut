package org.pebbleprojects.knockout.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;
import org.pebbleprojects.knockout.npc.NPCHandler;
import org.pebbleprojects.knockout.npc.customEvent.RightClickNPCEvent;

public class RightClickNPC implements Listener {

    @EventHandler   
    public void onRightClickNPC(final RightClickNPCEvent event) {
        final Player player = event.getPlayer();
        if (PlayerDataHandler.INSTANCE.players.contains(player) && NPCHandler.INSTANCE.getCustomData(event.getNPC().getNpc()) == 0)
            player.performCommand("knockout saveInventory");
    }
}
