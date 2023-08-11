package org.pebbleprojects.knockout.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SavedInventory {

    private final PlayerInventory inventory;
    private final ItemStack helmet;
    private final ItemStack chestPlate;
    private final ItemStack leggings;
    private final ItemStack boots;
    private final ItemStack[] itemStacks;

    public SavedInventory(final Player owner, final boolean cleanAfterSave) {
        inventory = owner.getInventory();

        helmet = inventory.getHelmet();
        chestPlate = inventory.getChestplate();
        leggings = inventory.getLeggings();
        boots = inventory.getBoots();

        itemStacks = inventory.getContents();
        if (cleanAfterSave)
            inventory.clear();
    }

    public void restoreInventory() {
        inventory.setHelmet(helmet);
        inventory.setChestplate(chestPlate);
        inventory.setLeggings(leggings);
        inventory.setBoots(boots);
        inventory.setContents(itemStacks);
    }

}
