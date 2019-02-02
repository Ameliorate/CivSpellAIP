package pw.amel.civspell.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pw.amel.civspell.CivSpells;
import pw.amel.civspell.SpellConfig;

import java.util.ArrayList;
import java.util.Arrays;

public class GiveSpellItem implements CommandExecutor {
    public GiveSpellItem(CivSpells main) {
        this.main = main;
    }

    private CivSpells main;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("civ-spell-api.give")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }
        if (args.length < 3) {
            return false;
        }
        String playerName = args[0];
        int number;
        try {
            number = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid number: " + e.getMessage());
            return false;
        }

        ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.remove(0); // Lazy way to remove first 2 elements.
        argsList.remove(0);

        String spellName = argsList.stream().reduce((a, b) -> a + " " + b).orElse("");

        SpellConfig.SpellData spellData = main.config.spells.get(spellName);
        if (spellData == null) {
            sender.sendMessage(ChatColor.RED + "That spell does not exist.");
            return true;
        }

        ItemStack item = spellData.triggerItem.clone();
        item.setAmount(number);
        Player player = main.getServer().getPlayer(playerName);
        player.getInventory().addItem(item);

        return true;
    }
}
