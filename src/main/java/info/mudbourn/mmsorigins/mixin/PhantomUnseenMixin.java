package info.mudbourn.mmsorigins.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.InvisibilityPower;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

/**
 * Phantomize: hostile mobs cannot pick out an invisible phantom on their own.
 *
 * <p>Attack-targeting goals funnel their candidate through
 * {@code TargetingConditions.test}, so failing the test here denies fresh
 * acquisition. Dropping a lock a goal-driven mob already holds is left to
 * {@link PhantomForgetTargetMixin}, since those goals keep an existing target
 * without re-running this test. The one exception is a mob the phantom has
 * struck, recognised by its revenge memory, which keeps fighting regardless.
 * The phantom is unseen until it gives itself away by attacking.
 */
@Mixin(TargetingConditions.class)
public class PhantomUnseenMixin {

    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$hidePhantom(ServerLevel level, LivingEntity attacker, LivingEntity target,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (!(attacker instanceof Mob mob) || !(target instanceof Player)) {
            return;
        }
        if (mob.getLastHurtByMob() == target) {
            return;
        }
        if (PowerHolderComponent.hasPower(target, InvisibilityPower.class, InvisibilityPower::isActive)) {
            cir.setReturnValue(false);
        }
    }
}
