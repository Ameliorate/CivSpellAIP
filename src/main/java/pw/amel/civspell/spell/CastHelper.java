package pw.amel.civspell.spell;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import pw.amel.civspell.*;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Contains assorted methods involving casting spells.
 */
public class CastHelper {
    private static HashSet<UUID> isOnCooldown = new HashSet<>();

    /**
     * Casts a spell, properly handling cooldowns and the like.
     * @return Weather or not the spell is on cooldown.
     */
    public static boolean castSpell(List<Effect> spellDefinition, Player caster,
                                    Block blockClicked, Action castAction, BlockFace castBlockFace, String spellName,
                                    ItemStack triggerItem, CivSpells mainPlugin) {
        CastData castData = new CastData();

        castData.player = caster;
        castData.blockClicked = blockClicked;
        castData.blockClickedFace = castBlockFace;
        castData.castAction = castAction;
        castData.spellName = spellName;
        castData.triggerItem = triggerItem;
        castData.main = mainPlugin;

        return castSpell(spellDefinition, castData, false, mainPlugin);
    }

    public static boolean castSpell(List<Effect> spellDefinition, CastData castData, boolean ignoreCooldown, CivSpells mainPlugin) {
        if (!ignoreCooldown && isOnCooldown.contains(castData.player.getUniqueId()))
            return false;

        if (!ignoreCooldown) {
            isOnCooldown.add(castData.player.getUniqueId());
            mainPlugin.getServer().getScheduler()
                    .runTaskLater(mainPlugin, () -> isOnCooldown.remove(castData.player.getUniqueId()), mainPlugin.config.coolDownTicks);
        }

        for (Effect spellI : spellDefinition) {
            spellI.cast(castData);
            if (castData.isAlreadyReturned())
                break;
        }

        return true;
    }
}
