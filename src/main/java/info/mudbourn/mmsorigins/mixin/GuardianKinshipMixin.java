package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

/**
 * Guardian Kinship: a guardian will not pick out a fishman as its prey.
 *
 * <p>Attack-targeting goals funnel their candidate through
 * {@code TargetingConditions.test}, so failing the test here denies fresh
 * acquisition. Elder guardians share the truce through the same class. A guardian
 * the fishman has struck keeps fighting through its revenge memory; dropping a
 * lock a goal already holds is left to {@link GuardianForgetTargetMixin}.
 */
@Mixin(TargetingConditions.class)
public class GuardianKinshipMixin {

    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$spareFishman(ServerLevel level, LivingEntity attacker, LivingEntity target,
                                         CallbackInfoReturnable<Boolean> cir) {
        if (!(attacker instanceof Guardian guardian) || !(target instanceof Player)) {
            return;
        }
        if (guardian.getLastHurtByMob() == target) {
            return;
        }
        if (MmsOriginsPowers.GUARDIAN_KINSHIP.isActive(target)) {
            cir.setReturnValue(false);
        }
    }
}
