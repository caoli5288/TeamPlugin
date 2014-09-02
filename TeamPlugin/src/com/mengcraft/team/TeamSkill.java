package com.mengcraft.team;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TeamSkill
{
	public static void skillHeal(List<Player> memberNearLeader, int teamLevel)
	{
		double getHealth;
		double getMaxHealth;

		String msg = ChatColor.GREEN + "你被 生命之光 笼罩了!";
		
		if (teamLevel > 4) {
			for (Player player : memberNearLeader) {
				getMaxHealth = player.getMaxHealth();
				player.setHealth(getMaxHealth);
				player.sendMessage(msg);
			}
		}
		else if (teamLevel > 2) {
            double setHealth;
			for (Player player : memberNearLeader) {
				getHealth = player.getHealth();
				getMaxHealth = player.getMaxHealth();
				getHealth = getHealth + 15;
				setHealth = Math.min(getHealth, getMaxHealth);
				player.setHealth(setHealth);
				player.sendMessage(msg);
			}
		}
		else {
            double setHealth;
			for (Player player : memberNearLeader) {
				getHealth = player.getHealth();
				getMaxHealth = player.getMaxHealth();
				getHealth = getHealth + 10;
				setHealth = Math.min(getHealth, getMaxHealth);
				player.setHealth(setHealth);
				player.sendMessage(msg);
			}
		}
	}
	
}
