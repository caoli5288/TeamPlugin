package com.mengcraft.team;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TeamHunter extends JavaPlugin {
    public static Plugin plugin = null;
    public static String actWorldName = null;
    public static String actCode = null;
    public static String actLoc = null;
    public static long actPoint = 0;

    @Override
    public void onEnable() {
        plugin = this;
        FileConfiguration getConfig = getConfig();
        actWorldName = getConfig.getString("hunter.act-world");
        actCode = getConfig.getString("hunter.act-code");
        actLoc = getConfig.getString("hunter.act-loc");
        actPoint = getConfig.getLong("hunter.act-point");
        new BroadThread().runTaskTimer(this, 3600, 3600);
        getCommand("team-hunter").setExecutor(new THCommand());
        THListener listener = new THListener();
        getServer().getPluginManager().registerEvents(listener, plugin);
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "梦梦家高性能服务器出租");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "淘宝店 http://shop105595113.taobao.com");
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "梦梦家高性能服务器出租");
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "淘宝店 http://shop105595113.taobao.com");
    }

    class BroadThread extends BukkitRunnable {
        @Override
        public void run() {
            String message;
            String[] loc;
            if (TeamHunter.actWorldName != null) {
                loc = TeamHunter.actLoc.split(";");
                message = ChatColor.GREEN + "宝箱安静地躺在在 " + TeamHunter.actWorldName + " 世界," +
                        " 坐标 " + loc[0] + ", " + loc[1] + ", " + loc[2] + " 处!";
                TeamHunter.plugin.getServer().broadcastMessage(message);
                message = ChatColor.GREEN + "最先夺得 " + TeamHunter.actPoint + " 个宝箱的队伍获得胜利!";
                TeamHunter.plugin.getServer().broadcastMessage(message);
            }
        }
    }
}
