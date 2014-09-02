package com.mengcraft.team.thread;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class TeleportThread implements Runnable{
    private final Entity entity;
    private final Location location;

    public TeleportThread(Entity entity, Location location) {
        this.entity = entity;
        this.location = location;
    }

    public TeleportThread(Entity from, Entity to) {
        this.entity = from;
        this.location = to.getLocation();
    }

    @Override
    public void run() {
        entity.teleport(location);
    }
}
