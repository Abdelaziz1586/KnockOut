package org.pebbleprojects.knockout.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class NPC {

    private String texture;
    private String signature;
    private final String name;
    private final int customData;
    private final EntityPlayer npc;
    private final Location location;
    private final NPCHandler handler;
    private final GameProfile gameProfile;

    public NPC(final String name, final Location location, final int customData, final NPCHandler handler) {
        texture = null;
        signature = null;
        this.name = name;
        this.handler = handler;
        this.location = location;
        this.customData = customData;
        final WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        gameProfile = new GameProfile(UUID.randomUUID(), ChatColor.translateAlternateColorCodes('&', name));
        npc = new EntityPlayer(((CraftServer) Bukkit.getServer()).getServer(), nmsWorld, gameProfile, new PlayerInteractManager(nmsWorld));
        npc.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public void setSkin(final String skinName) {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.ashcon.app/mojang/v2/user/" + skinName).openConnection();
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                final ArrayList<String> lines = new ArrayList<>();
                final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                reader.lines().forEach(lines::add);

                final String reply = String.join(" ",lines);
                int indexOfValue = reply.indexOf("\"value\": \"");
                int indexOfSignature = reply.indexOf("\"signature\": \"");
                texture = reply.substring(indexOfValue + 10, reply.indexOf("\"", indexOfValue + 10));
                signature = reply.substring(indexOfSignature + 14, reply.indexOf("\"", indexOfSignature + 14));

                gameProfile.getProperties().put("textures", new Property("textures", texture, signature));
            }

            else {
                Bukkit.getConsoleSender().sendMessage("Connection could not be opened when fetching player skin (Response code " + connection.getResponseCode() + ", " + connection.getResponseMessage() + ")");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendShowPacket(final Player player) {
        if (((CraftPlayer)player).getHandle().getWorld() != npc.getWorld()) return;

        final PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
        final DataWatcher watcher = npc.getDataWatcher();
        watcher.watch(10, (byte) 127);
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(npc.getId(), watcher, true);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);

        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc));
        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
        connection.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) (npc.yaw * 256 / 360)));
        new Thread(() -> new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npc));
            }
        }, 5)).start();
    }

    public void sendRemovePacket(final Player player) {
        final PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
        connection.sendPacket(new PacketPlayOutEntityDestroy(npc.getId()));
    }

    public void save() {
        handler.save(this);
    }

    public final String getName() {
        return name;
    }

    public final String getTexture() {
        return texture;
    }

    public final int getCustomData() {
        return customData;
    }

    public final EntityPlayer getNpc() {
        return npc;
    }

    public final String getSignature() {
        return signature;
    }

    public final Location getLocation() {
        return location;
    }

}
