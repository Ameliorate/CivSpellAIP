package pw.amel.civspell;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import pw.amel.civspell.spell.CastHelper;
import pw.amel.civspell.spell.Effect;
import vg.civcraft.mc.civmodcore.itemHandling.itemExpression.ItemExpression;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 * Parses the config to a convent getter interface.
 */
public class SpellConfig {
    public SpellConfig(Configuration config, CivSpells main) {
        this.main = main;
        reloadConfig(config);
    }

    private CivSpells main;

    public void reloadConfig(Configuration config) {
        spells = new HashMap<>();

        doGlobals(config);

        InputStream defaultSpells = main.getResource("defaultspells.yml");
        ConfigurationSection defaultSpellsConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultSpells))
                .getConfigurationSection("spells");
        parseSpells(defaultSpellsConfig);
        parseSpells(config.getConfigurationSection("spells"));
    }

    public int coolDownTicks;

    public boolean useVanillaExpMana;

    private void doGlobals(Configuration config) {
        coolDownTicks = config.getInt("antiOopsCoolDownTicks", 5);
        String manaMode = config.getString("mana.manaMode", "vanilla");

        if (manaMode.equals("vanilla")) {
            useVanillaExpMana = true;
        } else if (manaMode.equals("linear")) {
            useVanillaExpMana = false;
        } else {
            main.getLogger().log(Level.WARNING, "mana.manaMode is non existant " + manaMode + ". Defaulting to" +
                    " vanilla.");
            useVanillaExpMana = true;
        }
    }

    public HashMap<String, SpellData> spells;

    public static class SpellData {
        public String name;
        public List<Effect> spellDefinition;
        public ItemStack triggerItem;
        public ItemExpression itemExpression;

        /**
         * If you can cast this spell by right clicking with the trigger item.
         */
        public boolean rightClickCast;

        /**
         * If you can cast this spell by left clicking with the trigger item.
         */
        public boolean leftClickCast;
    }

    private void parseSpells(ConfigurationSection spells) {
        for (String spellKey : spells.getKeys(false)) {
            ConfigurationSection spellConfig = spells.getConfigurationSection(spellKey);
            ConfigurationSection spellDefinitionConfig = spellConfig.getConfigurationSection("effects");

            SpellData spellData = new SpellData();
            spellData.name = spellKey;
            spellData.triggerItem = spellConfig.getItemStack("triggerItem");
            spellData.itemExpression = spellConfig.contains("triggerItemExpression") ?
                    ItemExpression.getItemExpression(spellConfig, "triggerItemExpression") :
                    new ItemExpression(spellData.triggerItem, true);
            spellData.leftClickCast = spellConfig.getBoolean("leftClickCast", true);
            spellData.rightClickCast = spellConfig.getBoolean("rightClickCast", true);
            spellData.spellDefinition = CastHelper.parseSpellDefinition(spellDefinitionConfig);

            if (spellData.triggerItem != null && !spellData.itemExpression.matches(spellData.triggerItem))
                main.warning("Spell \"" + spellKey + "\"'s triggerItemExpression does not match its triggerItem.");

            this.spells.put(spellKey, spellData);
        }
    }
}
