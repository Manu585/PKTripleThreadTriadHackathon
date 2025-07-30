package me.manu.vortexstep.ability;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class Vortex {
    private final Location center;
    private final Entity target;
    private final double speed;
    private final int height;
    private final int radius;

    public Vortex(Location center, Entity target, double speed, int height, int radius) {
        this.center = center;
        this.target = target;
        this.speed = speed;
        this.height = height;
        this.radius = radius;
    }

    public void start() {

    }

    public void stop() {

    }

    public Location getCenter() {
        return center;
    }

    public Entity getTarget() {
        return target;
    }

    public double getSpeed() {
        return speed;
    }

    public int getHeight() {
        return height;
    }

    public int getRadius() {
        return radius;
    }
}
