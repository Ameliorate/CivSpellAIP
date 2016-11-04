package org.ame.civspell.gameplay;

import org.ame.civspell.Main;
import org.ame.civspell.SpellManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

class SpellbookGUI implements Listener {
    SpellbookGUI(String name, int size, int bookID, Player displayTo, Main mainPlugin,
                 boolean allowRemoval, boolean allowInserton, List<String> helpText) {
        this.bookID = bookID;
        this.name = name;
        this.size = size;
        this.guiInventory = Bukkit.createInventory(null, size, name);
        this.mainPlugin = mainPlugin;

        ItemStack close = new ItemStack(Material.BARRIER, 1);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("§7Close");
        close.setItemMeta(closeMeta);

        ItemStack help = new ItemStack(Material.BOOK_AND_QUILL, 1);
        ItemMeta helpMeta = help.getItemMeta();
        helpMeta.setDisplayName("§5§o" + helpText.get(0));
        helpText.remove(0);
        helpMeta.setLore(helpText);
        help.setItemMeta(helpMeta);

        guiInventory.setItem(size - 1, close);
        guiInventory.setItem(size - 2, help);

        HashMap<Integer, String> spellMap = new HashMap<>();
        PreparedStatement statement = mainPlugin.database
                .prepareStatement("SELECT name, slot FROM spells WHERE book_id=?");
        try {
            statement.setInt(1, bookID);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                String spellName = result.getString("name");
                int spellSlot = result.getInt("slot");
                spellMap.put(spellSlot, spellName);
            }
        } catch (SQLException e) {
            mainPlugin.getLogger().log(Level.SEVERE, "Failed to set spellbook pages.", e);
            return;
        }

        for (Integer slot : spellMap.keySet()) {
            if (!SpellManager.spellMap.containsKey(spellMap.get(slot))) {
                mainPlugin.getLogger().log(Level.WARNING, "Invalid spell in database: " + spellMap.get(slot));
                continue;
            }

            ItemStack spellIcon = new ItemStack(Material.PAPER, 1);
            ItemMeta spellIconMeta = spellIcon.getItemMeta();
            spellIconMeta.setDisplayName("§3" + spellMap.get(slot));
            ArrayList<String> spellIconLore = new ArrayList<>();
            spellIconLore.add("§7Mana to cast: " + SpellManager.spellMap.get(spellMap.get(slot)).manaUsage());
            spellIconMeta.setLore(spellIconLore);
            spellIcon.setItemMeta(spellIconMeta);

            guiInventory.setItem(slot, spellIcon);
        }

        displayTo.openInventory(guiInventory);

