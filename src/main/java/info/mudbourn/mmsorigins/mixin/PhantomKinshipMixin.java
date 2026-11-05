package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Phantom kinship: a phantom drops any bearer it has picked as prey, unless the bearer struck it first.
 */
@Mixin(Mob.class)
public class PhantomKinshipMixin {

    @Inject(method = "serverAiStep", at = @At("RETURN"))
    private void mmsOrigins$spareKin(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (!(mob instanceof Phantom)) {
            return;
        }
        LivingEntity target = mob.getTarget();
        if (target != null && mob.getLastHurtByMob() != target && MmsOriginsPowers.PHANTOM_KINSHIP.isActive(target)) {
            mob.setTarget(null);
        }
    }
}
