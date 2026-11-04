package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets an arachnid climb as fast as they run. Vanilla clamps sideways speed on a
 * climbable to a crawl and lifts at a fixed rate; for a bearer of the arachnid
 * climbing power this drops the sideways clamp and raises the lift to match
 * their ground speed, sprinting included.
 */
@Mixin(LivingEntity.class)
public abstract class ArachnidClimbMixin {

    private static final double VANILLA_LIFT = 0.2;
    private static final double SPEED_TO_BLOCKS_PER_TICK = 2.16;
    private static final double MAX_DESCENT = -0.15;

    @Shadow
    public abstract boolean onClimbable();

    @Shadow
    public abstract float getSpeed();

    @Shadow
    protected abstract boolean isSuppressingSlidingDownLadder();

    @Inject(method = "handleOnClimbable", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$unclampClimb(Vec3 motion, CallbackInfoReturnable<Vec3> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!mmsOrigins$isArachnid(self) || !onClimbable()) {
            return;
        }
        self.resetFallDistance();
        double y = Math.max(motion.y, MAX_DESCENT);
        if (y < 0.0 && !self.getInBlockState().is(Blocks.SCAFFOLDING) && isSuppressingSlidingDownLadder()) {
            y = 0.0;
        }
        cir.setReturnValue(new Vec3(motion.x, y, motion.z));
    }

    @ModifyConstant(method = "handleRelativeFrictionAndCalculateMovement", constant = @Constant(doubleValue = 0.2))
    private double mmsOrigins$runningLift(double lift) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!mmsOrigins$isArachnid(self)) {
            return lift;
        }
        return Math.max(VANILLA_LIFT, getSpeed() * SPEED_TO_BLOCKS_PER_TICK);
    }

    private static boolean mmsOrigins$isArachnid(LivingEntity entity) {
        return entity instanceof Player && MmsOriginsPowers.holds(entity, MmsOriginsPowers.ARACHNID_CLIMBING);
    }
}
