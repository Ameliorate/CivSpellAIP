package org.ame.civspell;


import org.bukkit.event.player.PlayerInteractEvent;

public interface Spell {
    void cast(PlayerInteractEvent event);

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
}
