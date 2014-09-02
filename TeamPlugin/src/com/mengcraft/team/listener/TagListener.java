package com.mengcraft.team.listener;

import com.mengcraft.team.TeamUtils;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.kitteh.tag.AsyncPlayerReceiveNameTagEvent;

public class TagListener implements Listener {

    @EventHandler
    public void on(AsyncPlayerReceiveNameTagEvent event) {
        String playerName = event.getNamedPlayer().getName();
        String leaderName = TeamUtils.getPlayerLeader(playerName);
        if (leaderName != null) {
            String prefix = TeamUtils.getTeamPrefix(leaderName);
            if (prefix != null) {
                event.setTag(ChatColor.GOLD + "<" + prefix + ChatColor.GOLD + ">"
                        + ChatColor.RESET + "<" + playerName + ">");
            }
        }
    }
}
