package com.mengcraft.team;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class THUtils {

    public static void setChest() {

        Random random = new Random();

        World actWorld = TeamHunter.plugin.getServer().getWorld(TeamHunter.actWorldName);
        Chunk[] chunks = actWorld.getLoadedChunks();

        int i = random.nextInt(chunks.length);
        Chunk chunk = chunks[i];

        int x = random.nextInt(16);
        int y = random.nextInt(128);
        int z = random.nextInt(16);
        Block block = chunk.getBlock(x, y, z);
        Location loc = block.getLocation();
        loc = getLoc(loc);
        block = loc.getBlock();

        x = loc.getBlockX();
        y = loc.getBlockY();
        z = loc.getBlockZ();

        TeamHunter.actLoc = x + ";" + y + ";" + z;

        Material material[] = {
                Material.IRON_AXE,
                Material.IRON_BARDING,
                Material.IRON_BOOTS,
                Material.IRON_CHESTPLATE,
                Material.IRON_HELMET,
                Material.IRON_HOE,
                Material.IRON_LEGGINGS,
                Material.IRON_PICKAXE,
                Material.IRON_SPADE,
                Material.IRON_SWORD
        };

        i = random.nextInt(material.length);

        ItemStack itemStack = new ItemStack(material[i]);
        ItemMeta itemMeta = itemStack.getItemMeta();

        TeamHunter.actCode = UUID.randomUUID().toString();

        List<String> lore = new ArrayList<String>();
        lore.add(TeamHunter.actCode);
        lore.add("以上为任务道具唯一编号");
        lore.add("请马上将道具藏到包裹中");

        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);

        block.setType(Material.CHEST);
        Chest chest = (Chest) block.getState();
        chest.getInventory().addItem(itemStack);

        FileConfiguration getConfig = TeamHunter.plugin.getConfig();
        getConfig.set("hunter.act-world", TeamHunter.actWorldName);
        getConfig.set("hunter.act-code", TeamHunter.actCode);
        getConfig.set("hunter.act-loc", TeamHunter.actLoc);
        getConfig.set("hunter.act-point", TeamHunter.actPoint);
        TeamHunter.plugin.saveConfig();

        String s = ChatColor.GREEN + "新的宝箱出现在 " + TeamHunter.actWorldName + " 世界," +
                " 坐标 " + x + ", " + y + ", " + z + " 处!";
        TeamHunter.plugin.getServer().broadcastMessage(s);
    }

    private static Location getLoc(Location loc) {
        Block getBlock = loc.getBlock();
        boolean status = getBlock.isEmpty();
        if (status) {
            double y = loc.getY();
            while (true) {
                loc.setY(--y);
                getBlock = loc.getBlock();
                status = getBlock.isEmpty();
                if (status) continue;
                else loc.setY(++y);
                return loc;
            }
        }
        else {
            double y = loc.getY();
            while (true) {
                loc.setY(++y);
                getBlock = loc.getBlock();
                status = getBlock.isEmpty();
                if (status) return loc;
            }
        }
    }

    public static long getTeamPoint(String leaderName) {
        String path = "teams." + leaderName;
        return TeamHunter.plugin.getConfig().getLong(path);
    }

    public static void setTeamPoint(String leaderName, long point) {
        String path = "teams." + leaderName;
        TeamHunter.plugin.getConfig().set(path, point);
    }
}
