package com.mengcraft.team;

import com.mengcraft.team.listener.TeamListener;
import com.mengcraft.team.listener.TagListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.mcstats.Metrics;

import java.io.IOException;
import java.util.List;

public class TeamPlugin extends JavaPlugin {
    public static Plugin plugin;
    public static Configuration tempConfig;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        plugin = this;
        tempConfig = new YamlConfiguration();
    }

    @Override
    public void onEnable() {
        try {
            new Metrics(this).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (getServer().getPluginManager().getPlugin("TagAPI") != null) {
            getServer().getPluginManager().registerEvents(new TagListener(), this);
        }
        if (getConfig().getBoolean("config.team.overRideSpawnCommand", false)) {
            getServer().getPluginManager().registerEvents(new SpawnCommand(), this);
        }
        getServer().getPluginManager().registerEvents(new TeamListener(), this);
        getOnlineTeamMate();
        new OnlineTimeThread().runTaskTimer(this, 1200, 1200);
        new SendAdvert().runTaskLater(this, 120);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean isPlayer = sender instanceof Player;
        boolean isName = command.getName().equalsIgnoreCase("team");
        boolean status = isPlayer && isName;
        if (status) Commander.commands(sender, args);
        isName = command.getName().equalsIgnoreCase("tm");
        status = isPlayer && isName;
        if (status) Commander.teamMessage(sender, args);
        return true;
    }

    private void getOnlineTeamMate() {
        String getName;
        String getLeaderName;

        Player[] onlinePlayers = getServer().getOnlinePlayers();
        for (Player player : onlinePlayers) {
            getName = player.getName();
            getLeaderName = TeamUtils.getPlayerLeader(getName);
            if (getLeaderName != null) TeamUtils.setPlayerList(getName, true);
        }
    }

    private class SendAdvert extends BukkitRunnable {
        @Override
        public void run() {
            String[] messages = {
                    ChatColor.GREEN + "梦梦家高性能服务器出租"
                    , ChatColor.GREEN + "淘宝店 http://shop105595113.taobao.com"
            };
            Bukkit.getConsoleSender().sendMessage(messages);
        }
    }

    private class OnlineTimeThread extends BukkitRunnable {
        @Override
        public void run() {
            List<String> playerList = TeamUtils.getPlayerList();
            for (String playerName : playerList) TeamUtils.setPlayerOnlineTime(playerName);
            saveConfig();
        }
    }

    private class SpawnCommand implements Listener {
        @EventHandler
        public void spawnCommand(PlayerCommandPreprocessEvent event) {
            boolean b = event.getMessage().equals("/spawn");
            if (b) {
                Player player = event.getPlayer();
                Commander.teamSpawn(player);
                event.setCancelled(true);
            }
        }
    }
}



