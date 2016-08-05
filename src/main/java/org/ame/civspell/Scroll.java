package org.ame.civspell;



import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;


class Scroll implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if it's a scroll.
        if (!(event.getMaterial() == Material.SUGAR_CANE) ||
                !(event.getItem().getItemMeta().getDisplayName().startsWith("Magic Scroll -- "))) {
            return;
        }
        // Make it not trigger stepping on redstone.
        else if (event.getAction() == Action.PHYSICAL) {
            return;
        }
        // Handle shift-clicking. (Is this needed?)
        else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.getPlayer().isSneaking()) {
            return;
        }

        String spellName = "";
        for (String part: event.getItem().getItemMeta().getDisplayName().split("Magic Scroll -- ")) {
            spellName = spellName + part + "Magic Scroll -- ";
            spellName = spellName.replaceAll("(Magic Scroll -- )+$", ""); // Remove last "Magic Scroll -- " from string.
        }

        Spell spell = SpellManager.spellMap.get(spellName);
        if (spell == null) {
            event.getPlayer().sendMessage("Please report to the server admins how you got a scroll of" +
                    "an invalid spell '" + spellName + "'.");
            event.getPlayer().getInventory().removeItem(new ItemStack(event.getItem()));
            return;
        }
        if (spell.canBeScroll()) {
            spell.cast(event);
            ItemStack toRemove = new ItemStack(event.getItem());
            toRemove.setAmount(1);
            event.getPlayer().getInventory().removeItem(toRemove);
        } else {
            event.getPlayer().sendMessage("Please report to the server admins how you got a scroll of" +
                    "a non-scrollable spell '" + spellName + "'.");
            event.getPlayer().getInventory().removeItem(new ItemStack(event.getItem()));
        }
    }
}
