package pw.amel.civspell.builtin;

import org.bukkit.configuration.ConfigurationSection;
import pw.amel.civspell.spell.CastData;
import pw.amel.civspell.spell.Effect;

public class SoundEffect implements Effect {
    public SoundEffect(ConfigurationSection config) {
        sound = config.getString("sound");
        volume = (float) config.getDouble("volume", 1.0);
        pitch = (float) config.getDouble("pitch", 1.0);
    }

    private String sound;
    private float volume;
    private float pitch;

    @Override
    public void cast(CastData castData) {
        castData.player.playSound(castData.player.getEyeLocation(), sound, volume, pitch);
    }
}
