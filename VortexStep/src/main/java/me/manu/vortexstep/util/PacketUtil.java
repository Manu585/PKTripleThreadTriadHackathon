package me.manu.vortexstep.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.EntityPositionData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityPositionSync;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class PacketUtil {
    private static final Map<UUID, FakeSlime> PLAYER_SLIMES = new HashMap<>();

    public static void showStepSlime(Player player, Location location) {
        FakeSlime slime = PLAYER_SLIMES.get(player.getUniqueId());

        if (slime == null) {
            slime = new FakeSlime(player, location);
            slime.spawn();
            PLAYER_SLIMES.put(player.getUniqueId(), slime);
        } else {
            slime.update(location);
        }
    }

    public static void removeSlime(Player player) {
        FakeSlime slime = PLAYER_SLIMES.remove(player.getUniqueId());

        if (slime != null) {
            WrapperPlayServerDestroyEntities destroy = new WrapperPlayServerDestroyEntities(new int[]{slime.getEntityId()});
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, destroy);
        }
    }

    private static class FakeSlime {
        private final int entityId = SpigotReflectionUtil.generateEntityId();
        private final UUID entityUuid = UUID.randomUUID();

        private final Player viewer;
        private Location location;

        public FakeSlime(Player viewer, Location location) {
            this.viewer = viewer;
            this.location = location;
        }

        public void spawn() {
            // Spawn Slime
            WrapperPlayServerSpawnEntity spawnSlime = new WrapperPlayServerSpawnEntity(
                    this.entityId,
                    this.entityUuid,
                    EntityTypes.SLIME,
                    SpigotConversionUtil.fromBukkitLocation(location),
                    0,
                    0,
                    new Vector3d(0, 0, 0)
            );

            PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, spawnSlime);
            PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, createMetadataPacket());
        }

        private WrapperPlayServerEntityMetadata createMetadataPacket() {
            byte flags = (byte) (0x20 | 0x40);

            List<EntityData<?>> data = List.of(
                    new EntityData<>(0, EntityDataTypes.BYTE, flags), // invisible & glowing
                    new EntityData<>(5, EntityDataTypes.BOOLEAN, true), // noGravity
                    new EntityData<>(15, EntityDataTypes.BYTE, (byte) 1), // noAI
                    new EntityData<>(16, EntityDataTypes.INT, 1) // small
            );

            return new WrapperPlayServerEntityMetadata(entityId, data);
        }

        public void update(Location location) {
            WrapperPlayServerEntityPositionSync teleportSlime = new WrapperPlayServerEntityPositionSync(
                    this.entityId,
                    new EntityPositionData(
                            new Vector3d(location.x(), location.y(), location.z()),
                            new Vector3d(0, 0, 0),
                            0,
                            0
                    ),
                    false
            );

            PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, teleportSlime);
        }

        public int getEntityId() {
            return entityId;
        }

        public UUID getEntityUuid() {
            return entityUuid;
        }

        public Player getViewer() {
            return viewer;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }
    }
}
