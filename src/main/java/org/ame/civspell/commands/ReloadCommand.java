package org.ame.civspell.commands;

import org.ame.civspell.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Reloads the config from disk.
 */
public class ReloadCommand implements CommandExecutor {
    public ReloadCommand(Main mainPlugin) {
        this.mainPlugin = mainPlugin;
    }

    private Main mainPlugin;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!commandSender.hasPermission("civ-spell-api.reload")) {
            commandSender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }

        mainPlugin.reloadConfig();
        mainPlugin.config.reloadConfig(mainPlugin.getConfig());
        commandSender.sendMessage("Reloaded config of CivSpellAPI");

        return true;
    }
}
