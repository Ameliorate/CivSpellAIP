package org.ame.civspell;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        System.out.println("CivSpellAPI Enabled.");
    }

    @Override
    public void onDisable() {
        System.out.println("CivSpellAPI Disabled.");
    }

}
