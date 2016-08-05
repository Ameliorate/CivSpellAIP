package org.ame.civspell;


import java.util.HashMap;

public class SpellManager {
    static HashMap<String, Spell> spellMap = new HashMap<>();

    public static void addSpell(String name, Spell spell) {
        spellMap.put(name, spell);
    }
}
