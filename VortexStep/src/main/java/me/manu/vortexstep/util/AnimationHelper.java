package me.manu.vortexstep.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class AnimationHelper {
    public static void drawParticleLine(Location start, Location end, Particle particle, double offsetX, double offsetY, double offsetZ, int count) {
        Vector dir = end.toVector().subtract(start.toVector());
        double length = dir.length();
        Vector step = dir.normalize().multiply(length / count);
        World world = start.getWorld();

        Location point = start.clone();
        for (int i = 0; i < count; i++) {
            world.spawnParticle(
                    particle,
                    point,
                    1,
                    offsetX,
                    offsetY,
                    offsetZ,
                    0
            );

            point.add(step);
        }
    }
}
