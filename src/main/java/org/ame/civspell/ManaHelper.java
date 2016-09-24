package org.ame.civspell;

import org.bukkit.entity.Player;

/**
 * Several methods assisting in using the mana/exp bar.
 */
public class ManaHelper {
    /**
     * Calculates the number of exp points in a given level using numbers from the minecraft wiki.
     */
    public static int xpInLevel(int level) {
        if(level <= 16) {
            return 2 * level + 7;
        } else if(level <= 31) {
            return 5 * level - 38;
        }
        return 9 * level - 158;
    }

    /**
     *
     */
    public static float getPlayerExtraXp(Player player) {
        int playerLevel = player.getLevel();
        float percentNext = player.getExp();
        return percentNext * xpInLevel(playerLevel + 1);
    }

    /**
     * Subtracts the given amount of XP points from the given player.
     * @return If the player has enough exp. If the player didn't have enough, no exp is subtracted.
     */
    public static boolean subtractXp(Player player, float orbsToSubtract) {
        // Credit to Soerxpso for the helper function. This took me days when I tried.

        // get initial values
        int playerLevel = player.getLevel();
        System.out.println(playerLevel);
        float percentNext = player.getExp();

        // if the player has no extra XP (exactly on the level mark)
        // then treat them as if they have 1 less level and are at 100%
        if(percentNext == 0) {
            if (playerLevel != 0) {
                playerLevel -= 1;
                player.setLevel(playerLevel);
            } else {
                return false;
            }
            percentNext = 1f;
        }

        // just converting the percentage to a concrete value
        float extraXp = percentNext * xpInLevel(playerLevel + 1);

        // if we can subtract the orbs without decrementing the level, do so
        // otherwise, subtract enough to put the player at 0% and recurse
        if(orbsToSubtract <= extraXp) {
            extraXp -= orbsToSubtract;
            float newPercent = extraXp / xpInLevel(playerLevel + 1);
            player.setExp(newPercent);
            return true;
        } else if (playerLevel != 0) {
            player.setExp(0);
            return subtractXp(player, orbsToSubtract - extraXp);
        } else {
            // Out of exp.
            return false;
        }
    }
}
