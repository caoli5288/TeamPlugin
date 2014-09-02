package com.mengcraft.team;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TeamChest extends JavaPlugin implements Listener {

    private static Plugin teamChest;
    public static Map<String, Inventory> inventoryMap;
    private Plugin teamPlugin;

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("TeamPlugin") == null) {
            setEnabled(false);
        } else {
            teamPlugin = getServer().getPluginManager().getPlugin("TeamPlugin");
            teamChest = this;
            getServer().getPluginManager().registerEvents(this, this);
            inventoryMap = new HashMap<>();
            new ChestThread().runTaskTimer(this, 6000, 6000);
        }
    }

    @EventHandler
    public void command(PlayerCommandPreprocessEvent event) {
        boolean b = event.getMessage().equals("/team chest");
        if (b) {
            event.setCancelled(true);
            getChest(event.getPlayer());
        }
    }

    private void getChest(Player player) {

        String playerName = player.getName();
        String leaderName = TeamUtils.getPlayerLeader(playerName);

        if (leaderName != null) {
            Inventory inventory = TeamChest.inventoryMap.get(leaderName);
            if (inventory != null) player.openInventory(inventory);
            else {
                int teamLevel = TeamUtils.getTeamLevel(leaderName);
                String prefix = teamPlugin.getConfig().getString("teams." + leaderName + ".prefix", "") + ChatColor.RESET;
                inventory = TeamChest.teamChest.getServer().createInventory(null, teamLevel * 9, "队伍 " + prefix + " 公共箱子");
                ConfigurationSection section = TeamChest.teamChest.getConfig().getConfigurationSection(leaderName);
                if (section != null) {
                    Set<String> itemStacks = section.getKeys(false);
                    int i = 0;
                    ItemStack stack;
                    for (String itemStack : itemStacks) {
                        stack = section.getItemStack(itemStack);
                        inventory.setItem(i, stack);
                        i = i + 1;
                    }
                }
                TeamChest.inventoryMap.put(leaderName, inventory);
                getChest(player);
            }
        }
    }

    private void getChestSave() {
        Inventory inventory;
        int i;
        ItemStack[] stacks;
        Set<String> keySet = TeamChest.inventoryMap.keySet();
        for (String key : keySet) {
            TeamChest.teamChest.getConfig().set(key, null);
            inventory = TeamChest.inventoryMap.get(key);
            stacks = inventory.getContents();
            i = 0;
            for (ItemStack stack : stacks) {
                if (stack != null) TeamChest.teamChest.getConfig().set(key + "." + i, stack);
                i = i + 1;
            }
        }
        TeamChest.teamChest.saveConfig();
        TeamChest.inventoryMap = new HashMap<>();
    }

    private class ChestThread extends BukkitRunnable {
        @Override
        public void run() {
            getChestSave();
        }
    }

}


