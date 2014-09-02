package com.mengcraft.team;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;


public class TeamRoll extends JavaPlugin {

    public static HashSet<Integer> dropItems;

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("TeamPlugin") != null) {
            getServer().getPluginManager().registerEvents(new RollListener(), this);
            dropItems = new HashSet<>();
            getServer().getScheduler().runTaskTimerAsynchronously(this, new ClearItemSet(), 3600, 3600);
        } else {
            setEnabled(false);
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "梦梦家高性能服务器出租");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "淘宝店 http://shop105595113.taobao.com");
    }

    private class RollListener implements Listener {

        @EventHandler
        public void onPlayerDropItem(PlayerDropItemEvent event) {
            int getEntityId = event.getItemDrop().getEntityId();
            TeamRoll.dropItems.add(getEntityId);
        }

        @EventHandler
        public void onPlayerPickup(PlayerPickupItemEvent event) {
            if (dropItems.contains(event.getItem().getEntityId())) {
                TeamRoll.dropItems.remove(event.getItem().getEntityId());
            } else if (TeamUtils.getPlayerLeader(event.getPlayer().getName()) != null) {
                Player player = rollPlayer(event.getPlayer());
                if (player != null) {
                    Item getItem = event.getItem();
                    ItemStack itemStack = getItem.getItemStack();
                    HashMap<Integer, ItemStack> map = player.getInventory().addItem(itemStack);
                    if (map.isEmpty()) {
                        player.sendMessage(ChatColor.DARK_PURPLE + "恭喜你获得了物品");
                    } else {
                        Location loc = player.getLocation();
                        player.getWorld().dropItem(loc, itemStack);
                    }
                    getItem.remove();
                    event.setCancelled(true);
                }
            }
        }

        private Player rollPlayer(Player player) {
            List<Player> nearby = TeamUtils.getTeamMateNearby(player.getName());
            if (nearby.size() > 1) {
                Random random = new Random();
                Player roll = null;
                int point = 0;
                for (Player near : nearby) {
                    int i = random.nextInt(1024);
                    near.sendMessage(ChatColor.DARK_PURPLE + "你掷出了 " + i + " 点");
                    if (i > point) {
                        point = i;
                        roll = near;
                    }
                }
                return roll;
            } else {
                return null;
            }
        }
    }

    private class ClearItemSet extends BukkitRunnable {
        @Override
        public void run() {
            TeamRoll.dropItems.clear();
        }
    }
}


