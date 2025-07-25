package dev.elliectron.elliesrailmod.event;

import dev.elliectron.elliesrailmod.ElliesRailImprovements;
import dev.elliectron.elliesrailmod.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = ElliesRailImprovements.MODID)
public class MinecartStoppingHandler {

    // Base deceleration rates
    private static final double BASE_DECELERATION_RATE = -0.0800/20;
    private static final double RAIN_DECELERATION_MULTIPLIER = 0.7; // 30% less deceleration in rain
    private static final double ATTACHED_PAX_DECEL_FACTOR = 0.955; // Reduced deceleration per attached minecart (player)
    private static final double ATTACHED_FREIGHT_DECEL_FACTOR = 0.930; // Reduced deceleration per attached minecart (chest/hopper/etc)
    private static final double MIN_SPEED = 0.01;

    // Brake thermals values
    private static final double ESTOP_DECEL_BONUS = 1.50;
    private static final double DEFAULT_BRAKE_TEMP = 293.0; // Kelvin
    private static final double EMER_BRAKE_HEATING_RATE = 0.800; // K heating per tick when using emergency brakes
    private static final double NORM_BRAKE_HEATING_RATE = 0.355; // K heating per tick when using normal brakes
    private static final double BRAKE_COOLING_RATE = 0.04; // K cooling amount per tick when not braking
    private static final double MAX_BRAKE_TEMP = 1000.0; // Kelvin - maximum overheating
    private static final double BRAKE_EFFECTIVENESS_LOSS_PER_K = 0.0015; // 0.15% per Kelvin above default
    private static final double MIN_BRAKE_EFFECTIVENESS = 0.60; // minimum 60% effectiveness

