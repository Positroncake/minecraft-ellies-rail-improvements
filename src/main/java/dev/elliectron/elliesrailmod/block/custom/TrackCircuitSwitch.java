package dev.elliectron.elliesrailmod.block.custom;

import dev.elliectron.elliesrailmod.event.MinecartSpdLimHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("DuplicatedCode")
public class TrackCircuitSwitch extends DetectorRailBlock {
    private static final int TRACK_CLASS = 5;
    private static final double NATURAL_FRICTION_DECEL = 0.0008/20.0;
    public static final IntegerProperty SWITCH_STATE = IntegerProperty.create("switch_state", 0, 2);

    public TrackCircuitSwitch(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(SWITCH_STATE, 0)
                .setValue(POWERED, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SWITCH_STATE);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            // Get the maximum incoming redstone signal from all directions
            int maxSignal = getMaxIncomingSignal(level, pos, state);

            // Convert signal strength to track type
            int newSignalType = sigStrengthToSigType(maxSignal);

            // Update the track type if it changed
            if (state.getValue(SWITCH_STATE) != newSignalType) {
                level.setBlock(pos, state.setValue(SWITCH_STATE, newSignalType), 3);
            }
        }

        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }


    private int getMaxIncomingSignal(Level level, BlockPos pos, BlockState state) {
        int maxIncSignal = 0;

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);

            // Skip if the neighbor is the same type of rail to avoid self-powering
            if (neighborState.getBlock() instanceof TrackCircuitSwitch) {
                continue;
            }

            int signal = level.getSignal(neighborPos, direction);
            int directSignal = level.getDirectSignal(neighborPos, direction);
            maxIncSignal = Math.max(maxIncSignal, Math.max(signal, directSignal));
        }

        return maxIncSignal;
    }

    private int sigStrengthToSigType(int signalStrength) {
        if (signalStrength > 2) return 2;
        return signalStrength;
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        float[] spdLimsMps = Speeds.GetConventionalSpdLimsMps(TRACK_CLASS);

        if (level.isRaining()) {
            if (cart instanceof MinecartChest || cart instanceof MinecartFurnace || cart instanceof MinecartHopper
                    || cart instanceof MinecartTNT || cart instanceof MinecartCommandBlock || cart instanceof MinecartSpawner) {
                return spdLimsMps[0] / 20f; // Slowest when freight carts are on wet tracks
            }
            return spdLimsMps[1] / 20f; // Reduced speed for freight carts on dry tracks
        }
        if (cart instanceof MinecartChest || cart instanceof MinecartFurnace || cart instanceof MinecartHopper
                || cart instanceof MinecartTNT || cart instanceof MinecartCommandBlock || cart instanceof MinecartSpawner) {
            return spdLimsMps[2] / 20f; // Slower speed when passenger carts are on wet tracks
        }
        return spdLimsMps[3] / 20f; // Full speed for passenger carts on dry tracks
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof AbstractMinecart cart) {
            CompoundTag nbt = cart.getPersistentData();

            // if minecart has NBT "spd", it is running on class5+ track
            //noinspection StatementWithEmptyBody
            if (nbt.contains("spd")) {
                double debugAccel = 0.0;
                double debugSpd = 0.0;

                Vec3 rawMvmnt = cart.getDeltaMovement();
                if (rawMvmnt.x > 0.005) {
                    nbt.putInt("vv", 1);
                } else if (rawMvmnt.x < -0.005) {
                    nbt.putInt("vv", -1);
                } else if (rawMvmnt.z > 0.005) {
                    nbt.putInt("vv", 2);
                } else if (rawMvmnt.z < -0.005) {
                    nbt.putInt("vv", -2);
                }

                CompoundTag tag = cart.getPersistentData();
                var vv = tag.getInt("vv");
                var spdMpt = tag.getDouble("spd");
                spdMpt = Math.max(spdMpt-NATURAL_FRICTION_DECEL, 0);
                nbt.putDouble("spd", spdMpt);
                if (vv == 1) {
                    cart.setPos(cart.position().add(spdMpt/4.0, 0, 0));
                } else if (vv == -1) {
                    cart.setPos(cart.position().add(-spdMpt/4.0, 0, 0));
                } else if (vv == 2) {
                    cart.setPos(cart.position().add(0, 0, spdMpt/4.0));
                } else if (vv == -2) {
                    cart.setPos(cart.position().add(0, 0, -spdMpt/4.0));
                }

                printDebug(cart, String.format("%06.3f", nbt.getDouble("spd")*20) + " [" + nbt.getInt("vv") + "] " + String.format("%06.3f", debugSpd) + " + " + String.format("%.5f", debugAccel));
            }
            // if minecart does NOT have NBT "spd", it is running on class4- track
            else {
                // at this time, nothing shall happen - the minecraft continues moving as normal, per vanilla minecart physics/logic
            }
        }

        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 4);
        }
    }

    @Override
    public void tick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        // Update neighbors to trigger redstone recalculation
        level.updateNeighborsAt(pos, this);

        // Keep scheduling ticks while players are nearby or carts are on rail
        boolean hasNearbyPlayers = isPlayerHoldingItemNearby(level, pos, "elliesrailmod:signal_override", 6.0) ||
                isPlayerHoldingItemNearby(level, pos, "elliesrailmod:signal_switch_alternate", 6.0);
        boolean hasCartsOnRail = !level.getEntitiesOfClass(AbstractMinecart.class, new net.minecraft.world.phys.AABB(pos).inflate(0.5)).isEmpty();

        if (hasNearbyPlayers || hasCartsOnRail) {
            level.scheduleTick(pos, this, 4);
        }
    }

    private void printDebug(AbstractMinecart cart, String output) {
        for (var passenger : cart.getPassengers()) {
            if (passenger instanceof Player player) {
                player.sendSystemMessage(Component.literal(output));
            }
        }
    }

    private boolean holdingSignalById(AbstractMinecart cart, String id) {
        ResourceLocation signalId = ResourceLocation.parse(id);
        Item signalItem = BuiltInRegistries.ITEM.get(signalId);

        for (var passenger : cart.getPassengers()) {
            if (passenger instanceof Player player) {
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();
                if (mainHand.is(signalItem) || offHand.is(signalItem)) return true;
            }
        }
        return false;
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        if (blockAccess instanceof Level level) {
            // Check for players within 3-block radius holding relevant items
            boolean holdingOverride = isPlayerHoldingItemNearby(level, pos, "elliesrailmod:signal_override", 3.0);
            boolean holdingAlternate = isPlayerHoldingItemNearby(level, pos, "elliesrailmod:signal_switch_alternate", 3.0);

            // If player is holding override, emit signal strength 0
            if (holdingOverride) {
                return 0;
            }

            // If player is holding alternate, emit signal strength 1
            if (holdingAlternate) {
                return 1;
            }
        }

        // Otherwise, follow the normal switch state logic
        if (blockState.getValue(SWITCH_STATE) == 2) {
            return 1;
        }

        return 0;
    }

    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        if (blockAccess instanceof Level level) {
            // Check for players within 3-block radius holding relevant items
            boolean holdingOverride = isPlayerHoldingItemNearby(level, pos, "elliesrailmod:signal_override", 3.0);
            boolean holdingAlternate = isPlayerHoldingItemNearby(level, pos, "elliesrailmod:signal_switch_alternate", 3.0);

            // If player is holding override, emit signal strength 0
            if (holdingOverride) {
                return 0;
            }

            // If player is holding alternate, emit signal strength 1
            if (holdingAlternate) {
                return 1;
            }
        }

        // Otherwise, follow the normal switch state logic
        if (blockState.getValue(SWITCH_STATE) == 2) {
            return 1;
        }

        return 0;
    }

    private boolean isPlayerHoldingItemNearby(Level level, BlockPos pos, String itemId, double radius) {
        ResourceLocation signalId = ResourceLocation.parse(itemId);
        Item signalItem = BuiltInRegistries.ITEM.get(signalId);

        Vec3 blockCenter = Vec3.atCenterOf(pos);

        // Get all players within the radius
        var players = level.getEntitiesOfClass(Player.class,
                new net.minecraft.world.phys.AABB(pos).inflate(radius));

        for (Player player : players) {
            // Check if player is within exact radius (AABB inflate can be slightly larger)
            if (player.position().distanceTo(blockCenter) <= radius) {
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();
                if (mainHand.is(signalItem) || offHand.is(signalItem)) {
                    return true;
                }
            }
        }
        return false;
    }
}