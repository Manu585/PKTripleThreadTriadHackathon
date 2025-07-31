package me.manu.vortexstep.ability;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Vortex {
    private final List<Double> ringPhases = new ArrayList<>();

    private final Entity target;
    private final double minRadius, maxRadius;
    private final int heightLevels;
    private final double expandRate;
    private final double angularSpeed;
    private final double verticalSpacing;
    private final double phaseDelay;
    private final double wobbleAmp;
    private final Particle particle;
    private final int count;
    private final double offsetX, offsetY, offsetZ;

    private long startTime;

    /**
     * @param target entity to follow
     * @param minRadius inner radius
     * @param maxRadius maximum radius over lifetime
     * @param heightLevels number of horizontal rings
     * @param expandRate how quickly it widens (1/sec)
     * @param angularSpeed spin speed (rad/sec)
     * @param verticalSpacing blocks between rings
     * @param phaseDelay seconds each level lags behind bottom
     * @param wobbleAmp how much rings sway
     * @param particle particle type
     * @param count particles per ring
     * @param offsetX/Y/Z spawn offsets
     */
    public Vortex(Entity target, double minRadius, double maxRadius, int heightLevels, double expandRate, double angularSpeed, double verticalSpacing, double phaseDelay, double wobbleAmp, Particle particle, int count, double offsetX, double offsetY, double offsetZ) {
        this.target         = target;
        this.minRadius      = minRadius;
        this.maxRadius      = maxRadius;
        this.heightLevels   = heightLevels;
        this.expandRate     = expandRate;
        this.angularSpeed   = angularSpeed;
        this.verticalSpacing= verticalSpacing;
        this.phaseDelay     = phaseDelay;
        this.wobbleAmp      = wobbleAmp;
        this.particle       = particle;
        this.count          = count;
        this.offsetX        = offsetX;
        this.offsetY        = offsetY;
        this.offsetZ        = offsetZ;

        // randomize each ring's wobble phase
        Random rand = new Random();
        for (int i = 0; i < heightLevels; i++) {
            ringPhases.add(rand.nextDouble() * 2 * Math.PI);
        }
    }

    public void start() {
        this.startTime = System.currentTimeMillis();
    }

    public void update() {
        // follow the entity's feet
        Location tloc = target.getLocation();
        Location under = tloc.getBlock().getRelative(BlockFace.DOWN).getLocation();
        Location center = under.add(0.5, 1.0, 0.5);

        World world = center.getWorld();
        double elapsed = (System.currentTimeMillis() - startTime) / 1000.0;

        for (int lvl = 0; lvl < heightLevels; lvl++) {
            double frac = (double) lvl / (heightLevels - 1);

            // exponential widening: starts near minRadius, approaches maxRadius
            double radius = minRadius + (maxRadius - minRadius)
                    * (1 - Math.exp(-expandRate * elapsed * frac));

            // each ring spins, top rings lag by phaseDelay*frac seconds
            double ringTime = Math.max(0, elapsed - phaseDelay * frac);
            double angle    = angularSpeed * ringTime + frac * Math.PI;

            // wobble for natural sway
            double phase    = ringPhases.get(lvl);
            double wobbleX  = wobbleAmp * Math.sin(ringTime + phase);
            double wobbleZ  = wobbleAmp * Math.cos(ringTime + phase);

            double x = center.getX() + wobbleX + radius * Math.cos(angle);
            double z = center.getZ() + wobbleZ + radius * Math.sin(angle);
            double y = center.getY() + lvl * verticalSpacing;

            world.spawnParticle(
                    particle,
                    new Location(world, x, y, z),
                    count,
                    offsetX, offsetY, offsetZ,
                    0
            );
        }
    }
}