package pw.amel.civspell;

import pw.amel.civspell.builtin.NopEffect;
import pw.amel.civspell.commands.GiveSpellItem;
import pw.amel.civspell.commands.ReloadCommand;
import pw.amel.civspell.gameplay.SpellCastListener;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;


public class CivSpells extends JavaPlugin {
    public SpellConfig config;

    @Override
    public void onEnable() {
        EffectManager.addEffect("nop", NopEffect.class);

        saveDefaultConfig();
        this.config = new SpellConfig(getConfig(), this);

        getCommand("csgiveitem").setExecutor(new GiveSpellItem(this));
        getCommand("csreload").setExecutor(new ReloadCommand(this));

        getServer().getPluginManager().registerEvents(new SpellCastListener(this), this);
    }

    /**
     * Adds an effect with the given name.
     * @param name The name of the effect that the user will refer to in their configuration.
     * @param effect The effect. It must implement Effect and have a public constructor that takes a
     *               ConfigurationSection as its only argument.
     */
    public void addEffect(String name, Class<?> effect) {
        EffectManager.addEffect(name, effect);
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
