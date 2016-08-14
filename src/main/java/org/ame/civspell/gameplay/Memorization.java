package org.ame.civspell.gameplay;

import com.mysql.jdbc.Statement;
import org.ame.civspell.Main;
import org.ame.civspell.Spell;
import org.ame.civspell.SpellCastMethod;
import org.ame.civspell.SpellManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class Memorization implements Listener {
    public Memorization(Main mainPlugin) {
        this.mainPlugin = mainPlugin;

        mainPlugin.database.execute("CREATE TABLE IF NOT EXISTS players (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "mem_book_id INTEGER" +
                ")");
    }

    private Main mainPlugin;
    private static HashMap<Player, Boolean> isOnCooldown = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!mainPlugin.getConfig().getBoolean("Memorization_Enabled")) {
            return;
        } else if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) {
            return;
        } else if (event.getAction() == Action.PHYSICAL) {
            return;
        } else if (!event.getPlayer().isSneaking()) {
            return;
        }

        int memBookID = -1;

        try {
            PreparedStatement preparedStatement = mainPlugin.database.getConnection()
                    .prepareStatement("SELECT mem_book_id FROM players WHERE uuid = ?");
            preparedStatement.setString(1, event.getPlayer().getUniqueId().toString());
            ResultSet result = preparedStatement.executeQuery();

            while (result.next()) {
                memBookID = result.getInt("mem_book_id");
            }
        } catch (SQLException e) {
            mainPlugin.getLogger().log(Level.SEVERE, "Could not execute SQL statement!", e);
            return;
        }

        if (memBookID == -1) {
            try {
                PreparedStatement preparedStatement = mainPlugin.database.getConnection()
                        .prepareStatement("INSERT INTO spellbooks (slot_selected) VALUES (0);",
                                Statement.RETURN_GENERATED_KEYS);
                preparedStatement.executeUpdate();
                ResultSet result = preparedStatement.getGeneratedKeys();

                while (result.next()) {
                    memBookID = result.getInt(1);
                }
            } catch (SQLException e) {
                mainPlugin.getLogger().log(Level.SEVERE, "Could not execute SQL statement!", e);
                return;
            }

            try {
                PreparedStatement preparedStatement = mainPlugin.database.getConnection()
                        .prepareStatement("INSERT INTO players (uuid, mem_book_id) VALUES (?, ?)");
                preparedStatement.setString(1, event.getPlayer().getUniqueId().toString());
                preparedStatement.setInt(2, memBookID);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                mainPlugin.getLogger().log(Level.SEVERE, "Could not execute SQL statement!", e);
                return;
            }
        }

        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // cast spell
            String selectedSpellName = null;
            try {
                PreparedStatement slotStmt = mainPlugin.database
                        .prepareStatement("SELECT slot_selected FROM spellbooks WHERE book_id=?");
                slotStmt.setInt(1, memBookID);
                ResultSet slotResult = slotStmt.executeQuery();
                slotResult.next();
                int selectedSlot = slotResult.getInt("slot_selected");

                PreparedStatement spellStmt = mainPlugin.database
                        .prepareStatement("SELECT name FROM spells WHERE book_id=? AND slot=?");
                spellStmt.setInt(1, memBookID);
                spellStmt.setInt(2, selectedSlot);
                ResultSet spellResult = spellStmt.executeQuery();
                while (spellResult.next()) {
                    selectedSpellName = spellResult.getString("name");
                }
                if (selectedSpellName == null) {
                    return;
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
                if (playerMana < manaUsage) {
                    // Not enough mana
                    int remainder = Math.abs(playerMana - manaUsage);
                    int healthMultiplier = mainPlugin.getConfig().getInt("Mana_Per_Half_Heart");
                    float healthToTake = (float) remainder / healthMultiplier;
                    event.getPlayer().damage(healthToTake);
                    event.getPlayer().setNoDamageTicks(0);
                    event.getPlayer().setLevel(0);
                } else {
                    event.getPlayer().setLevel(event.getPlayer().getLevel() - manaUsage);
                }
            }
        } else {
            ArrayList<String> helpText = new ArrayList<>();
            helpText.add("To select a spell, click on it.");
            helpText.add("To cast a spell after selecting it, shift left click with an empty hand.");
            new SpellbookGUI(event.getPlayer().getPlayerListName() + "'s Memorized Spells",
                    54, memBookID, event.getPlayer(), mainPlugin, false, false, helpText);
        }
    }
}
