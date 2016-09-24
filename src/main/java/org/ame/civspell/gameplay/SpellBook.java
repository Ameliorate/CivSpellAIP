package org.ame.civspell.gameplay;


import com.mysql.jdbc.Statement;
import org.ame.civspell.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class SpellBook implements Listener {
    public SpellBook(Main mainPlugin) {
        this.mainPlugin = mainPlugin;

        mainPlugin.database.execute("CREATE TABLE IF NOT EXISTS spellbooks (" +
                "book_id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT," +
                "slot_selected INTEGER" +
                ")");
        mainPlugin.database.execute("CREATE TABLE IF NOT EXISTS spells (" +
                "book_id INTEGER," +
                "slot INTEGER," +
                "name TEXT" +
                ")");
    }

    private Main mainPlugin;
    private static HashMap<Player, Boolean> isOnCooldown = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!mainPlugin.getConfig().getBoolean("Spellbook_Enabled")) {
            return;
        } else if (isOnCooldown.get(event.getPlayer()) != null && isOnCooldown.get(event.getPlayer())) {
            event.setCancelled(true);
            return;
        } else if (event.getAction() == Action.PHYSICAL) {
            return;
        }
        // Empty hand.
        else if (event.getItem() == null) {
            return;
        } else if (!event.getItem().hasItemMeta()) {
            return;
        }
        // Check for default name.
        else if (event.getItem().getItemMeta().getDisplayName() == null) {
            return;
        }
        // Check if a spellbook.
        else if (!event.getItem().getItemMeta().getDisplayName().equals(mainPlugin.getConfig().getString("Spellbook_Name"))) {
            return;
        } else if (event.getMaterial() != Material.ENCHANTED_BOOK) {
            return;
        }

        String spellBookIDString = null;
        if (event.getItem().getItemMeta().hasLore()) {
            for (String lore : event.getItem().getItemMeta().getLore()) {
                if (FormatEncoder.isStringEncoded(lore)) {
                    String maybeUUID = new FormatEncoder(lore, true).getDecoded();
                    if (maybeUUID.matches("ID:\\d+")) {
                        spellBookIDString = maybeUUID.replaceFirst("ID:", "");
                    }
                }
            }
        }
        int spellBookID;
        if (spellBookIDString == null) {
            try {
                PreparedStatement preparedStatement = mainPlugin.database.getConnection()
                        .prepareStatement("INSERT INTO spellbooks (slot_selected) VALUES (0);",
                        Statement.RETURN_GENERATED_KEYS);
                preparedStatement.executeUpdate();
                ResultSet tableKeys = preparedStatement.getGeneratedKeys();
                tableKeys.next();
                spellBookID = tableKeys.getInt(1);
            } catch (SQLException e) {
                mainPlugin.getLogger().log(Level.SEVERE, "Could not execute SQL statement!", e);
                return;
            }

            if (!event.getItem().getItemMeta().hasLore()) {
                ArrayList<String> lore = new ArrayList<>();
                lore.add(new FormatEncoder("ID:" + Integer.toString(spellBookID), false).getEncoded());
                ItemMeta meta = event.getItem().getItemMeta();
                meta.setLore(lore);
                event.getItem().setItemMeta(meta);
            } else {
                boolean set = false;
                int i = 0;
                for (String lore : event.getItem().getItemMeta().getLore()) {
                    if (!set && !FormatEncoder.isStringEncoded(lore)) {
                        set = true;
                        List<String> loreList = event.getItem().getItemMeta().getLore();
                        loreList.set(i, new FormatEncoder("ID:" + Integer.toString(spellBookID), false).getEncoded() +
                            lore);
                        event.getItem().getItemMeta().setLore(loreList);
                    }
                    i++;
                }
                if (!set) {
                    List<String> loreList = event.getItem().getItemMeta().getLore();
                    loreList.add(new FormatEncoder("ID:" + Integer.toString(spellBookID), false).getEncoded());
                    event.getItem().getItemMeta().setLore(loreList);
                }
            }
        } else {
            spellBookID = Integer.parseInt(spellBookIDString);
        }

        if (event.getPlayer().isSneaking()) {
            ArrayList<String> helpText = new ArrayList<>();
            helpText.add("To select a spell, click on it.");
            helpText.add("To put a spell into the book, move it into the interface as if it was an item in a chest.");
            helpText.add("In order to remove the spell after putting it in, shift click on it.");
            helpText.add("To cast a spell after selecting it, left or right click with the spellbook.");
            new SpellbookGUI("Spellbook " + spellBookID, 54, spellBookID, event.getPlayer(), mainPlugin,
                    true, true, helpText);
        } else {
            String selectedSpellName = null;
            try {
                PreparedStatement slotStmt = mainPlugin.database
                        .prepareStatement("SELECT slot_selected FROM spellbooks WHERE book_id=?");
                slotStmt.setInt(1, spellBookID);
                ResultSet slotResult = slotStmt.executeQuery();
                slotResult.next();
                int selectedSlot = slotResult.getInt("slot_selected");

                PreparedStatement spellStmt = mainPlugin.database
                        .prepareStatement("SELECT name FROM spells WHERE book_id=? AND slot=?");
                spellStmt.setInt(1, spellBookID);
                spellStmt.setInt(2, selectedSlot);
                ResultSet spellResult = spellStmt.executeQuery();
                while (spellResult.next()) {
                    selectedSpellName = spellResult.getString("name");
                }
                if (selectedSpellName == null) {
                    selectedSpellName = "nop";
                }
            } catch (SQLException e) {
                mainPlugin.getLogger().log(Level.SEVERE, "Failed to set spell to cast.", e);
                return;
            }

            Spell toCast = SpellManager.spellMap.get(selectedSpellName);
            if (toCast == null) {
                event.getPlayer().sendMessage("§cThat's not a valid spell!");
                try {
                    PreparedStatement stmt = mainPlugin.database
                            .prepareStatement("DELETE FROM spells WHERE name=?");
                    stmt.setString(1, selectedSpellName);
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    mainPlugin.getLogger().log(Level.SEVERE, "Failed to remove invalid spell.", e);
                }
                return;
            }

            isOnCooldown.put(event.getPlayer(), true);
            mainPlugin.getServer().getScheduler()
                    .runTaskLater(mainPlugin, () -> isOnCooldown.put(event.getPlayer(), false), 5);

            int manaUsage = toCast.manaUsage();
            int playerMana = event.getPlayer().getLevel();

            boolean shouldReduceMana = toCast.cast(event.getClickedBlock(), event.getPlayer(), event.getAction(),
                    event.getBlockFace(), event.getItem(), SpellCastMethod.SPELL_BOOK);
            if (shouldReduceMana) {
                boolean enoughMana = ManaHelper.subtractXp(event.getPlayer(), manaUsage);
                if (!enoughMana) {
                    int remainder = Math.abs(playerMana - manaUsage);
                    int healthMultiplier = mainPlugin.getConfig().getInt("Mana_Per_Half_Heart");
                    float healthToTake = (float) remainder / healthMultiplier;
                    event.getPlayer().damage(healthToTake);
                    event.getPlayer().setNoDamageTicks(0);
                    event.getPlayer().setLevel(0);
                    event.getPlayer().setExp(0);
                }
            }
        }
    }
}
