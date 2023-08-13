package org.pebbleprojects.knockout.handlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.pebbleprojects.knockout.Command;
import org.pebbleprojects.knockout.KnockOut;
import org.pebbleprojects.knockout.listeners.*;
import org.pebbleprojects.knockout.npc.NPCHandler;
import org.pebbleprojects.knockout.npc.customEvent.PacketReader;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Handler {

    private final File dataFile;
    public static Handler INSTANCE;
    private FileConfiguration config, data;

    public Handler() {
        INSTANCE = this;

        KnockOut.INSTANCE.getConfig().options().copyDefaults(true);
        KnockOut.INSTANCE.saveDefaultConfig();
        updateConfig();

        dataFile = new File(KnockOut.INSTANCE.getDataFolder().getPath(), "data.yml");

        if (!dataFile.exists()) {
            try {
                if (dataFile.createNewFile())
                    KnockOut.INSTANCE.getServer().getConsoleSender().sendMessage("§aCreated data.yml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updateData();

        new PlayerDataHandler();

        new NPCHandler();

        new PacketReader();

        KnockOut.INSTANCE.getCommand("KnockOut").setExecutor(new Command());

        final PluginManager pm = KnockOut.INSTANCE.getServer().getPluginManager();

        pm.registerEvents(new BlockBreak(), KnockOut.INSTANCE);
        pm.registerEvents(new BlockPlace(), KnockOut.INSTANCE);
        pm.registerEvents(new PlayerQuit(), KnockOut.INSTANCE);
        pm.registerEvents(new PlayerMove(), KnockOut.INSTANCE);
        pm.registerEvents(new PlayerDeath(), KnockOut.INSTANCE);
        pm.registerEvents(new EntityDamage(), KnockOut.INSTANCE);
        pm.registerEvents(new RightClickNPC(), KnockOut.INSTANCE);
        pm.registerEvents(new ProjectileHit(), KnockOut.INSTANCE);
        pm.registerEvents(new PlayerInteract(), KnockOut.INSTANCE);
        pm.registerEvents(new InventoryClick(), KnockOut.INSTANCE);
        pm.registerEvents(new PlayerDropItem(), KnockOut.INSTANCE);
        pm.registerEvents(new FoodLevelChange(), KnockOut.INSTANCE);
        pm.registerEvents(new AsyncPlayerChat(), KnockOut.INSTANCE);
        pm.registerEvents(new ProjectileLaunch(), KnockOut.INSTANCE);
        pm.registerEvents(new PlayerChangedWorld(), KnockOut.INSTANCE);
    }

    public void shutdown() {
        PlayerDataHandler.INSTANCE.shutdown();
        NPCHandler.INSTANCE.unloadNPCs();
    }

    public void updateConfig() {
        KnockOut.INSTANCE.reloadConfig();
        config = KnockOut.INSTANCE.getConfig();
    }

    public void updateData() {
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void writeData(final String key, final Object value) {
        data.set(key, value);
        try {
            data.save(dataFile);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    public void runTask(final Runnable runnable) {
        if (!Bukkit.isPrimaryThread()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            }.runTask(KnockOut.INSTANCE);
            return;
        }
        runnable.run();
    }

    public final BukkitTask runTaskLater(final Runnable runnable, final int delay) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskLater(KnockOut.INSTANCE, delay);
    }
    public Object getConfig(final String key, final boolean translate) {
        return config.isSet(key) ? (translate ? ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString(key))).replace("%nl%", "\n") : config.get(key)) : null;
    }

    public List<String> getConfigList(final String key, final boolean translate, final Player player) {
        if (!config.isSet(key)) return null;

        final List<String> list = config.getStringList(key);

        if (translate) {
            String s;
            for (int i = 0; i < list.size(); i++) {
                s = player != null ? PlayerDataHandler.INSTANCE.replaceStringWithData(player.getUniqueId(), list.get(i)).replace("%player%", player.getName()) : list.get(i);
                list.set(i, ChatColor.translateAlternateColorCodes('&', s));
            }
        }
        return list;
    }

    public List<Location> getSpawns() {
        Object o;
        final List<Location> spawns = new ArrayList<>();

        final Set<String> keys = getDataSection("spawns");

        if (keys == null) return spawns;

        for (final String key : keys) {
            o = getData("spawns." + key);

            if (o instanceof Location) spawns.add((Location) o);
        }

        return spawns;
    }

    public Object getData(final String key) {
        return data.isSet(key) ? data.get(key) : null;
    }

    public Set<String> getDataSection(final String key) {
        return data.isSet(key) ? data.getConfigurationSection(key).getKeys(false) : null;
    }
}
