package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class FlightSpeedCapMixin {

    // 50 blocks per second
    private static final double MAX_FLIGHT_SPEED = 2.5;
    // 35 blocks per second
    private static final double AVIAN_CRUISE_SPEED = 1.75;
    private static final double AVIAN_CRUISE_GAIN = 0.08;
    private static final float AVIAN_CLIMB_PITCH = -25.0F;

    @Inject(method = "tick", at = @At("TAIL"))
    private void mmsOrigins$capFlightSpeed(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        if (!player.isFallFlying() && !player.getAbilities().flying) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        if (player.isFallFlying() && MmsOriginsPowers.holds(player, MmsOriginsPowers.AVIAN_PEACE)) {
            motion = mmsOrigins$holdAvianCruise(player, motion);
            player.setDeltaMovement(motion);
        }
        if (motion.lengthSqr() > MAX_FLIGHT_SPEED * MAX_FLIGHT_SPEED) {
            player.setDeltaMovement(motion.normalize().scale(MAX_FLIGHT_SPEED));
        }
    }

    private static Vec3 mmsOrigins$holdAvianCruise(Player player, Vec3 motion) {
        double speed = motion.length();
        if (speed >= AVIAN_CRUISE_SPEED || player.getXRot() < AVIAN_CLIMB_PITCH) {
            return motion;
        }
        Vec3 push = player.getLookAngle().scale((AVIAN_CRUISE_SPEED - speed) * AVIAN_CRUISE_GAIN);
        return motion.add(push);
    }
}
