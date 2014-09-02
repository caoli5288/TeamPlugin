package com.mengcraft.team;

import com.mengcraft.team.thread.TeleportThread;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Commander {
    static boolean commands(CommandSender sender, String[] args) {
        if (args.length > 0) {
            int hashCode = args[0].hashCode();
            switch (hashCode) {
                case 92762796://agree
                    teamAgree(sender, args);
                    break;
                case -1352294148://create
                    teamCreate(sender);
                    break;
                case 94627585://chest
                    sender.sendMessage(ChatColor.RED + "腐竹没有安装队伍箱子插件哦");
                    break;
                case 287951985://dissolve
                    teamDissolve(sender);
                    break;
                case -1306084975://effect
                    teamEffect(sender);
                    break;
                case 3145580://flag
                    teamFlag(sender);
                    break;
                case 3198440://heal
                    teamSkill(sender, args[0]);
                    break;
                case 3267882://join
                    teamJoin(sender, args);
                    break;
                case -1106754295://leader
                    teamLeader(sender);
                    break;
                case 69785887://level up
                    teamLevelUp(sender);
                    break;
                case 3322014://list
                    teamList(sender);
                    return true;
                case -980110702://prefix
                    teamPrefix(sender, args);
                    break;
                case 3482191://quit
                    teamQuit(sender);
                    break;
                case -934610812://remove
                    teamRemove(sender, args);
                    break;
                case 1433904217://setspawn
                    teamSetSpawn(sender);
                    break;
                case 109638523://spawn
                    Player player = (Player) sender;
                    teamSpawn(player);
                    break;
                default:
                    teamInfo(sender);
                    break;
            }
            return true;
        }
        teamInfo(sender);
        return true;
    }

    public static void teamSpawn(Player player) {
        String senderName = player.getName();
        String leaderName = TeamUtils.getPlayerLeader(senderName);
        if (leaderName != null) {
            Location spawn = TeamUtils.getTeamSpawn(leaderName);
            if (spawn != null) {
                TeleportThread thread = new TeleportThread(player, spawn);
                TeamPlugin.plugin.getServer().getScheduler().runTaskLater(TeamPlugin.plugin, thread, 60);
                player.sendMessage(ChatColor.GREEN + "三秒后开始传送");
            } else player.sendMessage(ChatColor.RED + "请队长先设置复活点!");
        } else player.sendMessage(ChatColor.RED + "请先加入或创建队伍!");
    }

    private static void teamSetSpawn(CommandSender sender) {
        String senderName = sender.getName();
        String leaderName = TeamUtils.getPlayerLeader(senderName);
        boolean status = senderName.equals(leaderName);
        if (status) {
            Player player = (Player) sender;
            String worldName = player.getWorld().getName();
            List<String> black = TeamPlugin.plugin.getConfig().getStringList("black-world");
            status = black.contains(worldName);
            if (status) {
                String message = ChatColor.RED + "不允许在本世界设置复活点";
                sender.sendMessage(message);
            } else {
                Location location = player.getLocation();
                TeamUtils.setTeamSpawn(leaderName, location);
                String message = ChatColor.GREEN + "复活点设置成功";
                sender.sendMessage(message);
            }
        }
    }

    static void teamMessage(CommandSender sender, String[] args) {
        if (args.length > 0) {
            boolean status;

            String senderName = sender.getName();
            String leaderName = TeamUtils.getPlayerLeader(senderName);

            if (leaderName != null) {
                StringBuilder msgBuilder = new StringBuilder();
                status = senderName.equals(leaderName);

                if (status) {
                    msgBuilder.append(ChatColor.RED);
                    msgBuilder.append("<队长>");
                }
                msgBuilder.append(ChatColor.GOLD);
                msgBuilder.append("<").append(senderName).append("> ");
                for (String arg : args) {
                    msgBuilder.append(arg);
                    msgBuilder.append(" ");
                }
                String msg = msgBuilder.toString();
                TeamUtils.setTeamMessage(leaderName, msg);
            }
        }
    }

    private static boolean teamAgree(CommandSender sender, String[] args) {
        if (args.length > 1) {
            String senderName = sender.getName();
            String leaderName = TeamUtils.getPlayerLeader(senderName);
            boolean status = senderName.equals(leaderName);
            if (status) {
                String msg;

                int teamLevel = TeamUtils.getTeamLevel(leaderName);
                int teamSize = TeamUtils.getTeamMate(senderName).size();
                int teamLimit = TeamUtils.getTeamLimit(teamLevel);

                status = teamSize < teamLimit;
                if (status) {
                    List<String> requestList = TeamUtils.getRequestList(leaderName);
                    Player player = TeamPlugin.plugin.getServer().getPlayer(args[1]);
                    if (player != null) args[1] = player.getName();
                    else {
                        msg = "玩家 " + args[1] + " 没有申请加入队伍";
                        sender.sendMessage(ChatColor.RED + msg);
                        return true;
                    }
                    status = requestList.contains(args[1]);
                    if (status) {
                        String getLeaderName = TeamUtils.getPlayerLeader(args[1]);
                        if (getLeaderName != null) {
                            msg = ChatColor.RED + "玩家已加入其他队伍!";
                            sender.sendMessage(msg);
                            return true;
                        }
                        status = player.isOnline();
                        if (status) {
                            int maxHealth = 18 + 2 * teamLevel;
                            TeamUtils.setPlayerList(args[1], true);
                            String path = "config.team.growhealth";
                            status = TeamPlugin.plugin.getConfig().getBoolean(path, true);
                            if (status) player.setMaxHealth(maxHealth);
                        }
                        TeamUtils.setTeamMate(senderName, args[1], true);
                        TeamUtils.setPlayerLeader(args[1], senderName);
                        TeamUtils.setPlayerJoinTime(args[1]);
                        TeamUtils.setRequestList(leaderName, args[1], false);
                        TeamPlugin.plugin.saveConfig();
                        msg = "同意入队申请成功";
                        sender.sendMessage(ChatColor.GREEN + msg);
                        msg = "玩家 " + args[1] + " 加入了队伍";
                        TeamUtils.setTeamMessage(senderName, ChatColor.GREEN + msg);
                    }
                } else {
                    msg = "您的队伍已达最大人数";
                    sender.sendMessage(ChatColor.RED + msg);
                }
            }
        }
        return true;
    }

    private static boolean teamCreate(CommandSender sender) {
        String msg;
        String senderName = sender.getName();
        String leaderName = TeamUtils.getPlayerLeader(senderName);

        boolean status = leaderName != null;
        if (status) {
            String teamName = TeamUtils.getPlayerLeader(senderName);
            msg = "你已经是 " + teamName + " 队伍的成员";
            sender.sendMessage(ChatColor.RED + msg);
        } else {
            status = sender.hasPermission("team.create");
            if (status) {
                TeamUtils.setTeamLevel(senderName, 1);
                TeamUtils.setTeamMate(senderName, senderName, true);
                TeamUtils.setPlayerJoinTime(senderName);
                TeamUtils.setPlayerList(senderName, true);
                TeamUtils.setPlayerLeader(senderName, senderName);
                msg = "队伍创建成功";
                sender.sendMessage(ChatColor.GREEN + msg);
                TeamPlugin.plugin.saveConfig();
            }
        }
        return true;
    }

    private static boolean teamDissolve(CommandSender sender) {
        String senderName = sender.getName();
        String leaderName = TeamUtils.getPlayerLeader(senderName);
        boolean status = senderName.equals(leaderName);
        if (status) {
            String getName;
            String path;
            String msg = "队伍已被队长 " + senderName + " 解散";

            List<Player> onlineList = TeamUtils.getTeamMateOnline(senderName);
            for (Player onlinePlayer : onlineList) {
                getName = onlinePlayer.getName();
                TeamUtils.setPlayerList(getName, false);
                path = "config.team.growhealth";
                status = TeamPlugin.plugin.getConfig().getBoolean(path, true);
                if (status) onlinePlayer.setMaxHealth(20);
                onlinePlayer.sendMessage(ChatColor.RED + msg);
            }
            List<String> memberList = TeamUtils.getTeamMate(senderName);
            for (String memberName : memberList) {
                path = "players." + memberName;
                TeamPlugin.plugin.getConfig().set(path, null);
            }
            path = "teams." + senderName;
            TeamPlugin.plugin.getConfig().set(path, null);

            msg = "队伍已解散";
            sender.sendMessage(ChatColor.RED + msg);
            TeamPlugin.plugin.saveConfig();
        }
        return true;
    }

    private static void teamEffect(CommandSender sender) {
        String senderName = sender.getName();
        String leaderName = TeamUtils.getPlayerLeader(senderName);
        boolean status = senderName.equals(leaderName);
        if (status) {
            int teamLevel = TeamUtils.getTeamLevel(leaderName);
            if (teamLevel > 1) {
                long skillTime = TeamUtils.getTeamSkillTime(leaderName);
                long currentTime = System.currentTimeMillis() / 1000;
                long coolDown = 180;
                status = currentTime > skillTime + coolDown;
                if (status) {
                    String path = "teams." + leaderName + ".effect";
                    int taskId = TeamPlugin.tempConfig.getInt(path);
                    if (taskId > 0) {
                        TeamUtils.setTeamSkillTime(leaderName);
                        TeamPlugin.tempConfig.set(path, null);
                        TeamPlugin.plugin.getServer().getScheduler().cancelTask(taskId);
                        String message = ChatColor.GREEN + "团队光环 已关闭";
                        sender.sendMessage(message);
                    } else {
                        List<Player> mateOnline = TeamUtils.getTeamMateOnline(leaderName);
                        int onlineSize = mateOnline.size();
                        status = onlineSize > 1;
                        if (status) {
                            TeamUtils.setTeamSkillTime(leaderName);
                            TeamEffectThread thread = new TeamEffectThread(leaderName);
                            taskId = TeamPlugin.plugin.getServer().
                                    getScheduler().
                                    runTaskTimerAsynchronously(TeamPlugin.plugin, thread, 0, 20 * 90).
                                    getTaskId();
                            TeamPlugin.tempConfig.set(path, taskId);
                            String message = ChatColor.GREEN + "团队光环 已启动!";
                            sender.sendMessage(message);
                        } else {
                            String message = ChatColor.RED + "在线队友不足, 团队光环 启动失败";
                            sender.sendMessage(message);
                        }
                    }
                } else {
                    long leastTime = skillTime + coolDown - currentTime;
                    String message = ChatColor.RED + "技能冷却剩余 " + leastTime + " 秒";
                    sender.sendMessage(message);
                }
            }
        }
    }

    private static boolean teamFlag(CommandSender sender) {
        String senderName = sender.getName();
        String leaderName = TeamUtils.getPlayerLeader(senderName);
        boolean status = senderName.equals(leaderName);
        if (status) {
            int teamLevel = TeamUtils.getTeamLevel(leaderName);
            status = teamLevel > 1;
            if (status) {
                String msg;
                TeleportThread task;

                String teamName = TeamUtils.getPlayerLeader(senderName);
                Player senderPlayer = TeamPlugin.plugin.getServer().getPlayerExact(senderName);

                List<Player> memberList = TeamUtils.getTeamMateOnline(teamName);
                memberList.remove(senderPlayer);

                for (Player memberPlayer : memberList) {
                    msg = ChatColor.GREEN + "队长设立了一个传送旗帜";
                    memberPlayer.sendMessage(msg);
                    msg = ChatColor.GREEN + "3秒后开始传送到队长...";
                    memberPlayer.sendMessage(msg);
                    task = new TeleportThread(memberPlayer, senderPlayer);
                    TeamPlugin.plugin.getServer().getScheduler().runTaskLater(TeamPlugin.plugin, task, 60);
                }
                msg = ChatColor.GREEN + "你设立了一个传送旗帜...";
                sender.sendMessage(msg);
                msg = ChatColor.GREEN + "3秒后开始传送队友...";
                sender.sendMessage(msg);
            }
        }
        return true;
    }

    private static boolean teamInfo(CommandSender sender) {
        String msgString;
        String[] msgs = {};

        ArrayList<String> msgList = new ArrayList<String>();

        String senderName = sender.getName();
        String leaderName = TeamUtils.getPlayerLeader(senderName);
        boolean status = leaderName == null;
        if (status) {
            msgString = ChatColor.GOLD + "您好, 您还不属于任何队伍!";
            msgList.add(msgString);
            msgString = ChatColor.GOLD + "您可用的命令如下:";
            msgList.add(msgString);
            msgString = "";
            msgList.add(msgString);
            status = sender.hasPermission("team.create");
            if (status) {
                msgString = ChatColor.GOLD + "命令 /team create         创建属于您的队伍";
                msgList.add(msgString);
            }
            msgString = ChatColor.GOLD + "命令 /team join [abcd]    申请加入 abcd 的队伍";
            msgList.add(msgString);

            msgs = msgList.toArray(msgs);
            sender.sendMessage(msgs);
        } else {
            status = senderName.equals(leaderName);
            if (status) {
                int teamLevel = TeamUtils.getTeamLevel(leaderName);
                msgString = ChatColor.GOLD + "您好, 尊敬的队长大人!";
                msgList.add(msgString);
                msgString = ChatColor.GOLD + "您可用的命令如下:";
                msgList.add(msgString);
                msgString = "";
                msgList.add(msgString);
                msgString = ChatColor.GOLD + "命令 /team levelup          升级队伍";
                msgList.add(msgString);
                if (teamLevel > 3) {
                    msgString = ChatColor.GOLD + "命令 /team prefix [abcd]    设定队伍称号";
                    msgList.add(msgString);
                }
                if (teamLevel > 1) {
                    msgString = ChatColor.GOLD + "命令 /team flag             召集队员";
                    msgList.add(msgString);
                    msgString = ChatColor.GOLD + "命令 /team effect           队长技能 团队光环";
                    msgList.add(msgString);
                }
                msgString = ChatColor.GOLD + "命令 /team heal             队长技能 生命之光";
                msgList.add(msgString);
                msgString = ChatColor.GOLD + "命令 /team agree [abcd]     同意玩家 abcd 的入队申请";
                msgList.add(msgString);
                msgString = ChatColor.GOLD + "命令 /team remove [abcd]    将玩家 abcd 移出队伍";
                msgList.add(msgString);
                msgString = ChatColor.GOLD + "命令 /team list             查看队伍信息";
                msgList.add(msgString);
                msgString = ChatColor.GOLD + "命令 /team chest            打开队伍箱子";
                msgList.add(msgString);
                msgString = ChatColor.GOLD + "命令 /team setspawn         设置队伍复活点";
                msgList.add(msgString);
                msgString = ChatColor.GOLD + "命令 /team spawn            传送到队伍复活点";
                msgList.add(msgString);
                msgString = ChatColor.GOLD + "命令 /team dissolve         解散队伍";
                msgList.add(msgString);
                msgString = ChatColor.GOLD + "命令 /tm [abcd]             队内密语";
                msgList.add(msgString);
                msgs = msgList.toArray(msgs);
                sender.sendMessage(msgs);
            } else {
                msgString = ChatColor.GOLD + "您好, 尊敬的冒险队伍成员!";
                msgList.add(msgString);
                msgString = ChatColor.GOLD + "您可用的命令如下:";
                msgList.add(msgString);
                msgString = "";
                msgList.add(msgString);
                msgString = ChatColor.GOLD + "命令 /team leader    传送至队长";
                msgList.add(msgString);
                msgString = ChatColor.GOLD + "命令 /team list      查看队伍信息";
                msgList.add(msgString);
                msgString = ChatColor.GOLD + "命令 /team quit      离开队伍";
                msgList.add(msgString);
                msgString = ChatColor.GOLD + "命令 /tm [abc]      队内密语";
                msgList.add(msgString);
                msgString = ChatColor.GOLD + "命令 /team chest     打开队伍箱子";
                msgList.add(msgString);
                msgString = ChatColor.GOLD + "命令 /team spawn     传送到队伍复活点";
                msgList.add(msgString);
                msgs = msgList.toArray(msgs);
                sender.sendMessage(msgs);
            }
        }
        return true;
    }

    private static boolean teamJoin(CommandSender sender, String[] args) {
        if (args.length > 1) {
            String senderName = sender.getName();
            String leaderName = TeamUtils.getPlayerLeader(senderName);
            if (leaderName != null) {
                sender.sendMessage(ChatColor.RED + "你已经是 " + leaderName + " 队伍的成员");
                return true;
            }
            Player player = TeamPlugin.plugin.getServer().getPlayer(args[1]);
            if (player != null) {
                args[1] = player.getName();
                int teamLevel = TeamUtils.getTeamLevel(args[1]);
                if (teamLevel < 1) {
                    sender.sendMessage(ChatColor.RED + "玩家 " + args[1] + " 不是队长");
                    return true;
                }
                TeamUtils.setRequestList(args[1], senderName, true);
                TeamUtils.setTeamMessage(args[1], ChatColor.GREEN + "玩家 " + senderName + " 申请加入队伍");
                sender.sendMessage(ChatColor.GREEN + "申请加入 " + args[1] + " 的队伍成功");
            } else sender.sendMessage(ChatColor.RED + "玩家 " + args[1] + " 不在线");
        }
        return true;
    }

    private static boolean teamLeader(CommandSender sender) {
        String senderName = sender.getName();
        String leaderName = TeamUtils.getPlayerLeader(senderName);
        boolean status = leaderName != null && !senderName.equals(leaderName);
        if (status) {
            String msg;

            Server getServer = TeamPlugin.plugin.getServer();
            Player leaderPlayer = getServer.getPlayerExact(leaderName);
            status = leaderPlayer != null && leaderPlayer.isOnline();
            if (status) {
                msg = ChatColor.GREEN + "3秒后开始传送...";
                sender.sendMessage(msg);
                msg = ChatColor.GREEN + "队友 " + senderName + " 将传送到你身边...";
                leaderPlayer.sendMessage(msg);
                Player senderPlayer = getServer.getPlayerExact(senderName);
                TeleportThread task = new TeleportThread(senderPlayer, leaderPlayer);
                TeamPlugin.plugin.getServer().getScheduler().runTaskLater(TeamPlugin.plugin, task, 60);
            } else {
                msg = ChatColor.RED + "队长不在线";
                sender.sendMessage(msg);
            }
        }
        return true;
    }

    private static boolean teamLevelUp(CommandSender sender) {
        String senderName = sender.getName();
        String leaderName = TeamUtils.getPlayerLeader(senderName);
        boolean status = senderName.equals(leaderName);
        if (status) {
            String msg;

            List<String> memberList = TeamUtils.getTeamMate(leaderName);
            int teamLevel = TeamUtils.getTeamLevel(leaderName);
            int teamMembers = memberList.size();
            int teamLimit = TeamUtils.getTeamLimit(teamLevel);
            status = teamMembers < teamLimit;
            if (!status) {
                int memberOnline;
                double expNeed = TeamUtils.getTeamExpNeed(teamLevel);
                boolean b = true;

                for (String memberName : memberList) {
                    memberOnline = TeamUtils.getPlayerOnlineTime(memberName);
                    if (memberOnline < expNeed) b = false;
                }
                if (b) {
                    teamLevel = teamLevel + 1;
                    TeamUtils.setTeamLevel(leaderName, teamLevel);
                    msg = ChatColor.GREEN + "恭喜您, 队伍升级成功!";
                    sender.sendMessage(msg);
                } else {
                    msg = ChatColor.RED + "部分队员经验不满无法升级";
                    sender.sendMessage(msg);
                }
            } else {
                msg = ChatColor.RED + "队伍人数不满无法升级";
                sender.sendMessage(msg);
            }
        }

        return false;
    }

    private static boolean teamList(CommandSender sender) {
        String senderName = sender.getName();
        String leaderName = TeamUtils.getPlayerLeader(senderName);
        boolean status = leaderName != null;
        if (status) {
            Player memberPlayer;
            String onlineString;
            String path;
            int onlineTime;

            Server getServer = TeamPlugin.plugin.getServer();
            ArrayList<String> msgList = new ArrayList<String>();

            String msgString = ChatColor.GOLD + "信息:";
            msgList.add(msgString);
            int teamLevel = TeamUtils.getTeamLevel(leaderName);
            msgString = ChatColor.GOLD + "    - Level    " + teamLevel;
            msgList.add(msgString);
            double teamExpNeed = TeamUtils.getTeamExpNeed(teamLevel);
            msgString = ChatColor.GOLD + "    - Need     " + teamExpNeed;
            msgList.add(msgString);
            int teamLimit = TeamUtils.getTeamLimit(teamLevel);
            msgString = ChatColor.GOLD + "    - Limit    " + teamLimit;
            msgList.add(msgString);
            msgString = ChatColor.GOLD + "队长:";
            msgList.add(msgString);
            msgString = ChatColor.RED + "    - " + leaderName;
            msgList.add(msgString);
            msgString = ChatColor.GOLD + "队员:";
            msgList.add(msgString);
            List<String> memberList = TeamUtils.getTeamMate(leaderName);

            for (String memberName : memberList) {
                path = "players." + memberName + ".online";
                onlineTime = TeamPlugin.plugin.getConfig().getInt(path);

                memberPlayer = getServer.getPlayerExact(memberName);
                status = memberPlayer != null && memberPlayer.isOnline();
                if (status) onlineString = "online";
                else onlineString = ChatColor.RED + "offline";

                msgString = ChatColor.GREEN + "    - " + memberName + "    " + onlineTime + " min" + "    "
                        + onlineString;
                msgList.add(msgString);
            }

            List<String> requestList = TeamUtils.getRequestList(leaderName);
            status = requestList.size() > 0;

            if (status) {
                msgString = ChatColor.GOLD + "申请:";
                msgList.add(msgString);
                for (String requestName : requestList) {
                    msgString = ChatColor.GREEN + "    - " + requestName;
                    msgList.add(msgString);
                }
            }
            String[] msgs = {};
            msgs = msgList.toArray(msgs);
            sender.sendMessage(msgs);
        }
        return false;
    }

    private static void teamPrefix(CommandSender sender, String[] args) {
        if (args.length > 1) {
            String senderName = sender.getName();
            String leaderName = TeamUtils.getPlayerLeader(senderName);
            boolean status = senderName.equals(leaderName);
            if (status) {
                int teamLevel = TeamUtils.getTeamLevel(leaderName);
                if (teamLevel > 3) {
                    String msg;

                    long coolDown = 300;
                    long skillTime = TeamUtils.getTeamSkillTime(leaderName);
                    long currentTime = System.currentTimeMillis() / 1000;
                    status = currentTime > skillTime + coolDown;
                    if (status) {
                        int length = args[1].length();
                        if (length < 12) {
                            String prefix = args[1].replace("&", "§");
                            TeamUtils.setTeamPrefix(leaderName, prefix);
                            TeamPlugin.plugin.saveConfig();
                            TeamUtils.setTeamSkillTime(leaderName);
                            msg = ChatColor.GREEN + "队伍称号 " + args[1] + " 设置成功";
                            sender.sendMessage(msg);
                        } else {
                            msg = ChatColor.RED + "太长了, 麻烦弄短点";
                            sender.sendMessage(msg);
                        }
                    } else {
                        long leastTime = skillTime + coolDown - currentTime;
                        msg = ChatColor.RED + "技能冷却剩余 " + leastTime + " 秒";
                        sender.sendMessage(msg);
                    }
                }
            }
        }
    }

    private static boolean teamQuit(CommandSender sender) {
        String msg;
        String senderName = sender.getName();
        String leaderName = TeamUtils.getPlayerLeader(senderName);
        boolean status = leaderName != null && !leaderName.equals(senderName);
        if (status) {
            String path = "config.team.growhealth";
            status = TeamPlugin.plugin.getConfig().getBoolean(path, true);
            if (status) TeamPlugin.plugin.getServer().getPlayerExact(senderName).setMaxHealth(20);
            String teamName = TeamUtils.getPlayerLeader(senderName);
            TeamUtils.setTeamMate(teamName, senderName, false);
            TeamUtils.setPlayerList(senderName, false);
            TeamPlugin.plugin.saveConfig();
            msg = ChatColor.RED + "你退出了" + teamName + "的队伍 ";
            sender.sendMessage(msg);
            msg = ChatColor.RED + "玩家 " + senderName + " 退出了队伍 ";
            TeamUtils.setTeamMessage(teamName, msg);
        }
        return true;
    }

    private static boolean teamRemove(CommandSender sender, String[] args) {
        if (args.length > 1) {
            String senderName = sender.getName();
            String leaderName = TeamUtils.getPlayerLeader(senderName);
            boolean status = senderName.equals(leaderName) && !senderName.equals(args[1]);
            if (status) {
                String msg;
                String argLeader = TeamUtils.getPlayerLeader(args[1]);
                status = leaderName.equals(argLeader);
                if (status) {
                    TeamUtils.setTeamMate(senderName, args[1], false);
                    TeamPlugin.plugin.saveConfig();
                    msg = "你已被移出了 " + senderName + " 的队伍";

                    Player player = TeamPlugin.plugin.getServer().getPlayerExact(args[1]);
                    status = player != null && player.isOnline();
                    if (status) {
                        TeamUtils.setPlayerList(args[1], false);
                        String path = "config.team.growhealth";
                        status = TeamPlugin.plugin.getConfig().getBoolean(path, true);
                        if (status) player.setMaxHealth(20);
                        player.sendMessage(ChatColor.RED + msg);
                    }

                    msg = "玩家 " + args[1] + " 退出了队伍 ";
                    TeamUtils.setTeamMessage(senderName, ChatColor.RED + msg);
                } else {
                    msg = "玩家 " + args[1] + " 不是队伍成员";
                    sender.sendMessage(ChatColor.RED + msg);
                }
            }
        }
        return true;
    }

    private static boolean teamSkill(CommandSender sender, String skillName) {
        String senderName = sender.getName();
        String leaderName = TeamUtils.getPlayerLeader(senderName);
        boolean status = senderName.equals(leaderName);
        if (status) {
            String msg;

            String path = "config.team.skillcooldown";
            int coolDown = TeamPlugin.plugin.getConfig().getInt(path, 150);

            long skillTime = TeamUtils.getTeamSkillTime(leaderName);
            long currentTime = System.currentTimeMillis() / 1000;
            status = currentTime > skillTime + coolDown;
            if (status) {
                int teamLevel = TeamUtils.getTeamLevel(leaderName);
                List<Player> mateNearby = TeamUtils.getTeamMateNearby(leaderName);
                int mateSize = mateNearby.size();
                if (mateSize > 1) {
                    status = skillName.equals("heal");
                    if (status) {
                        TeamSkill.skillHeal(mateNearby, teamLevel);
                        TeamUtils.setTeamSkillTime(leaderName);
                    }
                } else {
                    msg = ChatColor.RED + "身边没有队员, 无法发动技能";
                    sender.sendMessage(msg);
                }
            } else {
                long leastTime = skillTime + coolDown - currentTime;
                msg = ChatColor.RED + "技能冷却剩余 " + leastTime + " 秒";
                sender.sendMessage(msg);
            }
        }
        return false;
    }

    private static class TeamEffectThread implements Runnable {
        String leaderName = null;

        public TeamEffectThread(String s) {
            leaderName = s;
        }

        @Override
        public void run() {
            List<Player> nearby = TeamUtils.getTeamMateNearby(leaderName);
            int size = nearby.size();
            if (size > 1) {
                Collection<PotionEffect> effects = getEffectList(leaderName);
                String s = ChatColor.GREEN + "你被 团队光环 笼罩了!";
                for (Player player : nearby) {
                    player.addPotionEffects(effects);
                    player.sendMessage(s);
                }
            } else {
                String s = ChatColor.RED + "身边没有队友, 无法发动团队光环!";
                nearby.get(0).sendMessage(s);
            }
        }

        private Collection<PotionEffect> getEffectList(String leaderName) {
            Collection<PotionEffect> effectList = new ArrayList<PotionEffect>();
            int teamLevel = TeamUtils.getTeamLevel(leaderName);
            if (teamLevel > 5) {
                PotionEffect effect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1200, 2);
                effectList.add(effect);
            } else if (teamLevel > 3) {
                PotionEffect effect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1200, 1);
                effectList.add(effect);
            } else if (teamLevel > 1) {
                PotionEffect effect = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1200, 0);
                effectList.add(effect);
            }
            return effectList;
        }
    }
}
