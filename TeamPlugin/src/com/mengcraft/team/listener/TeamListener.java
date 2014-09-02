package com.mengcraft.team.listener;

import com.mengcraft.team.TeamPlugin;
import com.mengcraft.team.TeamUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

public class TeamListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerReSpawn(PlayerRespawnEvent event) {
        String playerName = event.getPlayer().getName();
        String leaderName = TeamUtils.getPlayerLeader(playerName);
        if (leaderName != null) {
            Location location = TeamUtils.getTeamSpawn(leaderName);
            if (location != null) {
                event.setRespawnLocation(location);
            }
        }
    }

    @EventHandler
    void onPlayerChat(AsyncPlayerChatEvent event) {
        String playerName = event.getPlayer().getName();
        String leaderName = TeamUtils.getPlayerLeader(playerName);
        if (leaderName != null) {
            String prefix = TeamUtils.getTeamPrefix(leaderName);
            if (prefix != null) {
                prefix = ChatColor.GOLD + "<" + prefix + ChatColor.GOLD + ">" + ChatColor.RESET;
                String getFormat = event.getFormat();
                event.setFormat(prefix + getFormat);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void leaderUnDeath(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            String name = player.getName();
            String leader = TeamUtils.getPlayerLeader(name);
            boolean b = name.equals(leader)
                    && player.getHealth() - event.getDamage() < 0
                    && TeamUtils.getTeamSkillTime(leader) + 90 < System.currentTimeMillis() / 1000;
            if (b) {
                TeamUtils.setTeamSkillTime(name);
                int size = TeamUtils.getTeamMateNearby(name).size();
                String[] messages = {
                        ChatColor.GREEN + "抵抗死亡发动成功!",
                        ChatColor.GREEN + "你获得 " + size + " 秒的无敌时间!"
                };
                player.sendMessage(messages);
                player.setNoDamageTicks(20 * size);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getDamager() instanceof Player) {
                boolean isMate = isMate(event);
                if (isMate) event.setCancelled(true);
            }
        }
    }

    @EventHandler
    void onPlayerExpChange(PlayerExpChangeEvent event) {
        String playerName = event.getPlayer().getName();
        String leaderName = TeamUtils.getPlayerLeader(playerName);
        boolean status = leaderName != null;
        if (status) {
            String teamName = TeamUtils.getPlayerLeader(playerName);
            int teamLevel = TeamUtils.getTeamLevel(teamName);
            status = teamLevel > 1;
            if (status) {
                int extBefore = event.getAmount();
                int expPlus = Math.round(extBefore / 5);
                int expAfter = extBefore + expPlus;
                status = expAfter > 4;
                if (status) TeamUtils.setTeamExp(teamName, playerName);
                event.setAmount(expAfter);
            }
        }
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event) {
        double maxHealth = 20;
        Player player = event.getPlayer();
        String playerName = player.getName();
        String leaderName = TeamUtils.getPlayerLeader(playerName);
        boolean status = leaderName != null;
        if (status) {
            String msg;

            TeamUtils.setPlayerJoinTime(playerName);
            TeamUtils.setPlayerList(playerName, true);

            int teamLevel = TeamUtils.getTeamLevel(leaderName);
            maxHealth = 18 + 2 * teamLevel;

            status = playerName.equals(leaderName);
            if (status) {
                TeamUtils.setTeamSkillTime(leaderName);
                msg = "队长 " + playerName + " 上线了";
            } else msg = "队友 " + playerName + " 上线了";
            TeamUtils.setTeamMessage(leaderName, playerName, ChatColor.GREEN + msg);
        }
        String path = "config.team.growhealth";
        status = TeamPlugin.plugin.getConfig().getBoolean(path, true);
        if (status) player.setMaxHealth(maxHealth);
    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();
        String leaderName = TeamUtils.getPlayerLeader(playerName);
        boolean status = leaderName != null;
        if (status) {
            String s;

            TeamUtils.setPlayerList(playerName, false);
            status = playerName.equals(leaderName);
            if (status) {
                String path = "teams." + leaderName + ".effect";
                int taskId = TeamPlugin.tempConfig.getInt(path);
                if (taskId > 0) {
                    TeamPlugin.tempConfig.set(path, null);
                    TeamPlugin.plugin.getServer().getScheduler().cancelTask(taskId);
                }
                s = "队长 " + playerName + " 下线了";
            } else s = "队友 " + playerName + " 下线了";
            String teamName = TeamUtils.getPlayerLeader(playerName);
            TeamUtils.setTeamMessage(teamName, playerName, ChatColor.RED + s);
        }
    }

    private boolean isMate(EntityDamageByEntityEvent event) {
        Player self = (Player) event.getEntity();
        String selfLeader = TeamUtils.getPlayerLeader(self.getName());
        if (selfLeader != null) {
            Player other = (Player) event.getDamager();
            String otherLeader = TeamUtils.getPlayerLeader(other.getName());
            return selfLeader.equals(otherLeader);
        }
        return false;
    }

}

