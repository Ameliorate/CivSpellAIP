package pw.amel.civspell.builtin;

import org.bukkit.configuration.ConfigurationSection;
import pw.amel.civspell.spell.CastData;
import pw.amel.civspell.spell.Effect;

public class FailEffect implements Effect {
    public FailEffect(ConfigurationSection config) {
        int afterTicks = config.getInt("afterTicks", 0);
        double afterSeconds = config.getDouble("afterSeconds", 0);
        this.afterTicks = ((int) afterSeconds * 20) + afterTicks;
    }

    private int afterTicks;

    @Override
    public void cast(CastData castData) {
        if (afterTicks == 0)
            castData.returnCast();
        else
            castData.main.getServer().getScheduler().runTaskLater(castData.main, castData::returnCast, afterTicks);
    }
}
