package com.mengcraft.team;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class THCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player && args.length > 0) {
            if (args.length>1) TeamHunter.actPoint = Long.valueOf(args[1]);
            else TeamHunter.actPoint = 20;

            boolean status = args[0].equals("start");
            if (status && TeamHunter.actWorldName == null) {
                String senderName = sender.getName();
                Player senderPlayer = TeamHunter.plugin.getServer().getPlayerExact(senderName);
                TeamHunter.actWorldName = senderPlayer.getWorld().getName();
                Runnable runnable = new THChestThread();
                TeamHunter.plugin.getServer().getScheduler().runTask(TeamHunter.plugin, runnable);
                String message = ChatColor.GREEN + "夺宝游戏开始!!!";
                TeamHunter.plugin.getServer().broadcastMessage(message);
                message = ChatColor.GREEN + "最先夺得 " + TeamHunter.actPoint + " 个宝箱的队伍获得胜利!";
                TeamHunter.plugin.getServer().broadcastMessage(message);

                return true;
            } else {
                String message = ChatColor.RED + "已经有比赛在进行中!";
                sender.sendMessage(message);
                return true;
            }
        }
        String message = ChatColor.GOLD + "/team-hunter start [20]";
        sender.sendMessage(message);
        return true;
    }
}
