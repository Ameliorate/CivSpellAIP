package pw.amel.civspell.commands;


import pw.amel.civspell.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;

public class GiveSpellpageCommand implements CommandExecutor {
    public GiveSpellpageCommand(Main mainPlugin) {
        this.mainPlugin = mainPlugin;
    }

    private Main mainPlugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("civ-spell-api.give.page")) {
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
            sender.sendMessage("Invalid number: " + e.getMessage());
            return false;
        }
        ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.remove(0); // Lazy way to remove first 2 elements.
        argsList.remove(0);
        String spellName = argsList.stream().reduce((a, b) -> a + b).orElse("");
        Player player = mainPlugin.getServer().getPlayer(playerName);
        ItemStack page = new ItemStack(Material.PAPER, number);
        ItemMeta meta = page.getItemMeta();
        meta.setDisplayName(mainPlugin.config.getSpellpageNameFormat().replaceFirst("\\{NAME}", spellName));
        page.setItemMeta(meta);
        player.getInventory().addItem(page);
        return true;
    }
}
