package org.ame.civspell;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

class Scroll implements Listener {
    Scroll(Main mainPlugin) {
        this.mainPlugin = mainPlugin;
    }

    private Main mainPlugin;
    private static HashMap<Player, Boolean> isOnCooldown = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Handle cooldowns.
        if (isOnCooldown.get(event.getPlayer()) != null && isOnCooldown.get(event.getPlayer())) {
            return;
        }
        // Check if it's a scroll.
        else if (!(event.getMaterial() == Material.SUGAR_CANE) ||
                !(event.getItem().getItemMeta().getDisplayName().startsWith("Magic Scroll -- "))) {
            return;
        }
        // Make it not trigger stepping on redstone.
        else if (event.getAction() == Action.PHYSICAL) {
            return;
        }
        String spellName = "";
        for (String part : event.getItem().getItemMeta().getDisplayName().split("Magic Scroll -- ")) {
            spellName = spellName + part + "Magic Scroll -- ";
            spellName = spellName.replaceAll("(Magic Scroll -- )+$", ""); // Remove last "Magic Scroll -- " from string.
        }

        Spell spell = SpellManager.spellMap.get(spellName);
        if (spell == null) {
            event.getPlayer().sendMessage("Please report to the server admins how you got a scroll of " +
                    "an invalid spell '" + spellName + "'.");
            event.getPlayer().getInventory().removeItem(new ItemStack(event.getItem()));
            return;
        }
        if (spell.canBeScroll()) {
            spell.cast(event);
            ItemStack toRemove = new ItemStack(event.getItem());
            toRemove.setAmount(1);
            event.getPlayer().getInventory().removeItem(toRemove);
            isOnCooldown.put(event.getPlayer(), true);
            mainPlugin.getServer().getScheduler()
                    .runTaskLater(mainPlugin, () -> isOnCooldown.put(event.getPlayer(), false), 5);
        } else {
            event.getPlayer().sendMessage("Please report to the server admins how you got a scroll of " +
                    "a non-scrollable spell '" + spellName + "'.");
            event.getPlayer().getInventory().removeItem(new ItemStack(event.getItem()));
        }
    }
}
