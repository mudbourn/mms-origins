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

    private static final double CLIMB_ACCELERATION = 0.08;
    private static final double MAX_CLIMB_SPEED = 0.6;
    // 20 blocks per second
    private static final double CLIMB_TOP_SPEED = 1.0;
    private static final double CLIMB_DRAG = 0.9;

    @Inject(method = "travel", at = @At("TAIL"))
    private void mmsOrigins$avianClimb(Vec3 input, CallbackInfo ci) {
        if (!((Object) this instanceof Player player) || !AvianClimb.climbing(player)) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        double rise = Math.min(MAX_CLIMB_SPEED, Math.max(motion.y, 0.0) + CLIMB_ACCELERATION);
        Vec3 climb = new Vec3(motion.x, rise, motion.z);
        double speed = climb.length();
        if (speed > CLIMB_TOP_SPEED) {
            climb = climb.scale(Math.max(CLIMB_TOP_SPEED, speed * CLIMB_DRAG) / speed);
        }
        player.setDeltaMovement(climb);
    }
}
