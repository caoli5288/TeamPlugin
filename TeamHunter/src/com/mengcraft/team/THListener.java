package com.mengcraft.team;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;

public class THListener implements Listener {
    @EventHandler
    public void BlockBreakEvent(BlockBreakEvent event) {
        if (TeamHunter.actWorldName != null) {
            Location getLocation = event.getBlock().getLocation();
            String worldName = getLocation.getWorld().getName();
            boolean status = worldName.equals(TeamHunter.actWorldName);
            if (status) {
                String loc = getLocation.getBlockX() + ";"
                        + getLocation.getBlockY() + ";"
                        + getLocation.getBlockZ();
                status = loc.equals(TeamHunter.actLoc);
                if (status) event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent event) {
        if (TeamHunter.actCode != null) {
            ItemStack itemStack = event.getCurrentItem();
            boolean status = itemStack != null && itemStack.hasItemMeta();
            if (status) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                status = itemMeta.hasLore();
                if (status) {
                    List<String> lore = itemMeta.getLore();
                    status = lore.get(0).equals(TeamHunter.actCode);
                    if (status) {
                        String getWhoClicked = event.getWhoClicked().getName();
                        Player player = TeamHunter.plugin.getServer().getPlayerExact(getWhoClicked);
                        String playerName = player.getName();
                        String leaderName = TPUtils.getPlayerLeader(playerName);
                        if (leaderName != null) {
                            World world = TeamHunter.plugin.getServer().getWorld(TeamHunter.actWorldName);

                            String[] loc = TeamHunter.actLoc.split(";");
                            double x = Double.valueOf(loc[0]);
                            double y = Double.valueOf(loc[1]);
                            double z = Double.valueOf(loc[2]);
                            Location location = new Location(world, x, y, z);

                            lore.add(0, ChatColor.GOLD + "<纪念物品>");
                            lore.remove(3);
                            itemMeta.setLore(lore);
                            itemStack.setItemMeta(itemMeta);

                            HashMap<Integer, ItemStack> itemMap = player.getInventory().addItem(itemStack);
                            status = itemMap.isEmpty();
                            if (!status) world.dropItem(location, itemStack);

                            ItemStack airStack = new ItemStack(Material.AIR);

                            event.setCurrentItem(airStack);
                            event.setCancelled(true);

                            long point = THUtils.getTeamPoint(leaderName) + 1;
                            THUtils.setTeamPoint(leaderName, point);
                            if (point == TeamHunter.actPoint) {
                                FileConfiguration getConfig = TeamHunter.plugin.getConfig();

                                getConfig.set("teams", null);
                                getConfig.set("hunter", null);

                                TeamHunter.actLoc = null;
                                TeamHunter.actWorldName = null;
                                TeamHunter.actCode = null;
                                TeamHunter.actPoint = 0;

                                TeamHunter.plugin.saveConfig();

                                String s = ChatColor.GREEN + "恭喜 " + leaderName + " 率领队员们赢得夺宝游戏大赛!";
                                TeamHunter.plugin.getServer().broadcastMessage(s);
                                TeamHunter.plugin.getServer().broadcastMessage(s);
                                TeamHunter.plugin.getServer().broadcastMessage(s);
                            } else {
                                Runnable runnable = new THChestThread();
                                TeamHunter.plugin.getServer().getScheduler().runTask(TeamHunter.plugin, runnable);

                                String s = ChatColor.GREEN + "恭喜玩家 " + playerName + " 夺得宝箱!";
                                TeamHunter.plugin.getServer().broadcastMessage(s);

                                s = ChatColor.GREEN + "队伍 " + leaderName + " 已夺得 " + point + " 个宝箱!";
                                TeamHunter.plugin.getServer().broadcastMessage(s);
                            }
                            player.closeInventory();
                            CleanChestThread thread = new CleanChestThread(location);
                            TeamHunter.plugin.getServer().getScheduler().runTaskLater(TeamHunter.plugin, thread, 20);
                        } else {
                            event.setCancelled(true);
                            String s = ChatColor.RED + "你不是队伍成员";
                            player.sendMessage(s);
                        }
                    }
                }
            }
        }
    }

}

class CleanChestThread implements Runnable {
    static Location location;

    public CleanChestThread(Location loc) {
        location = loc;
    }

    @Override
    public void run() {
        location.getBlock().setType(Material.AIR);
    }


}
