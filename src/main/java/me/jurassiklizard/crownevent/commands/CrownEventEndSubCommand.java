package me.jurassiklizard.crownevent.commands;

import me.jurassiklizard.crownevent.CrownEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class CrownEventEndSubCommand implements CommandExecutor {
    public static String Label = "stop";
    private CrownEvent plugin;

    public CrownEventEndSubCommand(CrownEvent plugin){this.plugin = plugin;}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("Stop!");
        plugin.getCrownManager().stop();
        return true;
    }
}
