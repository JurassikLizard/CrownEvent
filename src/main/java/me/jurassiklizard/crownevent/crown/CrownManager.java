package me.jurassiklizard.crownevent.crown;

import me.jurassiklizard.crownevent.CrownEvent;
import me.jurassiklizard.crownevent.enums.CrownState;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class CrownManager {
    private CrownEvent plugin;
    private CrownState crownState = CrownState.DESPAWNED;
    private World world;
    private ItemStack crownItem;
    private Entity crownEntity = null;
    private Integer teleportTimerId = null;
    private Integer playerLocationCacheTimerId = null;
    private Integer voidCheckTimerId = null;
    private Integer rewardsTimerId = null;
    private Integer locationPingTimerId = null;
    private Location cachedLocation = null;
    private boolean isRunning = false;

    public CrownManager(CrownEvent plugin, World world){
        this.plugin = plugin;
        this.world = world;
        crownItem = createCrownItem();
    }

    public void start(){
        isRunning = true;
        // Essentially just spawns it in, and doesn't give Item or Player value so that a new crown is created
        setCrownState(CrownState.PODIUM, null, null, true);

        voidCheckTimerId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if(crownEntity == null) return;
                int yHeight = plugin.getConfig().getInt("crown-item.void-y-height");
                if(crownEntity.getType() == EntityType.DROPPED_ITEM){
                    Item crownItem = (Item) crownEntity;
                    if(crownItem.getLocation().getY() <= yHeight) setCrownState(CrownState.PODIUM, null, crownItem, false);
                }
                else if(crownEntity.getType() == EntityType.PLAYER){
                    Player player = (Player) crownEntity;
                    if(player.getLocation().getY() <= yHeight){
                        player.getInventory().remove(crownItem);
                        setCrownState(CrownState.PODIUM, null, null, false);
                    }
                }
            }
        }, 1L, 1L);

        runRunnables(true);
    }

    private void runRunnables(boolean firstTime){
        final int[] minute = {10};

        Runnable warnRunnable = new Runnable() {
            @Override
            public void run() {
                String timeAnnouncementString = plugin.getConfig().getString("location-ping.time-remaining");
                int time = 0;

                int cachedMinute = minute[0];
                System.out.println(cachedMinute);
                minute[0] = minute[0] - 1;
                System.out.println(minute[0]);
                System.out.println("________________");

                if(cachedMinute == 10) time = 10;
                else if (cachedMinute == 5) time = 5;
                else if (cachedMinute == 1) time = 1;
                else if (cachedMinute == 0){
                    String pingAnnouncementString = "";

                    if(crownEntity.getType() == EntityType.PLAYER) {
                        pingAnnouncementString = replaceEntityValues(
                                plugin.getConfig().getString("location-ping.player-has-control").replace("%player%", ((Player) crownEntity).getDisplayName())
                                , crownEntity);
                    }
                    else if(crownEntity.getType() == EntityType.DROPPED_ITEM){
                        pingAnnouncementString = replaceEntityValues(plugin.getConfig().getString("location-ping.no-player-has-control"), crownEntity);

                    }

                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        onlinePlayer.sendMessage(pingAnnouncementString);
                    }
                    System.out.println(pingAnnouncementString);

                    runRunnables(false);
                    return;
                }
                else return;

                timeAnnouncementString = timeAnnouncementString.replace("%time%", "" + time);

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.sendMessage(timeAnnouncementString);
                }

                System.out.println(timeAnnouncementString);
            }
        };

        String timerStartTimeInHours = "location-ping.timer-start-time-in-hours";
        String deductionTimeIncreaseInHours = "location-ping.deduction-time-increase-in-hours";
        String deductionTime = "stored-location-ping.deduction-time";
        String timerEndTime = "stored-location-ping.timer-end-time";

        Double hours = plugin.getConfig().getDouble(timerStartTimeInHours);
        Double deductHours = plugin.getConfig().getDouble(deductionTime);
        long timerEndTimeSeconds = plugin.getConfig().getLong(timerEndTime) - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        long seconds = (long)((hours - deductHours) * 3600d /* Hours to seconds */);
        if(firstTime && timerEndTimeSeconds > 0) seconds = timerEndTimeSeconds;

        if(teleportTimerId != null) Bukkit.getScheduler().cancelTask(teleportTimerId);
        teleportTimerId = null;

        System.out.println(seconds - TimeUnit.MINUTES.toSeconds(10L));

        teleportTimerId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, warnRunnable,
                (seconds - TimeUnit.MINUTES.toSeconds(10L)) * 20L, TimeUnit.MINUTES.toSeconds(1L) * 20L);

        Double deductionTimeIncrease = plugin.getConfig().getDouble(deductionTimeIncreaseInHours);

        plugin.getConfig().set(deductionTime, deductHours + deductionTimeIncrease);
        plugin.getConfig().set(timerEndTime, seconds + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
    }

    private String replaceEntityValues(String string, Entity entity){
        String newString = string.replace("%x%", "" + entity.getLocation().getBlockX())
                .replace("%y%", "" + entity.getLocation().getBlockY())
                .replace("%z%", "" + entity.getLocation().getBlockZ())
                .replace("%dimension%", environmentToString(entity.getLocation().getWorld().getEnvironment()));
        return newString;
    }

    public void stop(){
        isRunning = false;

        if(crownEntity.getType() == EntityType.PLAYER) ((Player)crownEntity).getInventory().remove(crownItem);
        else if(crownEntity.getType() == EntityType.DROPPED_ITEM) crownEntity.remove();
        crownEntity = null;

        if(playerLocationCacheTimerId != null) Bukkit.getScheduler().cancelTask(playerLocationCacheTimerId);
        playerLocationCacheTimerId = null;

        if(teleportTimerId != null) Bukkit.getScheduler().cancelTask(teleportTimerId);
        teleportTimerId = null;

        if(voidCheckTimerId != null) Bukkit.getScheduler().cancelTask(voidCheckTimerId);
        voidCheckTimerId = null;

        if(rewardsTimerId != null) Bukkit.getScheduler().cancelTask(rewardsTimerId);
        rewardsTimerId = null;

        if(locationPingTimerId != null) Bukkit.getScheduler().cancelTask(locationPingTimerId);
        locationPingTimerId = null;

        if(teleportTimerId != null) Bukkit.getScheduler().cancelTask(teleportTimerId);
        teleportTimerId = null;

        cachedLocation = null;
        saveCachedLocation();
    }

    private void addEffectsToItem(Item item){
        item.setGlowing(true);
        item.setFireTicks(0);
    }

    private void spawnCrown(Item item, boolean isFromRestart){
        readCachedLocation();
        Location spawnLocation;
        if(isFromRestart && cachedLocation != null) spawnLocation = cachedLocation;
        else spawnLocation = getSpawnCoords(world);

        if(crownItem == null) return;
        if(item == null){
            item = world.dropItem(spawnLocation, crownItem);
        }
        reCenterCrown(item, spawnLocation);
        crownEntity = item;
        addEffectsToItem(item);
    }

    private void saveCachedLocation(){
        plugin.getConfig().set("stored-position.position-cache", cachedLocation);
        plugin.saveConfig();
    }

    private void readCachedLocation(){
        cachedLocation = (Location) plugin.getConfig().get("stored-position.position-cache");
    }

    private Location getSpawnCoords(World world){
        int radius = plugin.getConfig().getInt("crown.spawn-radius");

        // nextInt is norm
        // ally exclusive of the top value,
        // so add 1 to make it inclusive
        int width = ThreadLocalRandom.current().nextInt(0, radius + 1); // Calculate x value of location
        int maximumPossibleHeight = (int) Math.floor(Math.sqrt(Math.pow(radius, 2) - Math.pow(width, 2))); // Maximum length of hypotenuse (radius) - current width gives us b^2, sqrt and flooring to get int.
        int height = ThreadLocalRandom.current().nextInt(0, maximumPossibleHeight + 1);
        int widthMultiplier = 1, heightMultiplier = 1; //To allow for both negative and positive positions
        if(ThreadLocalRandom.current().nextInt(0, 1 + 1) == 0) widthMultiplier = -1;
        if(ThreadLocalRandom.current().nextInt(0, 1 + 1) == 0) heightMultiplier = -1;
        width *= widthMultiplier;
        height *= heightMultiplier;
        return new Location(world, width, world.getHighestBlockYAt(width, height), height);
    }

    private ItemStack createCrownItem(){
        FileConfiguration config = plugin.getConfig();
        Material crownMaterial = Material.getMaterial(config.getString("crown-item.material-name"));
        if(crownMaterial == null){
            System.out.println("Incorrect Material Name!");
            return null;
        }
        ItemStack crownItem = new ItemStack(crownMaterial);
        if(config.getBoolean("crown-item.is-enchanted")){
            crownItem.addUnsafeEnchantment(Enchantment.LURE, 1);
            ItemMeta itemMeta = crownItem.getItemMeta();
            if(itemMeta == null) return null;
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            crownItem.setItemMeta(itemMeta);

        }
        ItemMeta itemMeta = crownItem.getItemMeta();
        if(itemMeta == null) return null;
        itemMeta.setDisplayName(config.getString("crown-item.name"));
        List<String> lore = new ArrayList<>(Arrays.asList(config.getString("crown-item.lore").split("%n%")));
        itemMeta.setLore(lore);
        crownItem.setItemMeta(itemMeta);
        return crownItem;
    }

    public void reCenterCrown(Item item, Location location){
        item.teleport(location);
    }

    public boolean isCrown(ItemStack itemStack){
        if(crownItem == null) return false;
        if(itemStack.getItemMeta() == null) return false;
        if(!itemStack.containsEnchantment(Enchantment.LURE) || !itemStack.getItemMeta().getItemFlags().contains(ItemFlag.HIDE_ENCHANTS)) return false;
        if(itemStack.getType() != crownItem.getType()) return false;
        if(!itemStack.getItemMeta().getDisplayName().equals(crownItem.getItemMeta().getDisplayName())) return false;
        if(!itemStack.getItemMeta().getLore().equals(crownItem.getItemMeta().getLore())) return false;
        return true;
    }

    // isFromRestart should never be passed by reference to setCrownState
    public void setCrownState(CrownState crownState, Player player, Item item, boolean isFromRestart) {
        this.crownState = crownState;
        if(crownState != CrownState.CONTROLLED) {
            if(playerLocationCacheTimerId != null) Bukkit.getScheduler().cancelTask(playerLocationCacheTimerId);
            playerLocationCacheTimerId = null;

            if(rewardsTimerId != null) Bukkit.getScheduler().cancelTask(rewardsTimerId);
            rewardsTimerId = null;
        }
        if(crownState != CrownState.TELEPORT){
            if(teleportTimerId != null) Bukkit.getScheduler().cancelTask(teleportTimerId);
            teleportTimerId = null;
        }

        switch (crownState) {
            // Requires item
            case DAMAGE -> {
                System.out.println("DAMAGE");
                if (item == null) break;
                crownEntity = item;

                cachedLocation = crownEntity.getLocation();
                saveCachedLocation();

                setCrownState(CrownState.PODIUM, player, item, false);
            }

            // Requires isFromRestart
            case PODIUM -> {
                System.out.println("PODIUM");
                crownEntity = item;

                spawnCrown(item, isFromRestart);

                String announcmentString = plugin.getConfig().getString("crown.item-spawn-announcement")
                        .replace("%x%", "" + crownEntity.getLocation().getBlockX())
                        .replace("%y%", "" + crownEntity.getLocation().getBlockY())
                        .replace("%z%", "" + crownEntity.getLocation().getBlockZ())
                        .replace("%dimension%", environmentToString(crownEntity.getLocation().getWorld().getEnvironment()));

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.sendMessage(announcmentString);
                }
                System.out.println(announcmentString);

                cachedLocation = crownEntity.getLocation();
                saveCachedLocation();
            }

            // Requires player and item
            case CONTROLLED -> {
                System.out.println("CONTROLLED");
                if (item == null || player == null) break;

                crownEntity = player;

                String[] commandLines = plugin.getConfig().getString("player-drop-pickup-commands.player-pickup-crown").split("%n%");
                for (String command : commandLines)
                    Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command.replace("%player%", player.getName()));

                long time = plugin.getConfig().getLong("crown.player-location-cache-time-seconds");
                playerLocationCacheTimerId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        cachedLocation = item.getLocation();
                        saveCachedLocation();
                    }
                }, 0L, time * 20L);

                long hours = plugin.getConfig().getLong("crown.reward-time-in-hours");
                rewardsTimerId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        String[] commandLines = plugin.getConfig().getString("crown.rewards-command").split("%n%");
                        for (String command : commandLines)
                            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command.replace("%player%", player.getName()));
                    }
                }, TimeUnit.HOURS.toSeconds(hours) * 20L, TimeUnit.HOURS.toSeconds(hours) * 20L);
            }

            // Requires player and item
            case TELEPORT -> {
                System.out.println("TELEPORT");
                if (item == null || player == null) break;
                crownEntity = item;

                addEffectsToItem(item);

                String[] commandLines = plugin.getConfig().getString("player-drop-pickup-commands.player-drop-crown").split("%n%");
                for (String command : commandLines)
                    Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command.replace("%player%", player.getName()));

                long time = plugin.getConfig().getLong("crown.item-teleport-time-seconds");
                teleportTimerId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        setCrownState(CrownState.PODIUM, player, item, false);
                    }
                }, time * 20L);

                cachedLocation = item.getLocation();
                saveCachedLocation();
            }

            // Requires player and item
            case SEMI_TELEPORT -> {
                System.out.println("SEMI_TELEPORT");
                if (item == null || player == null) break;
                crownEntity = item;

                addEffectsToItem(item);

                String[] commandLines = plugin.getConfig().getString("player-drop-pickup-commands.player-drop-crown").split("%n%");
                for (String command : commandLines)
                    Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command.replace("%player%", player.getName()));

                cachedLocation = item.getLocation();
                saveCachedLocation();
            }
        }
    }

    private String environmentToString(World.Environment environment){
        return switch (environment) {
            case CUSTOM -> "Custom";
            case NETHER -> "Nether";
            case NORMAL -> "Overworld";
            case THE_END -> "End";
        };
    }

    public Entity getCrownEntity() {
        return crownEntity;
    }

    public World getWorld() {
        return world;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public ItemStack getCrownItem() {
        return crownItem;
    }

    public CrownState getCrownState() {
        return crownState;
    }
}