        mainPlugin.getServer().getPluginManager().registerEvents(this, mainPlugin);
    }

    private String name;
    private int size;
    private int bookID;
    private Inventory guiInventory;
    private Main mainPlugin;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
            event.setCancelled(true);
        }

        if (event.getClickedInventory() == null) {
            return;
        } else if (event.getClickedInventory().getType() != InventoryType.CHEST ||
                !event.getClickedInventory().getName().equals(name)) {
            return;
        }

        event.setCancelled(true);

        if (event.getSlot() == size - 1) {
            mainPlugin.getServer().getScheduler().runTaskLater(mainPlugin,
                    () -> event.getWhoClicked().closeInventory(), 1);
            event.getWhoClicked().getLocation().getWorld()
                    .playEffect(event.getWhoClicked().getLocation(), Effect.CLICK2, null);
            return;
        }

        if (event.getClick() != ClickType.SHIFT_LEFT &&
                event.getClick() != ClickType.SHIFT_RIGHT &&
                (event.getCursor() == null || event.getCursor().getType() == Material.AIR)) {
            event.getWhoClicked().getLocation().getWorld()
                    .playEffect(event.getWhoClicked().getLocation(), Effect.CLICK2, null);
        }

        String[] pageNameSplit = mainPlugin.config.getSpellpageNameFormat().split("\\{NAME}");
        String pageNamePrefix = pageNameSplit[0];
        String pageNameSuffix = "";
        if (pageNameSplit.length == 2) {
            pageNameSuffix = pageNameSplit[1];
        }

        if ((event.getClick() != ClickType.SHIFT_LEFT || event.getClick() != ClickType.SHIFT_RIGHT) &&
                (event.getSlot() < size - 2) &&
                (event.getCursor() == null || event.getCursor().getType() == Material.AIR)
                ) {
            PreparedStatement statement = mainPlugin.database
                    .prepareStatement("UPDATE spellbooks SET slot_selected=? WHERE book_id=?");
            try {
                statement.setInt(1, event.getSlot());
                statement.setInt(2, bookID);
                statement.executeUpdate();
            } catch (SQLException e) {
                mainPlugin.getLogger().log(Level.SEVERE, "Failed to set spellbook slot.", e);
            }
        } else if (event.getSlot() < size - 2 &&
                event.getCursor().getType() == Material.PAPER &&
                event.getCursor().hasItemMeta() &&
                event.getCursor().getItemMeta().hasDisplayName() &&
                event.getCursor().getItemMeta().getDisplayName().startsWith(pageNamePrefix) &&
                event.getCursor().getItemMeta().getDisplayName().endsWith(pageNameSuffix)
                ) {
            String spellName = event.getCursor().getItemMeta().getDisplayName()
                    .replaceFirst(Pattern.quote(pageNamePrefix), "")
                    .replaceAll(Pattern.quote(pageNameSuffix) + "$", "");

            if (SpellManager.spellMap.get(spellName) == null) {
                event.getWhoClicked().setItemOnCursor(null);
                event.getWhoClicked().sendMessage(ChatColor.RED + "That itemstack contained an invalid spell!");
            }

            if (event.getCursor().getAmount() == 1) {
                event.getWhoClicked().setItemOnCursor(null);
            } else {
                event.getCursor().setAmount(event.getCursor().getAmount() - 1);
            }

            ItemStack spellIcon = new ItemStack(Material.PAPER, 1);
            ItemMeta spellIconMeta = spellIcon.getItemMeta();
            spellIconMeta.setDisplayName("§3" + spellName);
            ArrayList<String> spellIconLore = new ArrayList<>();
            spellIconLore.add("§7Mana to cast: " + SpellManager.spellMap.get(spellName).manaUsage());
            spellIconMeta.setLore(spellIconLore);
            spellIcon.setItemMeta(spellIconMeta);
            guiInventory.setItem(event.getSlot(), spellIcon);

            try {
                PreparedStatement statement = mainPlugin.database
                        .prepareStatement("INSERT INTO spells (book_id, slot, name) VALUES (?, ?, ?)");
                statement.setInt(1, bookID);
                statement.setInt(2, event.getSlot());
                statement.setString(3, spellName);
                statement.executeUpdate();
            } catch (SQLException e) {
                mainPlugin.getLogger().log(Level.SEVERE, "Failed to insert spell page.", e);
            }
        }

        if ((event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) &&
                event.getSlot() < size - 2
                ) {
            String spellName = null;

            try {
                PreparedStatement statement = mainPlugin.database
                        .prepareStatement("SELECT name FROM spells WHERE book_id=? AND slot=?");
                statement.setInt(1, bookID);
                statement.setInt(2, event.getSlot());
                ResultSet result = statement.executeQuery();

                while (result.next()) {
                    spellName = result.getString("name");
                }
            } catch (SQLException e) {
                mainPlugin.getLogger().log(Level.SEVERE, "Failed to check if spell was in slot.", e);
            }

            if (spellName != null) {
                guiInventory.setItem(event.getSlot(), null);

                try {
                    PreparedStatement statement = mainPlugin.database
                            .prepareStatement("DELETE FROM spells WHERE book_id=? AND slot=?");
                    statement.setInt(1, bookID);
                    statement.setInt(2, event.getSlot());
                    statement.executeUpdate();
                } catch (SQLException e) {
                    mainPlugin.getLogger().log(Level.SEVERE, "Failed to remove spell from slot.", e);
                }

                ItemStack spellPage = new ItemStack(Material.PAPER, 1);
                ItemMeta spellPageMeta = spellPage.getItemMeta();
                spellPageMeta.setDisplayName(mainPlugin.config.getSpellpageNameFormat()
                        .replaceFirst("\\{NAME}", spellName));
                spellPage.setItemMeta(spellPageMeta);

                event.getWhoClicked().getInventory().addItem(spellPage);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getName().equals(name)) {
            HandlerList.unregisterAll(this);
        }
    }
}
