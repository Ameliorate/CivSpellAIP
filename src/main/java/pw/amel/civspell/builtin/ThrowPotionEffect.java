package pw.amel.civspell.builtin;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.LingeringPotion;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.*;
import pw.amel.civspell.spell.CastData;
import pw.amel.civspell.spell.Effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ThrowPotionEffect implements Effect {
    public ThrowPotionEffect(ConfigurationSection config) {
        color = config.getColor("color");
        velocityMultiplier = (float) config.getDouble("velocityMultiplier", 1.0);
        isLingering = config.getBoolean("isLingering", false);

        List<ConfigurationSection> effects = getConfigList(config, "effects");
        ArrayList<PotionEffect> vanillaEffects = new ArrayList<>();

        for (ConfigurationSection effect : Objects.requireNonNull(effects)) {
            PotionEffectType type = PotionEffectType.getByName(effect.getString("type"));
            int durationSeconds = effect.getInt("durationSeconds");
            int duration = durationSeconds * 20;
            int level = effect.getInt("level");
            int amplifier = level - 1;
            vanillaEffects.add(new PotionEffect(type, duration, amplifier));
        }

        this.effects = vanillaEffects;
    }

    private Color color;
    private float velocityMultiplier;
    private boolean isLingering;
    private ArrayList<PotionEffect> effects;

    @Override
    public void cast(CastData castData) {
        // Construct the potion with the effects to be thrown
        ItemStack potionItem = new ItemStack(isLingering ? Material.LINGERING_POTION : Material.SPLASH_POTION);
        PotionMeta potionMeta = (PotionMeta) potionItem.getItemMeta();

        potionMeta.setBasePotionData(new PotionData(PotionType.MUNDANE));

        for (PotionEffect effect : effects) {
            potionMeta.addCustomEffect(effect, true);
        }

        if (color != null) {
            potionMeta.setColor(color);
        }

        potionItem.setItemMeta(potionMeta);

        // Spawn/throw the potion
        ThrownPotion thrownPotion = castData.player.launchProjectile(isLingering ? LingeringPotion.class : ThrownPotion.class);
        thrownPotion.setItem(potionItem);
        thrownPotion.setVelocity(thrownPotion.getVelocity().multiply(velocityMultiplier));

        castData.addReturnHook(() -> thrownPotion.remove());
    }

    @SuppressWarnings("unchecked") // fix your warnings, java
    private List<ConfigurationSection> getConfigList(ConfigurationSection config, String path)
    {
        if (!config.isList(path)) return null;

        List<ConfigurationSection> list = new ArrayList<>();

        for (Object object : config.getList(path)) {
            if (object instanceof Map) {
                MemoryConfiguration mc = new MemoryConfiguration();

                mc.addDefaults((Map<String, Object>) object);

                list.add(mc);
            }
        }

        return list;
    }

}
