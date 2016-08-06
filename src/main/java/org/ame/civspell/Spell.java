package org.ame.civspell;


import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
     * @return If the spell casted correctly and should deduct mana or scrolls.
     */
    boolean cast(Block blockClicked, Player player, Action action, BlockFace face, ItemStack itemUsed, SpellCastMethod castForm);

    /**
     * @return Weather or not the spell can occur as a scroll.
     */
    boolean canBeScroll();

    /**
     * @return If the spell can occur as a page.
     */
    boolean canBePage();

    /**
     * @return If the spell can be memorized and casted without a book.
     */
    boolean canBeMemorized();

    /**
     * @return The number of mana points used by one cast of the spell in spellbook form.
     */
    int manaUsage();
}
