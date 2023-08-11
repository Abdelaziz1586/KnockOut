package org.pebbleprojects.knockout.npc;

import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.pebbleprojects.knockout.handlers.Handler;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;

import java.util.*;

public class NPCHandler {

    private boolean isReloading;
    public static NPCHandler INSTANCE;
    private final HashMap<Integer, NPC> NPCs;

    public NPCHandler() {
        INSTANCE = this;
        
        NPCs = new HashMap<>();
        isReloading = false;
        loadNPCs();
    }

    public void loadNPCs(final Player player) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (final NPC npc : NPCs.values()) {
                    npc.sendRemovePacket(player);
                    npc.sendShowPacket(player);
                }
            }
        }, 500);
    }

    public void loadNPCs() {
        new Thread(() -> {
            if (isReloading)
                return;
            isReloading = true;
            NPC npc;
            NPCs.clear();
            final Set<String> NPCs = Handler.INSTANCE.getDataSection("NPCs");
            if (NPCs == null) return;
            for (final String i : NPCs) {
                try {
                    npc = new NPC(Handler.INSTANCE.getData("NPCs." + i + ".name").toString(), (Location) Handler.INSTANCE.getData("NPCs." + i + ".location"), Integer.parseInt(Handler.INSTANCE.getData("NPCs." + i + ".customData").toString()), this);
                    final Object o = Handler.INSTANCE.getData("NPCs." + i + ".text");
                    if (o != null)
                        npc.getNpc().getProfile().getProperties().put("textures", new Property("textures", o.toString(), Handler.INSTANCE.getData("NPCs." + i + ".signature").toString()));
                    this.NPCs.put(Integer.parseInt(i), npc);
                    PlayerDataHandler.INSTANCE.players.forEach(npc::sendShowPacket);
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
            isReloading = false;
        }).start();
    }

    public void unloadNPCs() {
        for (final Player player : PlayerDataHandler.INSTANCE.players) {
            for (final NPC npc : NPCs.values()) npc.sendRemovePacket(player);
        }

        NPCs.clear();
    }

    public void unloadNPCs(final Player player) {
        Handler.INSTANCE.runTask(() -> NPCs.values().forEach(npc -> npc.sendRemovePacket(player)));
    }

    public void deleteNPC(final int id) {
        final NPC npc = getNPC(id);
        if (npc != null) {
            PlayerDataHandler.INSTANCE.players.forEach(npc::sendRemovePacket);
            NPCs.remove(id);
        }
    }

    public void save(final NPC npc) {
        int i = 1;
        final Set<String> NPCs = Handler.INSTANCE.getDataSection("NPCs");
        if (NPCs != null)
            i = NPCs.size() + 1;
        Handler.INSTANCE.writeData("NPCs." + i + ".customData", npc.getCustomData());
        Handler.INSTANCE.writeData("NPCs." + i + ".location", npc.getLocation());
        Handler.INSTANCE.writeData("NPCs." + i + ".name", npc.getName());

        String s = npc.getTexture();
        if (s != null)
            Handler.INSTANCE.writeData("NPCs." + i + ".text", s);
        s = npc.getSignature();
        if (s != null)
            Handler.INSTANCE.writeData("NPCs." + i + ".signature", s);
        this.NPCs.put(i, npc);
    }

    public final NPC createNPC(final String name, final Location location, final int customData) {
        return new NPC(name, location, customData, this);
    }

    public final int getCustomData(final EntityPlayer entityPlayer) {
        for (final NPC npc : NPCs.values()) {
            if (entityPlayer.equals(npc.getNpc())) return npc.getCustomData();
        }
        return -1;
    }

    public final NPC getNPC(final int id) {
        if (NPCs.containsKey(id))
            return NPCs.get(id);
        return null;
    }

    public final Collection<NPC> getNPCs() {
        return NPCs.values();
    }
}
