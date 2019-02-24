package pw.amel.civspell;

import pw.amel.civspell.builtin.*;
import pw.amel.civspell.commands.GiveSpellItem;
import pw.amel.civspell.commands.ReloadCommand;
import pw.amel.civspell.gameplay.SpellCastListener;
import vg.civcraft.mc.civmodcore.ACivMod;

public class CivSpells extends ACivMod {
    public SpellConfig config;

    @Override
    public void onEnable() {
        // Meta effects: Effects that change how the spell executes

        addEffect("nop", NopEffect.class);
        addEffect("fail", FailEffect.class);
        addEffect("metaspell", MetaSepllEffect.class);
        addEffect("cooldown", CooldownEffect.class);

        // Basic effect effects: Effects that cause a simple change in the game, like playing a sound or
        //     giving the caster a potion effect.

        addEffect("throwpot", ThrowPotionEffect.class);
        addEffect("sound", SoundEffect.class);
        addEffect("removetriggeritem", RemoveTriggerItemEffect.class);

        // Spell type effects: Effects that add new kinds of spells, like scrolls and spell stones.

        addEffect("manaspell", ManaSpellEffect.class);
        addEffect("scroll", RemoveTriggerItemEffect.class);

        saveDefaultConfig();
        this.config = new SpellConfig(getConfig(), this);

        getCommand("csgiveitem").setExecutor(new GiveSpellItem(this));
        getCommand("csreload").setExecutor(new ReloadCommand(this));

        getServer().getPluginManager().registerEvents(new SpellCastListener(this), this);
    }

    @Override
    protected String getPluginName() {
        return "CivSpellAPI";
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
