package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Phantom truce: a hostile mob already locked onto a phantom drops it, and in
 * phantom form any mob does.
 *
 * <p>Blocking fresh acquisition is not enough for goal-driven hunters like
 * skeletons, whose {@code canContinueToUse} keeps an existing target without ever
 * re-running the targeting test. So after each server AI step this clears any
 * target that is now a spared phantom, unless the phantom has struck the mob,
 * in which case its revenge keeps it locked on regardless of sight.
 */
@Mixin(Mob.class)
public class PhantomForgetTargetMixin {

    @Inject(method = "serverAiStep", at = @At("RETURN"))
    private void mmsOrigins$forgetUnseenPhantom(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        LivingEntity target = mob.getTarget();
        if (!(target instanceof Player) || mob.getLastHurtByMob() == target) {
            return;
        }
        if (MmsOriginsPowers.PHANTOM_FORM.isActive(target)
                || mob instanceof Enemy && MmsOriginsPowers.PHANTOM_KINSHIP.isActive(target)) {
            mob.setTarget(null);
        }
    }
}
