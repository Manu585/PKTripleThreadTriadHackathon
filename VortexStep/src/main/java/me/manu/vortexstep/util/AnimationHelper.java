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

    public static void createVortex(Location center, int height, double maxRadius, double speedAmp, Particle particle, int count, double offsetX, double offsetY, double offsetZ) {
        World world = center.getWorld();
        double t = (System.currentTimeMillis() % 10000L) / 1000.0 * speedAmp;

        for (int y = 0; y <= height; y++) {
            double frac = (double) y / height;
            double radius = maxRadius * frac;

            double angle  = t - frac * Math.PI * 2 * 3;

            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            Location spawn = center.clone().add(x, y * 0.5, z);
            world.spawnParticle(
                    particle,
                    spawn,
                    count,
                    offsetX, offsetY, offsetZ,
                    0
            );
        }
    }
}
