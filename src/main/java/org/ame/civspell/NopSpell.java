package org.ame.civspell;

import org.bukkit.Effect;
import org.bukkit.event.player.PlayerInteractEvent;

class NopSpell implements Spell {

    @Override
    public void cast(PlayerInteractEvent event) {
        event.getPlayer().sendMessage("§3The spell wooshes and does nothing.");
        event.getPlayer().getEyeLocation().getWorld().playEffect(event.getPlayer().getLocation(), Effect.EXPLOSION_HUGE, null, 10);
        event.getPlayer().getEyeLocation().getWorld().playEffect(event.getPlayer().getLocation(), Effect.GHAST_SHOOT, null, 10);
    }

    @Override
    public boolean canBeScroll() {
        return true;
    }

    @Override
    public boolean canBePage() {
        return true;
    }

    @Override
    public boolean canBeMemorized() {
        return true;
    }
}
