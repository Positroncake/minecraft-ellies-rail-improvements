package dev.elliectron.elliesrailmod.block.custom;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

@SuppressWarnings("DuplicatedCode")
public class TrackCircuitAtp extends DetectorRailBlock {
    private static final int TRACK_CLASS = 5;
    private static final double NATURAL_FRICTION_DECEL = 0.0008/20.0;
    public static final IntegerProperty ATP_CTRL = IntegerProperty.create("atp_signal", 0, 15);

    public TrackCircuitAtp(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ATP_CTRL, 0)
                .setValue(POWERED, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ATP_CTRL);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            // Get the maximum incoming redstone signal from all directions
            int maxSignal = getMaxIncomingSignal(level, pos, state);

            // Convert signal strength to track type
            int newSignalType = sigStrengthToSigType(maxSignal);

            // Update the track type if it changed
            if (state.getValue(ATP_CTRL) != newSignalType) {
                level.setBlock(pos, state.setValue(ATP_CTRL, newSignalType), 3);
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
            if (neighborState.getBlock() instanceof TrackCircuitSignal) {
                continue;
            }

            int signal = level.getSignal(neighborPos, direction);
            int directSignal = level.getDirectSignal(neighborPos, direction);
            maxIncSignal = Math.max(maxIncSignal, Math.max(signal, directSignal));
        }

        return maxIncSignal;
    }

    private int sigStrengthToSigType(int signalStrength) {
        return Math.min(signalStrength, 15);
//      public static final double[] SIGNAL_STRENGTH_TO_SPEED_MPH = new double[] { 0, 0, 3, 10, 15, 20, 25, 30, 35, 40, 50, 60, 70, 80, 90, 100 };
//      // _______________________________ CORRESPONDING REDSTONE SIGNAL STRENGTH: 0, 1, 2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,  15 :ARRAY INDEX
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
        if (entity instanceof AbstractMinecart cart) {
            CompoundTag nbt = cart.getPersistentData();
            System.out.println(20.0*nbt.getDouble("signal_spdlim")*2.23694);

            // check to see if player is holding the override signal
            boolean holdingOverride = holdingSignalById(cart, "elliesrailmod:signal_override");
            if (!holdingOverride) { // apply the respective signal, if applicable
                final int signalType = state.getValue(ATP_CTRL);
                if (signalType == 0) {
                    nbt.remove("signal_spdlim");
                } else {
                    nbt.putDouble("signal_spdlim", Speeds.SignalStrToSpeedMpt(signalType));
                }
            }
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
}