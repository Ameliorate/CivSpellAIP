package pw.amel.civspell;

import pw.amel.civspell.builtin.*;
import pw.amel.civspell.commands.GiveSpellItem;
import pw.amel.civspell.commands.ReloadCommand;
import pw.amel.civspell.gameplay.SpellCastListener;
import org.bukkit.plugin.java.JavaPlugin;

public class CivSpells extends JavaPlugin {
    public SpellConfig config;

    @Override
    public void onEnable() {
        addEffect("nop", NopEffect.class);
        addEffect("throwpot", ThrowPotionEffect.class);
        addEffect("sound", SoundEffect.class);
        addEffect("removetriggeritem", RemoveTriggerItemEffect.class);
        addEffect("fail", FailEffect.class);

        saveDefaultConfig();
        this.config = new SpellConfig(getConfig(), this);

        getCommand("csgiveitem").setExecutor(new GiveSpellItem(this));
        getCommand("csreload").setExecutor(new ReloadCommand(this));

        getServer().getPluginManager().registerEvents(new SpellCastListener(this), this);
    }

    /**
     * Adds an effect with the given name.
     * @param name The name of the effect that the user will refer to in their configuration.
     * @param effect The effect. It must implement Effect and have a public constructor that takes a
     *               ConfigurationSection as its only argument.
     */
    public void addEffect(String name, Class<?> effect) {
        EffectManager.addEffect(name, effect);
    }

}
