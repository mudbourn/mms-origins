package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.HazeBond;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Spore haze truce: a mob never acquires a truffle it is neutral to, its master,
 * or its master's other soldiers as a target.
 *
 * <p>Attack-targeting goals funnel their candidate through
 * {@code TargetingConditions.test}, so failing the test here denies fresh
 * acquisition. Dropping a lock a goal already holds is left to
 * {@link HazeBondMixin}.
 */
@Mixin(TargetingConditions.class)
public class HazeNeutralMixin {

    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$spareBonded(ServerLevel level, LivingEntity attacker, LivingEntity target,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (attacker instanceof Mob mob && HazeBond.spares(mob, target)) {
            cir.setReturnValue(false);
        }
    }
}