    // note that the mod does not support minecart linking yet and therefore
    // , you will have to use other methods, whether it is vanilla or additional mod(s)
    private static final double ATTACHED_MINECART_SEARCH_RADIUS = 9.0;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof AbstractMinecart minecart)) return;
        if (minecart.getPassengers().isEmpty()) return;

        Player player = null;
        for (Entity passenger : minecart.getPassengers())
            if (passenger instanceof Player) {
                player = (Player) passenger;
                break;
            }
        if (player == null) return;

        // Update brake temperature every tick
        updateBrakeTemperature(minecart, player);

    //    System.out.println("SCANNING PLAYER HAND ITEM");
        // Apply braking based on signals
        if (isHoldingSignalStop(player)) {
    //        System.out.println("STOP HELD");
            CompoundTag tag = minecart.getPersistentData();
            boolean isClass5Plus = tag.contains("spd");
            if (isClass5Plus) {
                DecelerateMinecart(minecart, false, true);
            } else {
                Vec3 rawMvmnt = minecart.getDeltaMovement();
                double spd = Math.sqrt(rawMvmnt.x* rawMvmnt.x + rawMvmnt.z* rawMvmnt.z);
                if (spd < 0.01) minecart.setDeltaMovement(0, 0, 0);
                else DecelerateMinecart(minecart, false, false);
            }
        } else if (isHoldingEstopSignal(player)) {
    //        System.out.println("EMER STOP HELD");
            CompoundTag tag = minecart.getPersistentData();
            boolean isClass5Plus = tag.contains("spd");
            if (isClass5Plus) {
                DecelerateMinecart(minecart, true, true);
            } else {
                Vec3 rawMvmnt = minecart.getDeltaMovement();
                double spd = Math.sqrt(rawMvmnt.x* rawMvmnt.x + rawMvmnt.z* rawMvmnt.z);
                if (spd < 0.01) minecart.setDeltaMovement(0, 0, 0);
                else DecelerateMinecart(minecart, true, false);
            }
        }
    }

    private static void updateBrakeTemperature(AbstractMinecart minecart, Player player) {
        var nbt = minecart.getPersistentData();

        // Get current brake temperature (default to 293K if not set)
        double currentTemp = nbt.getDouble("brake_temperature");
        if (currentTemp == 0.0) {
            currentTemp = DEFAULT_BRAKE_TEMP;
        }

        boolean isEbraking = isHoldingEstopSignal(player);
        boolean isNbraking = isHoldingSignalStop(player);
        Vec3 vv = minecart.getDeltaMovement();
        double spd = Math.sqrt(vv.x*vv.x + vv.z*vv.z);

        if (isEbraking && spd > 0.01) { // high amount of brake heating when using emergency brakes
            // Square law heating: 2x rate at 100 mph (2.2352 blocks/tick)
            // Physics-accurate square law: 4x heat at double velocity
            double baselineMultiplier = 2.0; // 2x heat at 100 mph baseline
            double speedRatio = spd / 2.2352; // Speed relative to 100 mph
            double speedMultiplier = Math.max(0.1, baselineMultiplier * speedRatio * speedRatio);
            currentTemp = Math.min(MAX_BRAKE_TEMP, currentTemp + EMER_BRAKE_HEATING_RATE * speedMultiplier);
        } else if (isNbraking && spd > 0.01) { // low amount of brake heating when using normal brakes
            double baselineMultiplier = 2.0;
            double speedRatio = spd / 2.2352;
            double speedMultiplier = Math.max(0.1, baselineMultiplier * speedRatio * speedRatio);
            currentTemp = Math.min(MAX_BRAKE_TEMP, currentTemp + NORM_BRAKE_HEATING_RATE * speedMultiplier);
        } else if ((isEbraking || isNbraking) && spd < 0.01) { // if train is not actively decelerating, but brakes are still held, cool slowly
            currentTemp = Math.max(DEFAULT_BRAKE_TEMP, currentTemp - 0.3*BRAKE_COOLING_RATE);
        }
        else { // Cool down brakes when brakes released
            currentTemp = Math.max(DEFAULT_BRAKE_TEMP, currentTemp - BRAKE_COOLING_RATE);
        }

        // Store updated temperature
        nbt.putDouble("brake_temperature", currentTemp);

        // Optional: Send temperature warnings to player
        sendTemperatureWarnings(player, currentTemp);
    }

    public static double getBrakeEffectiveness(AbstractMinecart minecart) {
        var nbt = minecart.getPersistentData();
        double currentTemp = nbt.getDouble("brake_temperature");
        if (currentTemp == 0.0) currentTemp = DEFAULT_BRAKE_TEMP;

        double tempAboveDefault = Math.max(0, currentTemp - DEFAULT_BRAKE_TEMP);
        double effectivenessLoss = tempAboveDefault * BRAKE_EFFECTIVENESS_LOSS_PER_K;
        double effectiveness = 1.0 - effectivenessLoss;
        return Math.max(MIN_BRAKE_EFFECTIVENESS, effectiveness);
    }

    private static void sendTemperatureWarnings(Player player, double currentTemp) {
        // Only send warnings occasionally to avoid spam
        if (player.level().getGameTime() % 60 != 0) return; // Every 3 seconds

        // warn every 20 *C
        double tempCDeg = currentTemp - 273;
        if (tempCDeg > 20 && tempCDeg % 20 == 0) player.sendSystemMessage(Component.literal(
                "§e§l[CAUTION] BRAKE TEMP: " + String.format("%.0f°C", tempCDeg)
                        + " -" + ((currentTemp-DEFAULT_BRAKE_TEMP) * BRAKE_EFFECTIVENESS_LOSS_PER_K * 100) + "% effectiveness"));
    }

    private static boolean isHoldingSignalStop(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return mainHand.is(ModItems.SIGNAL_STOP.get()) || offHand.is(ModItems.SIGNAL_STOP.get());
    }

    private static boolean isHoldingEstopSignal(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return mainHand.is(ModItems.SIGNAL_E_STOP.get()) || offHand.is(ModItems.SIGNAL_E_STOP.get());
    }

    public static void DecelerateMinecart(AbstractMinecart minecart, boolean isEstop, boolean isClass5Plus) {
    //    System.out.println("CALLED EBRAKE " + isEstop);
        if (isClass5Plus) { // if the 'spd' NBT tag exists, that means the minecart is on a class 5 or higher track which uses custom speed/acceleration physics/logic
            CompoundTag nbt = minecart.getPersistentData();
       //     System.out.println("DECEL CLASS 5");
            double spd = nbt.getDouble("spd");
            if (spd < MIN_SPEED) {
                nbt.putDouble("spd", 0.0);
                minecart.setDeltaMovement(0, 0, 0);
                return;
            }

            double dynDecelRate = calcDynamicDecelRate(minecart, isEstop);
            dynDecelRate += spd;
            nbt.putDouble("spd", dynDecelRate);
    //        double deceledSpd = spd + dynDecelRate;
    //        System.out.println(dynDecelRate + " += > " + deceledSpd);
    //        nbt.putDouble("spd", deceledSpd);
        } else { // if the 'spd' NBT tag does not exist, that means the minecart is on a class 4 or lower track which uses vanilla speed/acceleration physics/logic
            Vec3 motion = minecart.getDeltaMovement();
            if (20 * Math.sqrt(motion.x * motion.x + motion.z * motion.z) < MIN_SPEED) {
                minecart.setDeltaMovement(0, motion.y, 0);
                return;
            }

            double dynamicDecelerationRate = calcDynamicDecelRate(minecart, isEstop);
            Vec3 decelAmount = calcDecelAmount(motion.x, motion.y, motion.z, dynamicDecelerationRate);
            minecart.setDeltaMovement(motion.add(decelAmount));
        }
    }

    private static double calcDynamicDecelRate(AbstractMinecart minecart, boolean isEstop) {
        Level level = minecart.level();
        double decelRate = BASE_DECELERATION_RATE;
        if (isRainingAtPosition(level, minecart)) decelRate *= RAIN_DECELERATION_MULTIPLIER;

        int[] attachedCount = countAttachedMinecarts(minecart);
        int paxMinecartsNum = attachedCount[0];
        int freightMinecartsNum = attachedCount[1];
        decelRate *= Math.pow(ATTACHED_PAX_DECEL_FACTOR, paxMinecartsNum);
        decelRate *= Math.pow(ATTACHED_FREIGHT_DECEL_FACTOR, freightMinecartsNum);

        if (isEstop) decelRate *= ESTOP_DECEL_BONUS;

        double brakeEffectiveness = getBrakeEffectiveness(minecart);
        decelRate *= brakeEffectiveness;

        return decelRate;
    }

    private static boolean isRainingAtPosition(Level level, AbstractMinecart minecart) {
        // Check if it's raining in the biome and if the minecart is exposed to sky
        return level.isRainingAt(minecart.blockPosition()) || (level.isRaining() && level.canSeeSky(minecart.blockPosition()));
    }

    private static int[] countAttachedMinecarts(AbstractMinecart leadMinecart) {
        int countPax = 0;
        int countFreight = 0;
        Level level = leadMinecart.level();

        for (Entity entity : level.getEntitiesOfClass(AbstractMinecart.class, leadMinecart.getBoundingBox().inflate(ATTACHED_MINECART_SEARCH_RADIUS))) {
            if (entity != leadMinecart && entity instanceof AbstractMinecart otherCart) {
                double distance = leadMinecart.distanceTo(entity);
                if (distance <= ATTACHED_MINECART_SEARCH_RADIUS) {
                    if (entity instanceof Minecart) ++countPax;
                    else ++countFreight;
                }
            }
        }

        return new int[] { countPax, countFreight } ;
    }

    private static Vec3 calcDecelAmount(double x, double y, double z, double decelerationRate) {
        double xAccel = 0, zAccel = 0;

        if (x < 0) xAccel = -decelerationRate;
        else if (x > 0) xAccel = decelerationRate;

        if (z < 0) zAccel = -decelerationRate;
        else if (z > 0) zAccel = decelerationRate;

        return new Vec3(xAccel, 0, zAccel);
    }
}