package org.ame.civspell;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

/**
 * Parses the config to a convent getter interface.
 */
public class SpellConfig {
    public SpellConfig(Configuration config) {
        reloadConfig(config);
    }

    public void reloadConfig(Configuration config) {
        this.spellNameFormat = ChatColor.translateAlternateColorCodes('&',
                config.getString("spellNameFormat", "&5&ka&r&bMagic Scroll&5&ka&r {NAME}"));
        this.memorizationEnabled = config.getBoolean("memorizationEnabled", true);
        this.spellbookEnabled = config.getBoolean("spellbookEnabled", true);
        this.spellbookName = ChatColor.translateAlternateColorCodes('&',
                config.getString("spellbookName", "&bSpellbook"));
        this.spellpageNameFormat = ChatColor.translateAlternateColorCodes('&',
                config.getString("spellpageNameFormat", "&5&ka&r&bSpell Page&5&ka&r {NAME}"));
        this.manaPerHalfHeart = config.getDouble("manaPerHalfHeart", 1);

        this.mySQLUsername = config.getString("Mysql.username", "user");
        this.mySQLPassword = config.getString("Mysql.password", "pass");
        this.mySQLDatabase = config.getString("Mysql.database", "civspellapi");
        this.mySQLHostname = config.getString("Mysql.hostname", "localhost");
        this.mySQLPort = config.getInt("Mysql.port", 3306);

        ConfigurationSection spellsSection = config.getConfigurationSection("spells");
        if (spellsSection != null) {
            for (String playerSpellName : spellsSection.getKeys(false)) {
                ConfigurationSection individualSpellSection = spellsSection.getConfigurationSection(playerSpellName);
                Spell spell = new Spell();

                if (individualSpellSection == null) {
                    continue;
                }

                spell.playerVisibleName = playerSpellName;
                spell.rawSpellToCast = individualSpellSection.getString("spell", "nop");
                spell.manaCost = individualSpellSection.getDouble("manaCost", 0);
                spell.isScrollCastable = individualSpellSection.getBoolean("scrollCastable", false);
                spell.isSpellbookCastable = individualSpellSection.getBoolean("spellbookCastable", false);
                spell.isMemoryCastable = individualSpellSection.getBoolean("memoryCastable", false);
                spell.config = individualSpellSection.getConfigurationSection("config");

                spells.put(playerSpellName, spell);
            }
        }
    }

    private String spellNameFormat;
    private boolean memorizationEnabled;
    private boolean spellbookEnabled;
    private String spellbookName;
    private String spellpageNameFormat;
    private double manaPerHalfHeart;

    private String mySQLUsername;
    private String mySQLPassword;
    private String mySQLDatabase;
    private String mySQLHostname;
    private int mySQLPort;

    private HashMap<String, Spell> spells = new HashMap<>();

    public String getSpellNameFormat() {
        return spellNameFormat;
    }

    public boolean isMemorizationEnabled() {
        return memorizationEnabled;
    }

    public boolean isSpellbookEnabled() {
        return spellbookEnabled;
    }

    public String getSpellbookName() {
        return spellbookName;
    }

    public String getSpellpageNameFormat() {
        return spellpageNameFormat;
    }

    public double getManaPerHalfHeart() {
        return manaPerHalfHeart;
    }

    public String getMySQLUsername() {
        return mySQLUsername;
    }

    public String getMySQLPassword() {
        return mySQLPassword;
    }

    public String getMySQLDatabase() {
        return mySQLDatabase;
    }

    public String getMySQLHostname() {
        return mySQLHostname;
    }

    public int getMySQLPort() {
        return mySQLPort;
    }

    public Spell getSpell(String playerVisibleSpellName) {
        return spells.get(playerVisibleSpellName);
    }

    public class Spell {
        public String playerVisibleName;
        public String rawSpellToCast;
        public double manaCost;
        public boolean isScrollCastable;
        public boolean isSpellbookCastable;
        public boolean isMemoryCastable;
        public ConfigurationSection config;
    }
}
