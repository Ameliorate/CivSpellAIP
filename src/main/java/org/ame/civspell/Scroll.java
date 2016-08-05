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
            event.setCancelled(true);
            return;
        }
        // Empty hand or vanilla sugar cane where it can't be placed.
        else if (event.getItem() == null) {
            return;
        }
        // Item with no meta.
        else if (event.getItem().getItemMeta() == null) {
            System.out.println("awjrkaht");
            return;
        }
        // Check for default name.
        else if (event.getItem().getItemMeta().getDisplayName() == null) {
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

        // Remove first "Magic Scroll -- " from name.
        String spellName = event.getItem().getItemMeta().getDisplayName().replaceAll("^Magic Scroll -- ", "");

        Spell spell = SpellManager.spellMap.get(spellName);
        if (spell == null) {
            event.getPlayer().sendMessage("Please report to the server admins how you got a scroll of " +
                    "an invalid spell '" + spellName + "'.");
            Main.removeFromEitherMainOrOffHand(new ItemStack(event.getItem()), event.getPlayer().getInventory());
            event.setCancelled(true);
            return;
        }
        if (spell.canBeScroll()) {
            spell.cast(event);
            ItemStack toRemove = new ItemStack(event.getItem());
            toRemove.setAmount(1);
            Main.removeFromEitherMainOrOffHand(toRemove, event.getPlayer().getInventory());
            isOnCooldown.put(event.getPlayer(), true);
            mainPlugin.getServer().getScheduler()
                    .runTaskLater(mainPlugin, () -> isOnCooldown.put(event.getPlayer(), false), 5);
            event.setCancelled(true);
        } else {
            event.getPlayer().sendMessage("Please report to the server admins how you got a scroll of " +
                    "a non-scrollable spell '" + spellName + "'.");
            Main.removeFromEitherMainOrOffHand(new ItemStack(event.getItem()), event.getPlayer().getInventory());
            event.setCancelled(true);
        }
    }
}
