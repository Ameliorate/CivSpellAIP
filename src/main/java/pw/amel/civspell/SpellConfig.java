package pw.amel.civspell;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import pw.amel.civspell.spell.CastHelper;
import pw.amel.civspell.spell.Effect;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        itemsToSpells = new HashMap<>();
        itemsToDefinitions = new HashMap<ItemStack, List<Effect>>();

        doGlobals(config);

        InputStream defaultSpells = main.getResource("defaultspells.yml");
        ConfigurationSection defaultSpellsConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultSpells))
                .getConfigurationSection("spells");
        parseSpells(defaultSpellsConfig);
        parseSpells(config.getConfigurationSection("spells"));

        syncSpells();
    }

    public int coolDownTicks;

    private void doGlobals(Configuration config) {
        coolDownTicks = config.getInt("antiOopsCoolDownTicks", 5);
    }

    public HashMap<String, SpellData> spells;
    public HashMap<ItemStack, SpellData> itemsToSpells;
    public HashMap<ItemStack, List<Effect>> itemsToDefinitions;

    public static class SpellData {
        public String name;
        public List<Effect> spellDefinition;
        public ItemStack triggerItem;

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
            spellData.leftClickCast = spellConfig.getBoolean("leftClickCast", true);
            spellData.rightClickCast = spellConfig.getBoolean("rightClickCast", true);
            spellData.spellDefinition = CastHelper.parseSpellDefinition(spellDefinitionConfig);

            this.spells.put(spellKey, spellData);
        }
    }

    private void syncSpells() {
        for (Map.Entry<String, SpellData> spell : spells.entrySet()) {
            itemsToSpells.put(spell.getValue().triggerItem, spell.getValue());
            itemsToDefinitions.put(spell.getValue().triggerItem, spell.getValue().spellDefinition);
        }
    }
}
