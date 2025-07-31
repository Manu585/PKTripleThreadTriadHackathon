package me.manu.vortexstep.ability;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.region.RegionProtection;
import me.manu.vortexstep.listener.AbilityListener;
import me.manu.vortexstep.manager.StepController;
import me.manu.vortexstep.util.PacketUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class VortexStep extends AirAbility implements AddonAbility {
    private static final String PATH = "ExtraAbilities.Manu.Air.VortexStep.";

    @Attribute(Attribute.COOLDOWN)
    private final long cooldown;
    @Attribute(Attribute.DURATION)
    private final long duration;
    @Attribute(Attribute.RANGE)
    private final double range;

    private final double minStepDistance;

    private final int radius;
    private final int maxSteps;
    private final long stepTimeOut;
    private final Entity target;

    private Location center;
    private StepController controller;
    private boolean initialized;

    private boolean tornadoActive = false;
    private long tornadoStart = 0;
    private Vortex tornadoEffect;

    public VortexStep(Player player) {
        super(player);

        this.cooldown = ConfigManager.getConfig().getLong(PATH + "Cooldown");
        this.duration = ConfigManager.getConfig().getLong(PATH + "Duration");
        this.maxSteps = ConfigManager.getConfig().getInt(PATH + "MaxSteps");
        this.range = ConfigManager.getConfig().getDouble(PATH + "Range");
        this.radius = ConfigManager.getConfig().getInt(PATH + "Radius");
        this.minStepDistance = ConfigManager.getConfig().getDouble(PATH + "MinStepDistance");
        this.stepTimeOut = 5_000;

        this.target = GeneralMethods.getTargetedEntity(this.player, range);

        if (this.target == null) return;
        if (RegionProtection.isRegionProtected(this.player, this.target.getLocation(), this)) return;

        start();
    }

    @Override
    public void progress() {
        if (!bPlayer.canBendIgnoreBinds(this)) {
            remove();
            return;
        }

        if (!initialized) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.MASTER, 2, 0.5f);
            this.center = target.getLocation().clone();
            this.controller = new StepController(player, center, radius, maxSteps, minStepDistance, stepTimeOut);
            this.initialized = true;
        }

        if (!tornadoActive) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 10, true, false, false));
        }

        // Check if target leaves bounding box or step timeout
        if (target.getLocation().distance(center) > radius + 0.5 || controller.isTimedOut()) {
            fail();
            return;
        }

        if (!controller.isComplete()) {
            controller.showCurrentStep();
            if (player.isSneaking() && controller.canAdvance(10.0)) {
                controller.advance();
            }
            return;
        }

        if (!tornadoActive) {
            tornadoActive = true;
            tornadoStart = System.currentTimeMillis();
            PacketUtil.removeSlime(player);

            tornadoEffect = new Vortex(target, 0.3, 8.0, 40, 0.5, 6, 0.8, 0.03, 0.6, AirAbility.getAirbendingParticles().getParticle(), 2, .1, .1, .1);
            tornadoEffect.start();

            player.removePotionEffect(PotionEffectType.SLOWNESS);
        }

        long elapse = System.currentTimeMillis() - tornadoStart;
        if (elapse < duration) {
            tornadoEffect.update();
        } else {
            remove();
        }
    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return this.cooldown;
    }

    @Override
    public String getName() {
        return "TornadoStep";
    }

    @Override
    public String getDescription() {
        return "CORNBALL";
    }

    @Override
    public Location getLocation() {
        return this.player != null ? this.player.getLocation() : null;
    }

    @Override
    public void load() {
        ConfigManager.getConfig().addDefault(PATH + "Cooldown", 1000);
        ConfigManager.getConfig().addDefault(PATH + "Duration", 8000);
        ConfigManager.getConfig().addDefault(PATH + "Range", 5);
        ConfigManager.getConfig().addDefault(PATH + "Radius", 5);
        ConfigManager.getConfig().addDefault(PATH + "MaxSteps", 3);
        ConfigManager.getConfig().addDefault(PATH + "MinStepDistance", 8);
        ConfigManager.defaultConfig.save();

        Bukkit.getServer().getPluginManager().registerEvents(new AbilityListener(), ProjectKorra.plugin);
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(new AbilityListener());
        PacketUtil.removeSlime(player);
    }

    @Override
    public void remove() {
        bPlayer.addCooldown(this);
        initialized = false;
        PacketUtil.removeSlime(player);
        super.remove();
    }

    @Override
    public String getAuthor() {
        return "Manu";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    private void fail() {
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1, true, false, false));
        remove();
    }

    public long getDuration() {
        return duration;
    }

    public double getRange() {
        return range;
    }

    public int getRadius() {
        return radius;
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public long getStepTimeOut() {
        return stepTimeOut;
    }

    public Entity getTarget() {
        return target;
    }

    public Location getCenter() {
        return center;
    }

    public StepController getController() {
        return controller;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
