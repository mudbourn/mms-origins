package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class AvianClimbMixin {

    private static final double CLIMB_ACCELERATION = 0.08;
    private static final double MAX_CLIMB_SPEED = 0.6;

    @Shadow
    protected boolean jumping;

    @Inject(method = "travel", at = @At("TAIL"))
    private void mmsOrigins$avianClimb(Vec3 input, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!jumping || !(self instanceof Player player) || !player.isFallFlying()) {
            return;
        }
        if (!MmsOriginsPowers.holds(player, MmsOriginsPowers.AVIAN_PEACE)) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        if (motion.y < MAX_CLIMB_SPEED) {
            double rise = Math.min(MAX_CLIMB_SPEED, motion.y + CLIMB_ACCELERATION);
            player.setDeltaMovement(motion.x, rise, motion.z);
        }
    }
}
