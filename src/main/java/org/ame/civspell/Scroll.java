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
    Scroll(Main mainPlugin, String formatString) {
        this.mainPlugin = mainPlugin;
        String[] fs = formatString.split("\\{NAME}");
        assert fs.length == 1 || fs.length == 2;
        this.formatStringPrefix = fs[0];
        if (fs.length == 2) {
            this.formatStringPostfix = fs[1];
        } else {
            this.formatStringPostfix = "";
        }
    }

    private Main mainPlugin;
    private String formatStringPrefix;
    private String formatStringPostfix;
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
                !(event.getItem().getItemMeta().getDisplayName().startsWith(formatStringPrefix) &&
                        event.getItem().getItemMeta().getDisplayName().endsWith(formatStringPostfix))) {
            return;
        }
        // Make it not trigger stepping on redstone.
        else if (event.getAction() == Action.PHYSICAL) {
            return;
        }

        // Parse spellName out of the item name.
        String spellName = event.getItem().getItemMeta().getDisplayName().replaceAll("^" + formatStringPrefix, "");
        spellName = spellName.replaceAll(formatStringPostfix + "$", "");

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
