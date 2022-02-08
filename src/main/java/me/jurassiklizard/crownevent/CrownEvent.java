package me.jurassiklizard.crownevent;

import com.tchristofferson.configupdater.ConfigUpdater;
import me.jurassiklizard.crownevent.commands.CrownEventCommand;
import me.jurassiklizard.crownevent.commands.CrownEventEndSubCommand;
import me.jurassiklizard.crownevent.commands.CrownEventStartSubCommand;
import me.jurassiklizard.crownevent.crown.CrownManager;
import me.jurassiklizard.crownevent.events.CrownEventsListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public final class CrownEvent extends JavaPlugin {
    private CrownManager crownManager;
    private World world;

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("Starting up...");
        world = Bukkit.getServer().getWorlds().get(0);
        registerConfigs();
        registerCommands();
        registerListeners();
        setupManagers();
//        persistentTimerManager.readPersistentTimers();
        //persistentTimerManager.createTeleportationCrownStatePersistentTimer(180L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        crownManager.stop();
        System.out.println("Shutting down...");
    }

    private void registerConfigs(){
        saveDefaultConfig();
        //The config needs to exist before using the updater
//        File configFile = new File(getDataFolder(), "config.yml");
//
//        try {
//            ConfigUpdater.update(this, "config.yml", configFile, Collections.emptyList());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        reloadConfig();
    }

    private void registerCommands(){
        CrownEventCommand crownEventCommand = new CrownEventCommand(this);
        crownEventCommand.registerSubCommand(CrownEventStartSubCommand.Label, new CrownEventStartSubCommand(this));
        crownEventCommand.registerSubCommand(CrownEventEndSubCommand.Label, new CrownEventEndSubCommand(this));
        this.getCommand(CrownEventCommand.Label).setExecutor(crownEventCommand);
    }

    private void registerListeners(){
        getServer().getPluginManager().registerEvents(new CrownEventsListener(this), this);
    }

    private void setupManagers(){
        crownManager = new CrownManager(this, world);
        //persistentTimerManager = new PersistentTimerManager(this, world);
    }

    public CrownManager getCrownManager() {
        return crownManager;
    }

//    public PersistentTimerManager getPersistentTimerManager() {
//        return persistentTimerManager;
//    }
}
