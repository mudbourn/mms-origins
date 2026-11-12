package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.AvianClimb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class AvianClimbMixin {

    private static final double THRUST = 0.05;
    private static final double STEEP_THRUST_MULTIPLIER = 2.75;
    private static final float STEEP_PITCH = -75.0F;
    private static final double ARMOR_SLOWDOWN = 0.1;
    // 25 blocks per second
    private static final double CLIMB_TOP_SPEED = 1.25;
    private static final double CLIMB_DRAG = 0.9;

    @Inject(method = "travel", at = @At("TAIL"))
    private void mmsOrigins$avianClimb(Vec3 input, CallbackInfo ci) {
        if (!((Object) this instanceof Player player) || !AvianClimb.climbing(player)) {
            return;
        }
        double thrust = THRUST;
        if (player.getXRot() <= STEEP_PITCH) {
            thrust *= STEEP_THRUST_MULTIPLIER;
        }
        thrust /= Math.max(1.0, player.getArmorValue() * ARMOR_SLOWDOWN);
        Vec3 look = player.getLookAngle();
        Vec3 motion = player.getDeltaMovement();
        Vec3 steer = look.scale(CLIMB_TOP_SPEED).subtract(motion);
        Vec3 climb = motion.add(look.scale(thrust)).add(steer.scale(thrust));
        double speed = climb.length();
        if (speed > CLIMB_TOP_SPEED) {
            climb = climb.scale(Math.max(CLIMB_TOP_SPEED, speed * CLIMB_DRAG) / speed);
        }
        player.setDeltaMovement(climb);
    }
}
