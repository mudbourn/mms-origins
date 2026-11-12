package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.HazeSoldiers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Spore haze bond: after each server AI step a bonded mob drops any lock on a
 * truffle it is neutral to, and a soldier is steered by its owner.
 */
@Mixin(Mob.class)
public class HazeBondMixin {

    @Inject(method = "serverAiStep", at = @At("RETURN"))
    private void mmsOrigins$stepBond(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (mob.level() instanceof ServerLevel level) {
            HazeSoldiers.step(mob, level);
        }
    }
}
