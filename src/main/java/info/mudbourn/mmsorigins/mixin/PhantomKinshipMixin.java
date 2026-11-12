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
 * Phantom kinship: a phantom drops any bearer it has picked as prey, unless the bearer struck it
 * first. A phantom still holding a revenge mark on something else, such as a bearer's attacker,
 * turns back to that instead of going idle.
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
        LivingEntity foe = mob.getLastHurtByMob();
        if (target != null && foe != target && MmsOriginsPowers.PHANTOM_KINSHIP.isActive(target)) {
            mob.setTarget(foe != null && foe.isAlive() ? foe : null);
        }
    }
}
