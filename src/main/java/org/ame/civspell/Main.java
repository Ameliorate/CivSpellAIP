package org.ame.civspell;

import org.ame.civspell.builtin.NopSpell;
import org.ame.civspell.gameplay.ManaBar;
import org.ame.civspell.gameplay.Scroll;
import org.ame.civspell.gameplay.SpellBook;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;


public class Main extends JavaPlugin {
    public Database database;

    @Override
    public void onEnable() {
        Configuration config = this.getConfig();
        config.addDefault("Scroll_Name_Format", "§5§ka§r§bMagic Scroll§5§ka§r {NAME}");
        if (StringUtils.countMatches((String)config.get("Scroll_Name_Format"), "{NAME}") != 1) {
            throw new IllegalArgumentException("There must be exactly 1 {NAME} tag in Scroll_Name_Format.");
        }
        config.addDefault("Spellbook_Enabled", true);
        config.addDefault("Spellbook_Name", "§bSpellbook");
        config.addDefault("Spellpage_Name_Format", "§5§ka§r§bSpell Page§5§ka§r {NAME}");
        config.addDefault("Player_Starting_Mana", 55);
        config.addDefault("Mana_Per_Half_Heart", 1);
        config.addDefault("Minutes_To_Regen_Fully", 5d);

        config.addDefault("Mysql.username", "user");
        config.addDefault("Mysql.password", "pass");
        config.addDefault("Mysql.database", "civspellapi");
        config.addDefault("Mysql.hostname", "localhost");
        config.addDefault("Mysql.port", 3306);

        config.options().copyDefaults(true);
        saveConfig();

        database = new Database(config.getString("Mysql.username"), config.getString("Mysql.password"),
                config.getString("Mysql.database"), config.getString("Mysql.hostname"), config.getInt("Mysql.port"),
                this);
        database.connect();

        SpellManager.addSpell("nop", new NopSpell());

        this.getCommand("givescroll").setExecutor(new CommandGiveScroll(this, (String)config.get("Scroll_Name_Format")));

        getServer().getPluginManager().registerEvents(new Scroll(this, (String)config.get("Scroll_Name_Format")), this);
        getServer().getPluginManager().registerEvents(new SpellBook(this), this);

        getServer().getScheduler().runTaskTimer(this, new ManaBar(this), 0, 20);
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
