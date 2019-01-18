package pw.amel.civspell.builtin;

import org.bukkit.configuration.ConfigurationSection;
import pw.amel.civspell.VanillaExpHelper;
import pw.amel.civspell.spell.CastData;
import pw.amel.civspell.spell.Effect;

public class ManaSpellEffect implements Effect {
    public ManaSpellEffect(ConfigurationSection config) {
        cost = config.getDouble("manaCost");
    }

    private double cost;

    @Override
    public void cast(CastData castData) {
        if (castData.main.config.useVanillaExpMana) {
            vanillaExp(castData);
        } else {
            linearExp(castData);
        }
    }

    private void vanillaExp(CastData castData) {
        boolean result = VanillaExpHelper.changePlayerExp(castData.player, (int) -cost);
        if (!result)
            castData.returnCast();
        else {
            castData.addReturnHook(() -> VanillaExpHelper.changePlayerExp(castData.player, (int) cost));
        }

    }

    private void linearExp(CastData castData) {
        float playerMana = castData.player.getLevel() + castData.player.getExp();

        if (playerMana < cost) {
            castData.returnCast();
            return;
        }

        float newPlayerMana = (float) (playerMana - cost);
        int newPlayerLevel = (int) Math.floor(newPlayerMana);
        float newPlayerPercent = newPlayerMana % 1;

        castData.player.setLevel(newPlayerLevel);
        castData.player.setExp(newPlayerPercent);

        if (!castData.isAlreadyReturned())
            castData.addReturnHook(() -> {
                int playerLevel = castData.player.getLevel();
                float playerPercent = castData.player.getExp();

                int newLevel = playerLevel + (int) Math.floor(cost);
                float newPercent = (float) (playerPercent + cost % 1);

                castData.player.setLevel(newLevel);
                castData.player.setExp(newPercent);
            });
    }
}
