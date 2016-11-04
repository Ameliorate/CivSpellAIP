package org.ame.civspell.gameplay;

import com.mysql.jdbc.Statement;
import org.ame.civspell.*;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!mainPlugin.config.isMemorizationEnabled()) {
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

            boolean result = CastHelper.castSpell(selectedSpellName, event.getPlayer(), SpellCastMethod.MEMORIZED,
                    event.getHand() == EquipmentSlot.HAND, event.getClickedBlock(), event.getAction(),
                    event.getBlockFace(), mainPlugin);
            if (result) {
                event.setCancelled(true);
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
