package com.mengcraft.team;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class TeamUtils {
    public static boolean setPlayerLeader(String playerName, String leaderName) {
        String path = "players." + playerName + ".leader";
        TeamPlugin.plugin.getConfig().set(path, leaderName);
        return true;
    }

    public static boolean setPlayerOnlineTime(String playerName) {
        long currentTime = System.currentTimeMillis() / 60000;

        String path = "players." + playerName + ".join";
        long joinTime = TeamPlugin.tempConfig.getLong(path, currentTime);
        TeamPlugin.tempConfig.set(path, currentTime);

        path = "players." + playerName + ".online";
        long onlineTime = TeamPlugin.plugin.getConfig().getInt(path, 0);
        onlineTime = onlineTime + currentTime - joinTime;
        TeamPlugin.plugin.getConfig().set(path, onlineTime);

        return true;
    }

    public static boolean setPlayerJoinTime(String playerName) {
        long currentTime = System.currentTimeMillis() / 60000;
        String string = "players." + playerName + ".join";
        TeamPlugin.tempConfig.set(string, currentTime);
        return true;
    }

    public static boolean setRequestList(String leaderName, String playerName, boolean isAdd) {
        String path = "requests." + leaderName;
        List<String> requestList = TeamPlugin.tempConfig.getStringList(path);
        boolean status = isAdd;
        if (status) {
            status = !requestList.contains(playerName);
            if (status) requestList.add(playerName);
        } else requestList.remove(playerName);
        TeamPlugin.tempConfig.set(path, requestList);
        return true;
    }

    public static boolean setTeamLevel(String leaderName, int level) {
        String path = "teams." + leaderName + ".level";
        TeamPlugin.plugin.getConfig().set(path, level);
        return true;
    }

    public static boolean setTeamMate(String leaderName, String playerName, boolean isAdd) {
        String path;
        List<String> memberList;
        path = "teams." + leaderName + ".members";
        memberList = TeamPlugin.plugin.getConfig().getStringList(path);
        if (isAdd) {
            memberList.add(playerName);
        } else {
            memberList.remove(playerName);
            path = "players." + playerName;
            TeamPlugin.plugin.getConfig().set(path, null);
            path = "teams." + leaderName + ".members";
        }
        TeamPlugin.plugin.getConfig().set(path, memberList);
        return true;
    }

    public static boolean setTeamMessage(String teamName, String senderName, String msg) {
        List<Player> memberList = getTeamMateOnline(teamName);
        Player senderPlayer = TeamPlugin.plugin.getServer().getPlayerExact(senderName);
        memberList.remove(senderPlayer);
        for (Player memberPlayer : memberList) {
            memberPlayer.sendMessage(msg);
        }
        return true;
    }

    public static boolean setTeamMessage(String teamName, String msg) {
        List<Player> memberList = getTeamMateOnline(teamName);
        for (Player memberPlayer : memberList) {
            memberPlayer.sendMessage(msg);
        }
        return true;
    }

    public static boolean setTeamExp(String leaderName, String playerName) {
        Player playerPlayer = TeamPlugin.plugin.getServer().getPlayerExact(playerName);
        List<Player> memberPlayerList = getTeamMateOnline(leaderName);
        memberPlayerList.remove(playerPlayer);
        for (Player memberPlayer : memberPlayerList) {
            memberPlayer.giveExp(1);
        }
        return true;
    }

    public static boolean setTeamSkillTime(String leaderName) {
        String path = "teams." + leaderName + ".skilltime";
        long currentTime = System.currentTimeMillis() / 1000;
        TeamPlugin.tempConfig.set(path, currentTime);
        return true;
    }

    public static String getPlayerLeader(String playerName) {
        String path = "players." + playerName + ".leader";
        return TeamPlugin.plugin.getConfig().getString(path);
    }

    public static int getPlayerOnlineTime(String playerName) {
        String path = "players." + playerName + ".online";
        return TeamPlugin.plugin.getConfig().getInt(path);
    }

    public static List<String> getRequestList(String leaderName) {
        String path = "requests." + leaderName;
        return TeamPlugin.tempConfig.getStringList(path);
    }

    public static double getTeamExpNeed(int teamLevel) {
        String path = "config.team.leastexp";
        double expNeed = TeamPlugin.plugin.getConfig().getInt(path, 24) * 60;

        if (teamLevel > 1) {
            teamLevel = teamLevel - 1;
            expNeed = expNeed * Math.pow(2, teamLevel);
        }
        return expNeed;
    }

    public static int getTeamLevel(String leaderName) {
        String path = "teams." + leaderName + ".level";
        return TeamPlugin.plugin.getConfig().getInt(path, 0);
    }

    public static int getTeamLimit(int teamLevel) {
        FileConfiguration getConfig = TeamPlugin.plugin.getConfig();
        String path = "config.team.size";
        int configSize = getConfig.getInt(path, 4);
        path = "config.team.growperlevel";
        int growSize = getConfig.getInt(path, 2) * (teamLevel - 1);
        return configSize + growSize;
    }

    public static List<Player> getTeamMateOnline(String teamName) {
        Server getServer = TeamPlugin.plugin.getServer();
        List<Player> onlineMemberList = new ArrayList<Player>();
        List<String> memberList = getTeamMate(teamName);
        Player player;
        boolean status;
        for (String memberName : memberList) {
            player = getServer.getPlayerExact(memberName);
            status = player != null && player.isOnline();
            if (status) onlineMemberList.add(player);
        }
        return onlineMemberList;
    }

    public static List<String> getTeamMate(String teamName) {
        String path = "teams." + teamName + ".members";
        return TeamPlugin.plugin.getConfig().getStringList(path);
    }

    public static List<Player> getTeamMateNearby(String playerName) {
        boolean status;
        Player getPlayer;
        String getPlayerName;
        String getLeaderName;

        String leaderName = TeamUtils.getPlayerLeader(playerName);

        List<Player> playerList = new ArrayList<Player>();

        Player leader = TeamPlugin.plugin.getServer().getPlayerExact(playerName);
        playerList.add(leader);

        String path = "config.team.effectnear";
        int d = TeamPlugin.plugin.getConfig().getInt(path, 16);
        List<Entity> nearbyEntities = leader.getNearbyEntities(d, d, d);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player) {
                getPlayer = (Player) entity;
                getPlayerName = getPlayer.getName();
                getLeaderName = TeamUtils.getPlayerLeader(getPlayerName);
                status = leaderName.equals(getLeaderName);
                if (status) playerList.add(getPlayer);
            }
        }
        return playerList;
    }

    public static long getTeamSkillTime(String leaderName) {
        String path = "teams." + leaderName + ".skilltime";
        return TeamPlugin.tempConfig.getLong(path);
    }

    public static boolean setPlayerList(String playerName, boolean b) {
        String path = "members";
        List<String> memberList = TeamPlugin.tempConfig.getStringList(path);
        if (b) memberList.add(playerName);
        else memberList.remove(playerName);
        TeamPlugin.tempConfig.set(path, memberList);
        return true;
    }

    public static List<String> getPlayerList() {
        String path = "members";
        return TeamPlugin.tempConfig.getStringList(path);
    }

    public static boolean setTeamPrefix(String leaderName, String string) {
        String path = "teams." + leaderName + ".prefix";
        TeamPlugin.plugin.getConfig().set(path, string);
        return true;
    }

    public static String getTeamPrefix(String leaderName) {
        String path = "teams." + leaderName + ".prefix";
        return TeamPlugin.plugin.getConfig().getString(path);
    }

    public static void setTeamSpawn(String leaderName, Location location) {
        StringBuilder builder = new StringBuilder();
        String worldName = location.getWorld().getName();
        builder.append(worldName);
        builder.append(";");
        double x = location.getX();
        builder.append(x);
        builder.append(";");
        double y = location.getY();
        builder.append(y);
        builder.append(";");
        double z = location.getZ();
        builder.append(z);
        String path = "teams." + leaderName + ".spawn";
        String spawn = builder.toString();
        TeamPlugin.plugin.getConfig().set(path, spawn);
    }

    public static Location getTeamSpawn(String leaderName) {
        String path = "teams." + leaderName + ".spawn";
        String spawn = TeamPlugin.plugin.getConfig().getString(path, null);
        if (spawn != null) {
            String[] str = spawn.split(";");
            World world = TeamPlugin.plugin.getServer().getWorld(str[0]);
            double x = Double.valueOf(str[1]);
            double y = Double.valueOf(str[2]);
            double z = Double.valueOf(str[3]);
            return new Location(world, x, y, z);
        }
        return null;
    }
}
