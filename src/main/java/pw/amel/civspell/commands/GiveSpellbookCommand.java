package pw.amel.civspell.commands;


import pw.amel.civspell.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GiveSpellbookCommand implements CommandExecutor {
    public GiveSpellbookCommand(Main mainPlugin) {
        this.mainPlugin = mainPlugin;
    }

    private Main mainPlugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("civ-spell-api.give.book")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }
        if (args.length != 1) {
            return false;
        }
        String playerName = args[0];
        Player player = mainPlugin.getServer().getPlayer(playerName);
        ItemStack book = new ItemStack(mainPlugin.config.getSpellbookMaterial(), 1);
        ItemMeta meta = book.getItemMeta();
        meta.setDisplayName(mainPlugin.config.getSpellbookName());
        book.setItemMeta(meta);
        player.getInventory().addItem(book);
        return true;
    }
}
