package me.manu.vortexstep.listener;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import me.manu.vortexstep.ability.VortexStep;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class AbilityListener implements Listener {

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();
        BendingPlayer bendingPlayer = BendingPlayer.getBendingPlayer(player);

        if (bendingPlayer == null) return;
        if (bendingPlayer.getBoundAbility() == null) return;

        if (bendingPlayer.getBoundAbility().equals(CoreAbility.getAbility(VortexStep.class))) {
            if (CoreAbility.hasAbility(player, VortexStep.class)) return;
            if (!bendingPlayer.canBend(CoreAbility.getAbility(VortexStep.class))) return;
            new VortexStep(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        BendingPlayer bendingPlayer = BendingPlayer.getBendingPlayer(player);

        if (bendingPlayer == null) return;
        if (bendingPlayer.getBoundAbility() == null) return;

        if (CoreAbility.getAbility(player, VortexStep.class) != null) {
            CoreAbility.getAbility(player, VortexStep.class).remove();
        }
    }
}
