package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.power.PosePower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies {@link PosePower} after vanilla has settled the pose for the tick,
 * replacing the newer Apoli {@code pose} power type that Apoli-Legacy omits.
 */
@Mixin(LivingEntity.class)
public class LivingEntityPoseMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void mmsOrigins$forcePose(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        for (PosePower power : PowerHolderComponent.getPowers(self, PosePower.class)) {
            if (power.isActive()) {
                self.setPose(power.getPose());
            }
        }
    }
}
