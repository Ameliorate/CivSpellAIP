package pw.amel.civspell.builtin;

import org.bukkit.configuration.ConfigurationSection;
import pw.amel.civspell.SpellConfig;
import pw.amel.civspell.spell.CastData;
import pw.amel.civspell.spell.CastHelper;
import pw.amel.civspell.spell.Effect;

public class MetaSepllEffect implements Effect {
    public MetaSepllEffect(ConfigurationSection config) {
        name = config.getString("name");
    }

    private String name;

    @Override
    public void cast(CastData castData) {
        SpellConfig.SpellData data = castData.main.config.spells.get(name);
        CastHelper.castSpell(data.spellDefinition, castData, true, castData.main);
    }
}
