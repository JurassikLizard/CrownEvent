package me.jurassiklizard.crownevent.events;


import me.jurassiklizard.crownevent.CrownEvent;
import me.jurassiklizard.crownevent.enums.CrownState;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CrownEventsListener implements Listener {
    private CrownEvent plugin;

    public CrownEventsListener(CrownEvent plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemDropEvent(PlayerDropItemEvent e){
        if(!plugin.getCrownManager().isRunning()) return;

        ItemStack itemStack = e.getItemDrop().getItemStack();
        if(!plugin.getCrownManager().isCrown(itemStack)) return;
        Player player = e.getPlayer();
        plugin.getCrownManager().setCrownState(CrownState.TELEPORT, player, e.getItemDrop(), false);
    }

    @EventHandler
    public void onEntityItemPickup(EntityPickupItemEvent e){
        if(!plugin.getCrownManager().isRunning()) return;

        ItemStack itemStack = e.getItem().getItemStack();
        if(!plugin.getCrownManager().isCrown(itemStack)) return;
        if(!(e.getEntity() instanceof Player)){
            e.setCancelled(true);
            return;
        }
        Player player = (Player) e.getEntity();
        plugin.getCrownManager().setCrownState(CrownState.CONTROLLED, player, e.getItem(), false);
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent e){
        if(!plugin.getCrownManager().isRunning()) return;

        if(e.getEntity().getType() != EntityType.DROPPED_ITEM) return;
        ItemStack itemStack = ((Item)e.getEntity()).getItemStack();
        if(!plugin.getCrownManager().isCrown(itemStack)) return;
        plugin.getCrownManager().setCrownState(CrownState.DAMAGE, null, ((Item)e.getEntity()), false); //Re-teleport
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent e){
        if(!plugin.getCrownManager().isRunning()) return;

        List<ItemStack> drops = e.getDrops();
        if(!drops.contains(plugin.getCrownManager().getCrownItem())) return;
        if(plugin.getCrownManager().getCrownEntity() != e.getEntity()) return;
        drops.remove(plugin.getCrownManager().getCrownItem());
        Item item = e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), plugin.getCrownManager().getCrownItem());
        plugin.getCrownManager().setCrownState(CrownState.TELEPORT, e.getEntity(), item, false);
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e){
        if(!plugin.getCrownManager().isRunning()) return;

        if(e.getPlayer().getInventory().contains(plugin.getCrownManager().getCrownItem())){
            e.getPlayer().getInventory().remove(plugin.getCrownManager().getCrownItem());
            Item item = e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), plugin.getCrownManager().getCrownItem());
            e.getPlayer().getInventory().remove(plugin.getCrownManager().getCrownItem());
            plugin.getCrownManager().setCrownState(CrownState.SEMI_TELEPORT, e.getPlayer(), item, false);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        e.getPlayer().getInventory().remove(plugin.getCrownManager().getCrownItem());
    }

    @EventHandler
    public void onItemDespawnEvent(ItemDespawnEvent e){
        if(!plugin.getCrownManager().isRunning()) return;

        ItemStack itemStack = e.getEntity().getItemStack();
        if(!plugin.getCrownManager().isCrown(itemStack)) return;
        e.setCancelled(true);
    }

//    @EventHandler
//    public void onHopperPickup(InventoryPickupItemEvent e){
//        ItemStack itemStack = e.getItem().getItemStack();
//        if(!plugin.getCrownManager().isCrown(itemStack)) return;
//        if(e.getInventory().getType() == InventoryType.HOPPER) e.setCancelled(true);
//    }
}
