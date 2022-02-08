package me.jurassiklizard.crownevent.commands;

import me.jurassiklizard.crownevent.CrownEvent;
import me.jurassiklizard.crownevent.enums.CrownState;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CrownEventStartSubCommand implements CommandExecutor {
    public static String Label = "start";
    private CrownEvent plugin;

    public CrownEventStartSubCommand(CrownEvent plugin){this.plugin = plugin;}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("Start!");
        plugin.getCrownManager().start();
        return true;
    }
}
