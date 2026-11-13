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

    // 6 blocks per second
    private static final double CLIMB_RATE = 0.3;
    private static final double CLIMB_EASE = 0.15;
    private static final double ARMOR_SLOWDOWN = 0.1;

    @Inject(method = "travel", at = @At("TAIL"))
    private void mmsOrigins$avianClimb(Vec3 input, CallbackInfo ci) {
        if (!((Object) this instanceof Player player) || !AvianClimb.climbing(player)) {
            return;
        }
        double rate = CLIMB_RATE / Math.max(1.0, player.getArmorValue() * ARMOR_SLOWDOWN);
        Vec3 motion = player.getDeltaMovement();
        if (motion.y >= rate) {
            return;
        }
        double rise = motion.y + (rate - motion.y) * CLIMB_EASE;
        player.setDeltaMovement(motion.x, Math.max(0.0, rise), motion.z);
    }
}
