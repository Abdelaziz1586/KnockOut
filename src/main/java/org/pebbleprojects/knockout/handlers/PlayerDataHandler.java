package org.pebbleprojects.knockout.handlers;

import fr.mrmicky.fastboard.FastBoard;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftSound;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.pebbleprojects.knockout.KnockOut;
import org.pebbleprojects.knockout.npc.NPCHandler;
import org.pebbleprojects.knockout.npc.customEvent.PacketReader;
import org.pebbleprojects.knockout.utils.SavedInventory;
import se.file14.procosmetics.ProCosmetics;
import se.file14.procosmetics.api.ProCosmeticsProvider;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class PlayerDataHandler {

    public Location spawn;
    private final ProCosmetics api;
    public final ArrayList<Player> players;
    public static PlayerDataHandler INSTANCE;
    private final ArrayList<Block> tempBlocks;
    public final HashMap<UUID, Player> lastDamage;
    private final HashMap<UUID, Integer> killStreaks;
    private final HashMap<UUID, FastBoard> scoreboards;
    private final HashMap<UUID, SavedInventory> savedInventories;
    private final ItemStack knockBackStick, knockBackBow, blocks, pearl;

    public PlayerDataHandler() {
        INSTANCE = this;

        players = new ArrayList<>();
        lastDamage = new HashMap<>();
        scoreboards = new HashMap<>();
        killStreaks = new HashMap<>();
        tempBlocks = new ArrayList<>();
        api = Bukkit.getPluginManager().getPlugin("ProCosmetics") != null ? ProCosmeticsProvider.get() : null;

        KnockOut.INSTANCE.getServer().getConsoleSender().sendMessage(api == null ? "§cCouldn't find ProCosmetics, running without it" : "§aHooked into ProCosmetics");

        savedInventories = new HashMap<>();

        pearl = createItemStack(Material.ENDER_PEARL, "§5EnderPearl", null, 1, false);
        blocks = createItemStack(Material.SANDSTONE, "§5Blocks", null, 64, false);
        knockBackBow = createItemStack(Material.BOW, "§5Bow", null, 1, false);
        knockBackStick = createItemStack(Material.STICK, "§5Stick", null, 1, false);

        ItemMeta itemMeta = knockBackStick.getItemMeta();

        itemMeta.addEnchant(Enchantment.KNOCKBACK, 2, true);

        knockBackStick.setItemMeta(itemMeta);

        itemMeta = knockBackBow.getItemMeta();

        itemMeta.addEnchant(Enchantment.ARROW_KNOCKBACK, 1, true);

        knockBackBow.setItemMeta(itemMeta);

        updateMap();


    }

    public void shutdown() {
        final ArrayList<Player> playersCache = new ArrayList<>(players);
        final ArrayList<org.bukkit.block.Block> tempBlocksCache = new ArrayList<>(tempBlocks);
        final HashMap<UUID, SavedInventory> savedInventoriesCache = new HashMap<>(savedInventories);

        for (final Player player : playersCache) {
            quickLeave(player);
            savedInventoriesCache.get(player.getUniqueId()).restoreInventory();
        }
        for (final Block block : tempBlocksCache)
            block.setType(Material.AIR);
    }

    public void updateMap() {
        final int[] i = {1};

        new BukkitRunnable() {
            @Override
            public void run() {
                final List<Location> spawns = Handler.INSTANCE.getSpawns();

                if (spawns.isEmpty()) {
                    spawn = null;
                    i[0] = 1;
                    return;
                }

                spawn = spawns.get(new Random().nextInt(spawns.size()-1));
                i[0] = 24000;
            }
        }.runTaskTimer(KnockOut.INSTANCE, 0, i[0]);
    }

    public void broadcast(final String message) {
        for (final Player player : players) player.sendMessage(message);
    }

    public void sendTitle(final Player player, final String title, final String subTitle) {
        PacketPlayOutTitle packet;
        final PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        if (title != null) {
            packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, new ChatComponentText(title), 1, 5, 1);
            playerConnection.sendPacket(packet);
        }
        if (subTitle != null) {
            packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, new ChatComponentText(subTitle), 1, 5, 1);
            playerConnection.sendPacket(packet);
        }
    }

    public void sendActionbar(final Player player, final String message) {
        if (player == null || message == null) return;

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(new ChatComponentText(message), (byte) 2));
    }

    public void join(final Player player) {
        if (spawn == null) {
            if (player.isOp()) {
                player.sendMessage("§cYou haven't set the game location yet.");
                return;
            }
            player.kickPlayer("§cGame is under setup, come back later!");
            return;
        }

        if (players.contains(player)) {
            player.sendMessage(Handler.INSTANCE.getConfig("otherMessages.alreadyIn", true).toString());
            return;
        }

        final String worldName = player.getWorld().getName();

        if (!(worldName.equalsIgnoreCase("airLobby") || worldName.equalsIgnoreCase("bedwarsLobby"))) {
            player.sendMessage(Handler.INSTANCE.getConfig("otherMessages.inAnotherGame", true).toString());
            return;
        }

        saveInventory(player);
        getPlayerReady(player);
        player.setFoodLevel(20);

        players.add(player);
        NPCHandler.INSTANCE.loadNPCs(player);
        PacketReader.INSTANCE.inject(player);
    }

    public void leave(final Player player) {
        if (!players.contains(player)) {
            player.sendMessage(Handler.INSTANCE.getConfig("otherMessages.notIn", true).toString());
            return;
        }

        if (lastDamage.containsKey(player.getUniqueId())) {
            death(player, lastDamage.get(player.getUniqueId()));
        }

        lastDamage.remove(player.getUniqueId());
        players.remove(player);
        if (scoreboards.containsKey(player.getUniqueId())) {
            scoreboards.get(player.getUniqueId()).delete();
            scoreboards.remove(player.getUniqueId());
        }

        restoreInventory(player);

        NPCHandler.INSTANCE.unloadNPCs(player);
        PacketReader.INSTANCE.uninject(player);

        final Object o = Handler.INSTANCE.getData("lobby");
        if (o instanceof Location) Handler.INSTANCE.runTask(() -> player.teleport((Location) o));
    }

    public void death(final Player victim, final Player attacker) {
        victim.getInventory().clear();
        addDeath(victim.getUniqueId());
        victim.setHealth(victim.getMaxHealth());

        if (attacker != null) {
            addKill(attacker.getUniqueId());
            addKillStreak(attacker.getUniqueId());
            attacker.setHealth(attacker.getMaxHealth());
            getPlayerReady(attacker);

            Object o;
            if (api != null) {
                o = Handler.INSTANCE.getConfig("kill.coins", false);
                api.getEconomyManager().getEconomySystem().addCoins(api.getUserManager().getUser(attacker), o == null ? 20 : (Integer) o);
            }

            o = Handler.INSTANCE.getConfig("players." + attacker.getUniqueId() + ".savedInventory.pearl", false);

            if (attacker.getInventory().contains(Material.ENDER_PEARL) || !(o instanceof Integer)) {
                attacker.getInventory().addItem(pearl);
                return;
            }

            attacker.getInventory().setItem((Integer) o, pearl);
        }

        broadcast(attacker != null ? "§7[§e⚔§7] §d§l" + attacker.getDisplayName() +"§7got killed by §d§l" + attacker.getDisplayName() : "§7[§e⚔§7] §d§l" + victim.getDisplayName() + " §7died.");

        Handler.INSTANCE.runTaskLater(() -> {
            if (victim.isOnline()) getPlayerReady(victim);
        }, 1);
    }

    public void placeTemp(final Block block) {
        tempBlocks.add(block);

        final int[] i = {0};

        new BukkitRunnable() {
            @Override
            public void run() {
                i[0]++;

                ((CraftServer) Bukkit.getServer()).getHandle().sendPacketNearby(block.getX(), block.getY(), block.getZ(), 120, ((CraftWorld) block.getWorld()).getHandle().dimension, new PacketPlayOutBlockBreakAnimation(new Random().nextInt(5000), new BlockPosition(block.getX(), block.getY(), block.getZ()), i[0]));

                if (i[0] == 10) {
                    Handler.INSTANCE.runTask(() -> {
                        try {
                            for (Sound sound : Sound.values()) {
                                Field f = CraftSound.class.getDeclaredField("sounds");
                                f.setAccessible(true);

                                String[] sounds = (String[]) f.get(null);
                                Method getBlock = CraftBlock.class.getDeclaredMethod("getNMSBlock");
                                getBlock.setAccessible(true);
                                Object nmsBlock = getBlock.invoke(block);
                                net.minecraft.server.v1_8_R3.Block nms = (net.minecraft.server.v1_8_R3.Block) nmsBlock;

                                if (nms.stepSound.getBreakSound()
                                        .equals(sounds[sound.ordinal()])) {
                                    block.getWorld().playSound(block.getLocation(), sound, 1, 1);
                                }
                            }
                        }  catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException |
                                  InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }

                        block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType(), 120);
                        block.setType(Material.AIR);

                        tempBlocks.remove(block);
                    });
                    this.cancel();
                }
            }
        }.runTaskTimer(KnockOut.INSTANCE, 0, 20);
    }

    public void getPlayerReady(final Player player) {
        final PlayerInventory playerInventory = player.getInventory();

        playerInventory.clear();
        playerInventory.setHelmet(null);
        playerInventory.setChestplate(null);
        playerInventory.setLeggings(null);
        playerInventory.setBoots(null);

        Object o = Handler.INSTANCE.getData("players." + player.getUniqueId() + ".savedInventory.stick");
        playerInventory.setItem((o instanceof Integer ? (Integer) o : 0), knockBackStick);

        o = Handler.INSTANCE.getData("players." + player.getUniqueId() + ".savedInventory.bow");
        playerInventory.setItem((o instanceof Integer ? (Integer) o : 1), knockBackBow);

        o = Handler.INSTANCE.getData("players." + player.getUniqueId() + ".savedInventory.blocks");
        playerInventory.setItem((o instanceof Integer ? (Integer) o : 2), blocks);

        Handler.INSTANCE.runTask(() -> player.teleport(spawn));

        player.setFireTicks(0);
        updateScoreboard(player);
    }

    public void saveInventory(final Player player) {
        savedInventories.put(player.getUniqueId(), new SavedInventory(player, true));
    }

    public void restoreInventory(final Player player) {
        if (savedInventories.containsKey(player.getUniqueId())) {
            savedInventories.get(player.getUniqueId()).restoreInventory();
            savedInventories.remove(player.getUniqueId());
        }
    }

    public final int getKills(final UUID uuid) {
        int i = 0;
        final Object o = Handler.INSTANCE.getData("players." + uuid + ".kills");
        if (o instanceof Integer)
            i = (Integer) o;
        return i;
    }

    public final int getKillStreaks(final UUID uuid) {
        return killStreaks.getOrDefault(uuid, 0);
    }

    public final int getDeaths(final UUID uuid) {
        int i = 0;
        final Object o = Handler.INSTANCE.getData("players." + uuid + ".deaths");
        if (o instanceof Integer)
            i = (Integer) o;
        return i;
    }

    private void addKill(final UUID uuid) {
        Handler.INSTANCE.writeData("players." + uuid + ".kills", getKills(uuid)+1);
    }

    public void addKillStreak(final UUID uuid) {
        killStreaks.put(uuid, getKillStreaks(uuid)+1);
    }

    public void addDeath(final UUID uuid) {
        Handler.INSTANCE.writeData("players." + uuid + ".deaths", getDeaths(uuid)+1);
    }

    public void updateScoreboard(final Player player) {
        new Thread(() -> {
            FastBoard scoreboard = scoreboards.getOrDefault(player.getUniqueId(), null);

            if (scoreboard == null) {
                scoreboard = new FastBoard(player);
                scoreboards.put(player.getUniqueId(), scoreboard);
            }

            scoreboard.updateTitle(Handler.INSTANCE.getConfig("scoreboard.title", true).toString());
            scoreboard.updateLines(Handler.INSTANCE.getConfigList("scoreboard.lines", true, player));
        }).start();
    }

    public ItemStack createItemStack(final Material material, final String name, final List<String> lore, int amount, boolean unbreakable) {
        ItemStack item = new ItemStack(material, amount);
        final ItemMeta meta = item.getItemMeta();

        if (meta != null) {

            if (name != null)
                meta.setDisplayName(name);

            if (lore != null)
                meta.setLore(lore);

            item.setItemMeta(meta);
        }

        if (unbreakable) {
            final net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
            final NBTTagCompound tag = nmsStack.getTag();
            tag.setBoolean("Unbreakable", true);
            nmsStack.setTag(tag);
            item = CraftItemStack.asBukkitCopy(nmsStack);
        }
        return item;
    }

    public String replaceStringWithData(final UUID uuid, final String input) {
        return input.replace("%kills%", String.valueOf(getKills(uuid)))
                .replace("%deaths%", String.valueOf(getDeaths(uuid)))
                .replace("%killStreaks%", String.valueOf(getKillStreaks(uuid)));
    }


    // Internal Functions
    private void quickLeave(final Player player) {
        PacketReader.INSTANCE.uninject(player);

        if (scoreboards.containsKey(player.getUniqueId())) {
            scoreboards.get(player.getUniqueId()).delete();
            scoreboards.remove(player.getUniqueId());
        }

        restoreInventory(player);

        final Object o = Handler.INSTANCE.getData("lobby");
        if (o instanceof Location) player.teleport((Location) o);
    }
}
