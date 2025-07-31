package me.manu.vortexstep.manager;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public record Step(Location location) {
    public void playStepAnimation(Player player) {
        player.spawnParticle(Particle.ELECTRIC_SPARK, location, 4, 0.1, 0.1, 0.1, 0);
    }

    public Location getLocation() {
        return location;
    }
}
