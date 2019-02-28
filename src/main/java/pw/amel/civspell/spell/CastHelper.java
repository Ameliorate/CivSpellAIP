package pw.amel.civspell.spell;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import pw.amel.civspell.*;

import java.util.ArrayList;
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
    public static boolean castSpell(SpellConfig.SpellData spell, Player caster,
                                    Block blockClicked, Action castAction, BlockFace castBlockFace,
                                    ItemStack triggerItem, CivSpells mainPlugin) {
        CastData castData = new CastData();

        castData.player = caster;
        castData.blockClicked = blockClicked;
        castData.blockClickedFace = castBlockFace;
        castData.castAction = castAction;
        castData.spellName = spell.name;
        castData.triggerItem = triggerItem;
        castData.main = mainPlugin;
        castData.exampleItem = spell.triggerItem;
        castData.itemExpression = spell.itemExpression;

        return castSpell(spell.spellDefinition, castData, false, mainPlugin);
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

    public static List<Effect> parseSpellDefinition(ConfigurationSection definition) {
        ArrayList<Effect> spellDefinition = new ArrayList<>();

        for (String type : definition.getKeys(false)) {
            Effect implementation = EffectManager.constructEffect(type,
                    definition.getConfigurationSection(type));
            spellDefinition.add(implementation);
        }

        return spellDefinition;
    }
}
