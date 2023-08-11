package org.pebbleprojects.knockout.npc.customEvent;


import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.pebbleprojects.knockout.npc.NPC;

public class RightClickNPCEvent extends Event implements Cancellable {

    private final NPC npc;
    private final Player player;
    private boolean isCancelled;
    private static final HandlerList handlers = new HandlerList();

    public RightClickNPCEvent(final Player player, final NPC npc) {
        this.npc = npc;
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public NPC getNPC() {
        return npc;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        isCancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
