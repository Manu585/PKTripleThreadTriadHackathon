package me.manu.vortexstep.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class StepController {
    private final Player player;
    private final List<Step> steps;
    private final long timeout;
    private long lastStepTime;
    private int currentIndex;

    public StepController(Player player, Location center, int radius, int maxSteps, long timeout) {
        this.player = player;
        this.timeout = timeout;
        this.lastStepTime = System.currentTimeMillis();
        this.currentIndex = 0;
        this.steps = new ArrayList<>();

        initSteps(center, radius, maxSteps);
    }

    private void initSteps(Location center, int radius, int maxSteps) {
        double angleInc = 360.0 / maxSteps;
        Location base = center.clone().add(0, 1, 0);

        for (int i = 0; i < maxSteps; i++) {
            double rad = Math.toRadians(i * angleInc);
            double x = base.getX() + radius * Math.cos(rad);
            double z = base.getZ() + radius * Math.sin(rad);
            int y = base.getWorld().getHighestBlockYAt((int) x, (int) z) + 1;

            Location loc = new Location(base.getWorld(), x + 0.5, y, z + 0.5);
            steps.add(new Step(loc));
        }
    }

    public void showCurrentStep() {
        Location location = getCurrentStepLocation();
        PacketUtil.showStepSlime(player, location);

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
        lastStepTime = System.currentTimeMillis();
        currentIndex++;

        if (isComplete()) {
            PacketUtil.removeSlime(player);
        }
    }

    public boolean isComplete() {
        return currentIndex >= steps.size();
    }

    public Location getCurrentStepLocation() {
        return steps.get(currentIndex).getLocation();
    }

}
