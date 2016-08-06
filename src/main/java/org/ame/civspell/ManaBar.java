package org.ame.civspell;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

class ManaBar implements Runnable {
    ManaBar(Main mainPlugin) {
        this.mainPlugin = mainPlugin;
    }

    private Main mainPlugin;

    @Override
    public void run() {
        // It is assumed that this function is called every second.
        for (Player p : mainPlugin.getServer().getOnlinePlayers()) {
            int maxMana = getPlayerEquiptmentManaPoints(p);
            int currentMana = p.getLevel();
            p.setExp((float) currentMana / maxMana);
            if (currentMana == maxMana) {
                continue;
            } else if (currentMana > maxMana) {
                p.setLevel(maxMana);
                p.setExp(1.0f);
                continue;
            }
            float regenPerSecondFloat = getRegenRate(maxMana);
            int regenPerSecond;
            if (regenPerSecondFloat < 1) {
                regenPerSecond = 0;
                // random mana
            } else {
                regenPerSecond = (int) regenPerSecondFloat;
            }
            // If regen would put the player above the max mana, set it to the max mana instead.
            if (currentMana < maxMana && currentMana + regenPerSecond >= maxMana) {
                p.setLevel(maxMana);
                p.setExp(1.0f);
            } else {
                p.setLevel(currentMana + regenPerSecond);
                p.setExp(((float)(currentMana + regenPerSecond)) / maxMana);
            }
        }
    }

    private int getRegenRate(int maxMana) {
        double minutesToRegenFully = mainPlugin.getConfig().getDouble("Minutes_To_Regen_Fully");
        int secondsToRegenFully = (int)minutesToRegenFully*60;
        if (maxMana / secondsToRegenFully == 0) {
            return 1;
        }
        return maxMana / secondsToRegenFully;
    }

    private int getPlayerEquiptmentManaPoints(Player p) {
        int headBonus = getItemManaPointBonus(p.getInventory().getHelmet());
        int chestBonus = getItemManaPointBonus(p.getInventory().getChestplate());
        int legBonus = getItemManaPointBonus(p.getInventory().getLeggings());
        int bootBonus = getItemManaPointBonus(p.getInventory().getBoots());
        int mainHandBonus = getItemManaPointBonus(p.getInventory().getItemInMainHand());
        int offHandBonus = getItemManaPointBonus(p.getInventory().getItemInOffHand());
        int maxMana = headBonus + chestBonus + legBonus + bootBonus + mainHandBonus + offHandBonus +
                mainPlugin.getConfig().getInt("Player_Starting_Mana");
        if (maxMana < 0) {
            return 0;
        } else {
            return maxMana;
        }
    }

    private static int getItemManaPointBonus(ItemStack i) {
        int manaBonus = 0;
        if (i == null || i.getItemMeta() == null || i.getType() == Material.AIR) {
            return 0;
        } else if (i.getItemMeta().hasLore()) {
            for (String lore : i.getItemMeta().getLore()) {
                if (lore.matches("§r  §9\\+\\n+ Mana Points")) {
                    String itemBonusStr = lore.replaceAll("[^\\n\\-]", "").replaceFirst("9", "");
                    // Remove all non-number characters, then the first 9 from the color code.
                    manaBonus += Integer.parseInt(itemBonusStr);
                }
            }
        }
        return manaBonus;
    }
}
