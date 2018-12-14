package pw.amel.civspell.builtin;

import pw.amel.civspell.Spell;
import pw.amel.civspell.SpellCastMethod;
import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

/**
 * Simple test spell that makes a sound and some particles.
 */
public class NopSpell implements Spell {
    @Override
    public boolean cast(Block blockClicked, Player player, Action action, BlockFace face, ItemStack itemUsed,
                        SpellCastMethod castForm, ConfigurationSection config) {
        player.sendMessage("§3The spell wooshes away, doing nothing.");
        player.getEyeLocation().getWorld().playEffect(player.getLocation(), Effect.EXPLOSION_HUGE, null, 10);
        player.getEyeLocation().getWorld().playEffect(player.getLocation(), Effect.GHAST_SHOOT, null, 10);
        return true;
    }
}
