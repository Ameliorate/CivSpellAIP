package org.ame.civspell;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.UUID;

import static org.ame.civspell.SpellCastMethod.SPELL_BOOK;

/**
 * Contains assorted methods involving casting spells.
 */
public class CastHelper {
    private static HashSet<UUID> isOnCooldown = new HashSet<>();

    /**
     * Casts a spell, properly deducting exp/items if needed.
     * @param mainHandCast If the item the spell was casted with was in the main hand. If castMethod is MEMORIZED,
     *                     this boolean is ignored.
     * @return Weather or not the spell casted properly.
     */
    public static boolean castSpell(String spellName, Player caster, SpellCastMethod castMethod, boolean mainHandCast,
                                    Block blockClicked, Action castAction, BlockFace castBlockFace, Main mainPlugin) {
        Spell spell = SpellManager.spellMap.get(spellName);
        if (spell == null) {
            caster.sendMessage("§cPlease report to the server admins how you got a scroll of " +
                    "an invalid spell '" + spellName + "'.");
            return false;
        } else if ((castMethod == SpellCastMethod.SCROLL && !spell.canBeScroll()) ||
                (castMethod == SPELL_BOOK && !spell.canBePage()) ||
                (castMethod == SpellCastMethod.MEMORIZED && !spell.canBeMemorized())) {
            return false;
        } else if (isOnCooldown.contains(caster.getUniqueId())) {
            return false;
        }

        isOnCooldown.add(caster.getUniqueId());
        mainPlugin.getServer().getScheduler()
                .runTaskLater(mainPlugin, () -> isOnCooldown.remove(caster.getUniqueId()), 5);

        switch (castMethod) {
            case SCROLL:
                ItemStack spellScroll;
                if (mainHandCast) {
                    spellScroll = caster.getInventory().getItemInMainHand();
                } else {
                    spellScroll = caster.getInventory().getItemInOffHand();
                }

                if (spellScroll == null || spellScroll.getAmount() == 0) {
                    return false;
                }

                boolean removeScroll = spell.cast(blockClicked, caster, castAction, castBlockFace, spellScroll, castMethod);
                if (removeScroll) {
                    ItemStack toRemove = new ItemStack(spellScroll);
                    toRemove.setAmount(1);
                    Main.removeFromEitherMainOrOffHand(toRemove, caster.getInventory());
                } else {
                    return false;
                }
                break;

            case SPELL_BOOK:
            case MEMORIZED:
                ItemStack castItem = null;
                if (castMethod == SpellCastMethod.SPELL_BOOK) {
                    if (mainHandCast) {
                        castItem = caster.getInventory().getItemInMainHand();
                    } else {
                        castItem = caster.getInventory().getItemInOffHand();
                    }
                }
                boolean costsExp = spell.cast(blockClicked, caster, castAction, castBlockFace, castItem, castMethod);
                if (costsExp) {
                    int manaUsage = spell.manaUsage();
                    int playerMana = caster.getLevel();
                    boolean enoughMana = ManaHelper.subtractXp(caster, manaUsage);
                    if (!enoughMana) {
                        int remainder = Math.abs(playerMana - manaUsage);
                        int healthMultiplier = mainPlugin.getConfig().getInt("Mana_Per_Half_Heart");
                        float healthToTake = (float) remainder / healthMultiplier;
                        caster.damage(healthToTake);
                        caster.setNoDamageTicks(0);
                        caster.setLevel(0);
                        caster.setExp(0);
                    }
                } else {
                    return false;
                }
                break;
        }
        return true;
    }
}
