package org.pebbleprojects.knockout;

import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.StringUtil;
import org.pebbleprojects.knockout.handlers.Handler;
import org.pebbleprojects.knockout.handlers.ParticleHandler;
import org.pebbleprojects.knockout.handlers.PlayerDataHandler;
import org.pebbleprojects.knockout.npc.NPC;
import org.pebbleprojects.knockout.npc.NPCHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Command implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(final CommandSender sender, final org.bukkit.command.Command command, final String label, final String[] args) {
        new Thread(() -> {
            if (args.length == 0) {
                sendHelpList(sender);
                return;
            }

            if (sender.hasPermission("knockout.admin")) {
                if (sender instanceof Player) {
                    final Player player = (Player) sender;

                    if (args[0].equalsIgnoreCase("setLobby")) {
                        Handler.INSTANCE.writeData("lobby", player.getLocation());

                        player.sendMessage("§7§l- §5FalconMC§7§l - > Set lobby location to your current location");
                        return;
                    }

                    if (args[0].equalsIgnoreCase("addSpawn")) {
                        Handler.INSTANCE.writeData("spawns." + (Handler.INSTANCE.getSpawns().size()+1), player.getLocation());

                        player.sendMessage("§7§l- §5FalconMC§7§l - > Added spawn to your current location");
                        return;
                    }

                    if (args[0].equalsIgnoreCase("setInventoryNPC")) {
                        player.sendMessage("§eCreating Inventory Saver NPC...");
                        final NPC npc = NPCHandler.INSTANCE.createNPC(Handler.INSTANCE.getConfig("inventory-saver-npc.name", true).toString(), player.getLocation(), 0);
                        npc.setSkin(Handler.INSTANCE.getConfig("inventory-saver-npc.skinName", false).toString());
                        npc.save();

                        for (final Player p : PlayerDataHandler.INSTANCE.players) npc.sendShowPacket(p);

                        player.sendMessage("§aCreated Inventory Saver NPC at your location.");
                        if (!PlayerDataHandler.INSTANCE.players.contains(player)) {
                            player.sendMessage("§eNOTE: as you're not in the game, you won't be able to see the NPC. Join to see the NPC you have created!");
                        }
                        return;
                    }
                }

                if (args[0].equalsIgnoreCase("reload")) {
                    sender.sendMessage("§eReloading...");
                    NPCHandler.INSTANCE.unloadNPCs();
                    Handler.INSTANCE.updateConfig();
                    Handler.INSTANCE.updateData();
                    NPCHandler.INSTANCE.loadNPCs();
                    sender.sendMessage("§aReloaded!");
                    return;
                }

                if (args[0].equalsIgnoreCase("removeNPC")) {
                    if (args.length >= 2) {
                        try {
                            final int id = Integer.parseInt(args[1]);
                            if (id >= 1) {
                                if (Handler.INSTANCE.getData("NPCs." + id + ".name") != null) {
                                    Handler.INSTANCE.writeData("NPCs." + id, null);

                                    NPCHandler.INSTANCE.deleteNPC(id);

                                    sender.sendMessage("§aSuccessfully removed NPC with ID of §e" + id);
                                    return;
                                }
                            }
                            sender.sendMessage("§cThere is no NPC with such an ID!");
                            return;
                        } catch (final NumberFormatException ignored) {
                            sender.sendMessage("§cID of NPCs start from 1 to above.");
                            return;
                        }
                    }
                }
            }

            if (sender instanceof Player) {
                final Player player = (Player) sender;

                if (args[0].equalsIgnoreCase("join")) {
                    PlayerDataHandler.INSTANCE.join(player);
                    return;
                }

                if (args[0].equalsIgnoreCase("leave")) {
                    PlayerDataHandler.INSTANCE.leave(player);
                    return;
                }

                if (args[0].equalsIgnoreCase("saveInventory")) {
                    if (!PlayerDataHandler.INSTANCE.players.contains(player)) {
                        player.sendMessage("§cYou're not in KnockOut!");
                        return;
                    }

                    final PlayerInventory inventory = player.getInventory();
                    final ItemStack[] items = inventory.getContents();

                    Material material;
                    ItemStack itemStack;

                    for (int i = 0; i < items.length; i++) {
                        itemStack = items[i];

                        if (itemStack == null) continue;

                        material = itemStack.getType();

                        if (material == Material.STICK) {
                            Handler.INSTANCE.writeData("players." + player.getUniqueId() + ".savedInventory.stick", i);
                            continue;
                        }
                        if (material == Material.SANDSTONE) {
                            Handler.INSTANCE.writeData("players." + player.getUniqueId() + ".savedInventory.blocks", i);
                            continue;
                        }
                        if (material == Material.ENDER_PEARL) {
                            Handler.INSTANCE.writeData("players." + player.getUniqueId() + ".savedInventory.pearl", i);
                            continue;
                        }
                        if (material == Material.BOW) {
                            Handler.INSTANCE.writeData("players." + player.getUniqueId() + ".savedInventory.bow", i);
                            continue;
                        }
                        if (material == Material.FEATHER) {
                            Handler.INSTANCE.writeData("players." + player.getUniqueId() + ".savedInventory.speed", i);
                            continue;
                        }
                        if (material == Material.GOLD_PLATE) {
                            Handler.INSTANCE.writeData("players." + player.getUniqueId() + ".savedInventory.launchpad", i);
                            continue;
                        }
                        if (material == Material.ARROW) {
                            Handler.INSTANCE.writeData("players." + player.getUniqueId() + ".savedInventory.arrow", i);
                        }
                    }

                    player.sendMessage("§aSaved your inventory slots");
                    return;
                }

                if (args[0].equalsIgnoreCase("test")) {
//                    ParticleHandler.INSTANCE.playParticle(player);
                    return;
                }
            }

            sendHelpList(sender);
        }).start();
        return false;
    }


    // Internal Functions
    private void sendHelpList(final CommandSender sender) {
        for (final String s : Handler.INSTANCE.getConfigList("otherMessages.invalidArguments." + (sender.hasPermission("knockout.admin") ? "admin" : "player"), true, null)) sender.sendMessage(s);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final org.bukkit.command.Command command, final String label, final String[] args) {
        final List<String> completions = new ArrayList<>(), choices = new ArrayList<>();

        final boolean isPlayer = sender instanceof Player;

        if (isPlayer) {
            choices.addAll(Arrays.asList("join", "leave", "saveInventory"));
        }

        if (sender.hasPermission("knockout.admin")) {
            choices.addAll(Arrays.asList("reload", "removeNPC"));

            if (isPlayer) {
                choices.addAll(Arrays.asList("setLobby", "addSpawn"));
            }
        }

        StringUtil.copyPartialMatches(args[0], choices, completions);

        return completions;
    }
}
