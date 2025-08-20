package dev.elliectron.elliesrailmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class Class4Intersection extends RailBlock {
    private static final int TRACK_CLASS = 4;
    // private boolean lastRailNorthSouth = true;

    public Class4Intersection(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public RailShape getRailDirection(BlockState state, BlockGetter world, BlockPos pos, @Nullable AbstractMinecart cart) {
        if (cart == null) return RailShape.NORTH_SOUTH;
        Vec3 vv = cart.getDeltaMovement(); // vv = velocity vector (of minecart 'cart')
        // if (vv.x == 0 && vv.z == 0) {
        //     return lastRailNorthSouth ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST;
        // }
        if (Math.abs(vv.x) > Math.abs(vv.z)) {
            // lastRailNorthSouth = false;
            return RailShape.EAST_WEST;
        }
        // lastRailNorthSouth = true;
        return RailShape.NORTH_SOUTH;
    }

    @Override
    public boolean canMakeSlopes(BlockState state, BlockGetter world, BlockPos pos) {
        // Prohibit ascending rails for intersections
        return false;
    }

//    @Override
//    public BlockState updateDir(Level world, BlockPos pos, BlockState state, boolean initialPlacement) {
//        // Only allow straight orientations - no curves
//        RailShape currentShape = state.getValue(getShapeProperty());
//
//        // Force the rail to only use straight orientations
//        if (currentShape != RailShape.NORTH_SOUTH && currentShape != RailShape.EAST_WEST) {
//            // Default to north-south if somehow a curve was attempted
//            return state.setValue(getShapeProperty(), RailShape.NORTH_SOUTH);
//        }
//
//        return state;
//    }

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
}
