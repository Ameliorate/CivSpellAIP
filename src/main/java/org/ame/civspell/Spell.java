package org.ame.civspell;


import org.bukkit.entity.Player;

public interface Spell {
    void cast(Player caster);

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
     * @return The human-facing name for the spell.
     */
    String getHumanName();
}
