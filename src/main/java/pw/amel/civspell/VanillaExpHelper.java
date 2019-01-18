package pw.amel.civspell;

import org.bukkit.entity.Player;

/**
 * Several methods assisting in using the mana/exp bar.
 */
public class VanillaExpHelper {
    /**
     * Calculates the number of exp points in a given level using numbers from the minecraft wiki.
     */
    public static int xpInLevel(int level) {
        if(level <= 15) {
            return 2 * level + 7;
        } else if(level <= 30) {
            return 5 * level - 38;
        }
        return 9 * level - 158;
    }

    /**
     * @return The amount of exp the player has past the previous level.
     */
    public static float getPlayerExtraXp(int playerLevel, float percentNext) {
        return percentNext * xpInLevel(playerLevel + 1);
    }

    /**
     * @return The total number of exp orbs the player with the given level has.
     */
    public static float totalXp(int playerLevel, float percentNext) {
        float extraXp = percentNext * xpInLevel(playerLevel + 1);

        if (playerLevel == 0) {
            return extraXp;
        } else {
            return totalXp(playerLevel - 1, 0) + extraXp;
        }
    }

    /**
     * Subtracts the given amount of XP points from the given player.
     * @return If the player has enough exp. If the player didn't have enough, no exp is subtracted.
     */
    public static boolean subtractXp(Player player, float orbsToSubtract) {
        // Credit to Soerxpso for the helper function. This took me days when I tried.

        // get initial values
        int playerLevel = player.getLevel();
        float percentNext = player.getExp();

        if (totalXp(playerLevel, percentNext) > orbsToSubtract) {
            return false;
        }

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
        float extraXp = getPlayerExtraXp(playerLevel, percentNext);

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

    // Calculate total experience up to a level
    public static int getExpAtLevel(int level){
        if(level <= 16){
            return (int) (Math.pow(level,2) + 6*level);
        } else if(level <= 31){
            return (int) (2.5*Math.pow(level,2) - 40.5*level + 360.0);
        } else {
            return (int) (4.5*Math.pow(level,2) - 162.5*level + 2220.0);
        }
    }

    // Calculate player's current EXP amount
    public static int getPlayerExp(Player player){
        int exp = 0;
        int level = player.getLevel();

        // Get the amount of XP in past levels
        exp += getExpAtLevel(level);

        // Get amount of XP towards next level
        exp += Math.round(xpInLevel(level) * player.getExp());

        return exp;
    }

    // Give or take EXP
    public static boolean changePlayerExp(Player player, int exp) {
        if (exp == 0)
            return true;
        else if (exp > 0)
            player.giveExp(exp);
        else if (exp < 0)
            return subtractXp(player, Math.abs(exp));
        return true;
    }
}
