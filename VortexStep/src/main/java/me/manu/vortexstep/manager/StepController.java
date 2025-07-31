package me.manu.vortexstep.manager;

import me.manu.vortexstep.util.AnimationHelper;
import me.manu.vortexstep.util.PacketUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StepController {
    private final Player player;
    private final List<Step> steps;
    private final long timeout;
    private long lastStepTime;
    private int currentIndex;

    public StepController(Player player, Location center, int radius, int maxSteps, double minStepDistance, long timeout) {
        this.player = player;
        this.timeout = timeout;
        this.lastStepTime = System.currentTimeMillis();
        this.currentIndex = 0;
        this.steps = new ArrayList<>();

        initSteps(center, radius, maxSteps, minStepDistance);
    }

    private void initSteps(Location center, int radius, int maxSteps, double minStepDistance) {
        Random random = new Random();
        Location base = center.clone().add(0, 1, 0);
        int attempts = 0;

        while (steps.size() < maxSteps && attempts < maxSteps * 10) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double x = base.getX() + radius * Math.cos(angle);
            double z = base.getZ() + radius * Math.sin(angle);
            int y = base.getWorld().getHighestBlockYAt((int) x, (int) z) + 1;

            Location candidate = new Location(base.getWorld(), x + .5, y, z + .5);

            // Check distance to all existing
            boolean tooClose = false;
            for (Step step : steps) {
                if (step.getLocation().distance(candidate) < minStepDistance) {
                    tooClose = true;
                    break;
                }
            }

            if (!tooClose) {
                steps.add(new Step(candidate));
            } else {
                attempts++;
            }
        }

        // Fallback
        if (steps.size() < maxSteps) {
            double inc = 2 * Math.PI / maxSteps;
            for (int i = steps.size(); i < maxSteps; i++) {
                double angle = i * inc;
                double x = base.getX() + radius * Math.cos(angle);
                double z = base.getZ() + radius * Math.sin(angle);
                int y = base.getWorld().getHighestBlockYAt((int) x, (int) z) + 1;

                steps.add(new Step(new Location(base.getWorld(), x + .5, y, z + .5)));
            }
        }
    }

    public void showCurrentStep() {
        if (isComplete()) return;
        PacketUtil.showStepSlime(player, getCurrentStepLocation());
        steps.get(currentIndex).playStepAnimation(player);
    }

    public boolean isTimedOut() {
        return System.currentTimeMillis() - lastStepTime > timeout;
    }

    public boolean canAdvance(double maxAngleDeg) {
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        Vector to = steps.get(currentIndex).getLocation().toVector().subtract(eye.toVector()).normalize();
        double angle = Math.toDegrees(Math.acos(dir.dot(to)));

        return angle <= maxAngleDeg && !isTimedOut();
    }

    public void advance() {
        if (isComplete()) {
            PacketUtil.removeSlime(player);
        }

        Location from = player.getLocation().clone();
        Location to = getCurrentStepLocation().clone();

        Location here = player.getLocation();
        to.setYaw(here.getYaw());
        to.setPitch(here.getPitch());

        AnimationHelper.drawParticleLine(from.clone().add(0, 0.8, 0), to.clone().add(0, 0.8, 0), Particle.CLOUD, .1, .2, .1, 60);

        player.teleport(to);
        player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_AIR, SoundCategory.MASTER, 2, 2.0f);

        lastStepTime = System.currentTimeMillis();
        currentIndex++;
    }

    public boolean isComplete() {
        return currentIndex >= steps.size();
    }

    public Location getCurrentStepLocation() {
        return steps.get(currentIndex).getLocation();
    }
}
