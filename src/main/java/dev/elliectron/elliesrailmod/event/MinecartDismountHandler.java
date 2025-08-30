package dev.elliectron.elliesrailmod.event;

import dev.elliectron.elliesrailmod.ElliesRailImprovements;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMountEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = ElliesRailImprovements.MODID)
public class MinecartDismountHandler {

    private static final int SEARCH_LATERAL_RADIUS = 4;
    private static final int SEARCH_HEIGHT = 2;

    private static final Map<UUID, Vec3> pendingTeleports = new HashMap<>();

    @SubscribeEvent
    public static void onEntityDismount(EntityMountEvent event) {
        if (event.isMounting()) return;

        Entity entity = event.getEntityBeingMounted();
        Entity rider = event.getEntityMounting();

        if (!(rider instanceof ServerPlayer player) || !(entity instanceof Minecart minecart)) {
            return;
        }

        Level level = player.level();
        Optional<BlockPos> bestDismountPos = findBestDismountPosition(level, minecart.blockPosition());

        if (bestDismountPos.isPresent()) {
            BlockPos dismountPos = bestDismountPos.get();
            Vec3 targetPos = new Vec3(
                    dismountPos.getX() + 0.5,
                    dismountPos.getY() + 0.5,
                    dismountPos.getZ() + 0.5
            );
            pendingTeleports.put(player.getUUID(), targetPos);
            level.getServer().execute(() -> {
                Vec3 teleportPos = pendingTeleports.remove(player.getUUID());
                if (teleportPos != null && player.isAlive()) {
                    player.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
                    player.resetFallDistance();
                }
            });
        }
    }

    private static Optional<BlockPos> findBestDismountPosition(Level level, BlockPos center) {
        BlockPos bestPos = null;
        double bestDistanceSquared = Double.MAX_VALUE;
        int bestPriority = Integer.MAX_VALUE;

        for (int x = -SEARCH_LATERAL_RADIUS; x <= SEARCH_LATERAL_RADIUS; x++) {
            for (int y = -SEARCH_HEIGHT; y <= SEARCH_HEIGHT; y++) {
                for (int z = -SEARCH_LATERAL_RADIUS; z <= SEARCH_LATERAL_RADIUS; z++) {
                    BlockPos checkPos = center.offset(x, y, z);
                    BlockState blockState = level.getBlockState(checkPos);

                    if (isAvoidedBlock(blockState)) continue;

                    BlockPos abovePos = checkPos.above();
                    if (!level.getBlockState(abovePos).isAir() ||
                            !level.getBlockState(abovePos.above()).isAir()) continue;

                    // top priority    (1)=yellow carpet (the edges of platforms are typically yellow)
                    //                  2 =oak planks (i assume it's a reasonable material, for players to build platforms with
                    // lowest priority (3)=any other valid block
                    int priority = getPriority(blockState);
                    double distanceSquared = center.distSqr(checkPos);

                    if (priority < bestPriority ||
                            (priority == bestPriority && distanceSquared < bestDistanceSquared)) {
                        bestPriority = priority;
                        bestDistanceSquared = distanceSquared;
                        bestPos = checkPos;
                    }
                }
            }
        }

        return Optional.ofNullable(bestPos);
    }

    private static int getPriority(BlockState blockState) {
        if (blockState.is(Blocks.YELLOW_CARPET)) return 1;
        String blockName = blockState.getBlock().toString();
        if (blockName.contains("_planks")) return 2;
        return 3;
    }

    private static boolean isAvoidedBlock(BlockState blockState) {
        String blockName = blockState.getBlock().toString();

        for (int i = 1; i <= 5; i++) {
            if (blockName.contains("rail_class_" + i) || blockName.contains("powered_rail_class_" + i)) {
                return true;
            }
        }

        return blockName.contains("linear_induction_rail") ||
                blockName.contains("rail_intersection") ||
                blockName.contains("track_circuit_switch") ||
                blockName.contains("track_circuit_signal") ||
                blockName.contains("track_circuit_atp") ||
                blockName.contains("track_circuit_ato") ||
                blockName.contains("elec_600v_bare") ||
                blockName.contains("elec_650v_bare") ||
                blockName.contains("elec_750v_bare") ||
                blockName.contains("elec_25kv_bare") ||
                blockName.contains("vvvfvcf_generator_bare");
    }
}