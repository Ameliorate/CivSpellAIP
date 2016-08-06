package org.ame.civspell;


import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

class CommandGiveScroll implements CommandExecutor {
    CommandGiveScroll(Main mainPlugin, String formatString) {
        this.mainPlugin = mainPlugin;
        this.formatString = formatString;
    }

    private Main mainPlugin;
    private String formatString;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args.length > 3) {
            sender.sendMessage("/" + label + " [player] [spell] {number}");
            return false;
        }
        String playerName = args[0];
        String spellName = args[1];
        int number = 1;
        if (args.length == 3) {
            try {
                number = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid number: " + e.getMessage());
                return false;
            }
        }
        Player player = mainPlugin.getServer().getPlayer(playerName);
        ItemStack scroll = new ItemStack(Material.SUGAR_CANE, number);
        ItemMeta meta = scroll.getItemMeta();
        meta.setDisplayName(formatString.replaceFirst("\\{NAME}", spellName));
        scroll.setItemMeta(meta);
        player.getInventory().addItem(scroll);
        return true;
    }
}
