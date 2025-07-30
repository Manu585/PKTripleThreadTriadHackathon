package me.manu.vortexstep.ability;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.region.RegionProtection;
import me.manu.vortexstep.listener.AbilityListener;
import me.manu.vortexstep.util.AnimationHelper;
import me.manu.vortexstep.util.PacketUtil;
import me.manu.vortexstep.util.StepController;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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

    private final int radius;
    private final int maxSteps;
    private final long stepTimeOut;
    private final Entity target;

    private Location center;
    private StepController controller;
    private boolean initialized;

    public VortexStep(Player player) {
        super(player);

        this.cooldown = ConfigManager.getConfig().getLong(PATH + "Cooldown");
        this.duration = ConfigManager.getConfig().getLong(PATH + "Duration");
        this.maxSteps = ConfigManager.getConfig().getInt(PATH + "MaxSteps");
        this.range = ConfigManager.getConfig().getDouble(PATH + "Range");
        this.radius = ConfigManager.getConfig().getInt(PATH + "Radius");
        this.stepTimeOut = 5_000;

        this.target = GeneralMethods.getTargetedEntity(this.player, range);

        if (this.target == null) return;
        if (RegionProtection.isRegionProtected(this.player, this.target.getLocation(), this)) return;

        start();
    }

    @Override
    public void progress() {
        if (!bPlayer.canBend(this)) {
            remove();
            return;
        }

        if (!initialized) {
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.MASTER, 2, 0.5f);
            this.center = target.getLocation().clone();
            this.controller = new StepController(player, center, radius, maxSteps, stepTimeOut);
            this.initialized = true;
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 10, true, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 30, 5, true, false, false));

        // Check if target leaves bounding box or step timeout
        if (target.getLocation().distance(center) > radius + 0.5 || controller.isTimedOut()) {
            fail();
            return;
        }

        if (controller.isComplete()) {
            player.removePotionEffect(PotionEffectType.SLOWNESS);

            Block blockUnder = target.getLocation().getBlock().getRelative(BlockFace.DOWN);
            Location vortexCenter = blockUnder.getLocation().add(0.5, 1.0, 0.5);

            AnimationHelper.createVortex(vortexCenter, 10, 3, 16, AirAbility.getAirbendingParticles().getParticle(), 8, .1, .1, .1);

            return;
        }

        controller.showCurrentStep();

        if (player.isSneaking() && controller.canAdvance(15.0)) {
            Location from = player.getLocation().clone();
            Location to = controller.getCurrentStepLocation().clone();

            Location here = player.getLocation();
            to.setYaw(here.getYaw());
            to.setPitch(here.getPitch());

            Location fromRaised = from.clone().add(0, 0.5, 0);
            Location toRaised = to.clone().add(0, 0.5, 0);

            AnimationHelper.drawParticleLine(fromRaised, toRaised, Particle.CLOUD, .1, .2, .1, 60);

            player.teleport(to);
            controller.advance();
            player.playSound(player.getLocation(), Sound.ITEM_MACE_SMASH_AIR, SoundCategory.MASTER, 2, 2.0f);
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
        player.removePotionEffect(PotionEffectType.SLOWNESS);

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
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0, true, false, false));
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
