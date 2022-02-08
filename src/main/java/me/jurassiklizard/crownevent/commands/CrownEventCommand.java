package me.jurassiklizard.crownevent.commands;

import me.jurassiklizard.crownevent.CrownEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CrownEventCommand extends CommandBase<CrownEvent>{
    public static String Label = "kingcrown";

    public CrownEventCommand(CrownEvent plugin) {
        super(plugin);
    }

    @Override
    public boolean runCommand(CommandSender sender, Command rootCommand, String label, String[] args) {
        sender.sendMessage("Include and argument value!");
        return true;
    }
}
