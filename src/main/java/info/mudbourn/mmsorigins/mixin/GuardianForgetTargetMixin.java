package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Guardian Kinship: a guardian already locked onto a fishman drops the target.
 *
 * <p>Blocking fresh acquisition is not enough for a goal that keeps its target
 * without re-running the targeting test, so after each server AI step a guardian
 * clears any target that is now a spared fishman, unless the bearer has struck
 * it, in which case its revenge keeps it locked on.
 */
@Mixin(Mob.class)
public class GuardianForgetTargetMixin {

    @Inject(method = "serverAiStep", at = @At("RETURN"))
    private void mmsOrigins$forgetFishman(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (!(mob instanceof Guardian)) {
            return;
        }
        LivingEntity target = mob.getTarget();
        if (!(target instanceof Player player) || mob.getLastHurtByMob() == target) {
            return;
        }
        if (MmsOriginsPowers.GUARDIAN_KINSHIP.isActive(player)) {
            mob.setTarget(null);
        }
    }
}
