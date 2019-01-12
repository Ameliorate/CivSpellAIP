package pw.amel.civspell;

import org.bukkit.configuration.ConfigurationSection;
import pw.amel.civspell.spell.Effect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class EffectManager {
    private static HashMap<String, Class<?>> effectMap = new HashMap<>();

    /**
     * Adds an effect with the given name.
     * @param name The name of the effect that the user will refer to in their configuration.
     * @param effect The effect. It must implement Effect and have a public constructor that takes a
     *               ConfigurationSection as its only argument.
     */
    public static void addEffect(String name, Class<?> effect) {
        if (!Effect.class.isAssignableFrom(effect)) {
            throw new IllegalArgumentException("Effect " + effect + " does not implement interface Effect.");
        }

        effectMap.put(name, effect);
    }

    /**
     * Gets the given effect with the configuration.
     * @param name The name of the effect.
     * @param config The ConfigurationSection that will be passed to the effect.
     * @return The effect or null if the effect with the name is not found.
     */
    public static Effect constructEffect(String name, ConfigurationSection config) {
        Class<?> effectClass = effectMap.get(name);
        if (effectClass == null) {
            return null;
        }

        try {
            Constructor<?> constructor = effectClass.getConstructor(ConfigurationSection.class);
            return (Effect) constructor.newInstance(config);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError(e);
        }
    }
}
