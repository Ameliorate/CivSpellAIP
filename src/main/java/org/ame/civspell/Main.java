package org.ame.civspell;

import org.ame.civspell.builtin.NopSpell;
import org.ame.civspell.gameplay.Memorization;
import org.ame.civspell.gameplay.Scroll;
import org.ame.civspell.gameplay.SpellBook;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {
    public Database database;
    public SpellConfig config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.config = new SpellConfig(getConfig());

        database = new Database(config.getMySQLUsername(), config.getMySQLPassword(),
                config.getMySQLDatabase(), config.getMySQLHostname(), config.getMySQLPort(),
                this);
        database.connect();

        SpellManager.addSpell("nop", new NopSpell());

        this.getCommand("givescroll").setExecutor(new CommandGiveScroll(this, config.getSpellNameFormat()));

        getServer().getPluginManager().registerEvents(new Scroll(this, config.getSpellNameFormat()), this);
        getServer().getPluginManager().registerEvents(new SpellBook(this), this);
        getServer().getPluginManager().registerEvents(new Memorization(this), this);
    }

    @Override
    public void onDisable() {
        database.close();
    }

    /**
     * Removes the given itemstack from either the player's main hand or their offhand, preferring offhand first.
     * @return If there were enough items of the right kind to remove. If this is false, no items have been removed from the player's inventory.
     */
    public static boolean removeFromEitherMainOrOffHand(ItemStack item, PlayerInventory player) {
        ItemStack mainHandItem = player.getItemInMainHand();
        ItemStack offHandItem = player.getItemInOffHand();
        if (mainHandItem.getAmount() < item.getAmount() && offHandItem.getAmount() < item.getAmount()) {
            return false;
        } else if (mainHandItem.isSimilar(item)) {
            if (mainHandItem.getAmount() == item.getAmount()) {
                player.setItemInMainHand(new ItemStack(Material.AIR));
                return true;
            } else {
                mainHandItem.setAmount(mainHandItem.getAmount() - item.getAmount());
                player.setItemInMainHand(mainHandItem);
                return true;
            }
        } else if (offHandItem.isSimilar(item)) {
            if (offHandItem.getAmount() == item.getAmount()) {
                player.setItemInOffHand(new ItemStack(Material.AIR));
                return true;
            } else {
                offHandItem.setAmount(offHandItem.getAmount() - item.getAmount());
                player.setItemInOffHand(offHandItem);
                return true;
            }
        } else {
            return false;
        }
    }
}
