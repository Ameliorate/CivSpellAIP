package org.ame.civspell;


import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

public interface Spell {
    /**
     * @param blockClicked The block clicked to cast the spell.
     * @param player The player who casted the spell.
     * @param action If the spell was triggered with a left click or a right click.
     * @param face The face of the block clicked, if any.
     * @param itemUsed The item used to cast the spell.
     * @param castForm The method used to cast the spell, such as a scroll or spellbook.
     * @param config A config that's provided to allow spells to have variable functionality in the civspellapi's config
     *               without needing complex configs on the plugin adding the spell's side.
     * @return If the spell casted correctly and should deduct mana or scrolls.
     */
    boolean cast(Block blockClicked, Player player, Action action, BlockFace face, ItemStack itemUsed,
                 SpellCastMethod castForm, ConfigurationSection config);
}
