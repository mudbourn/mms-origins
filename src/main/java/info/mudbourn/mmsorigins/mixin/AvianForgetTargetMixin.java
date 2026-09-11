package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Avian Peace: an overworld hunter already locked onto a bird-folk bearer drops it.
 *
 * <p>Blocking fresh acquisition is not enough for goal-driven hunters whose
 * {@code canContinueToUse} keeps an existing target without re-running the
 * targeting test. So after each server AI step an overworld {@link Enemy} clears
 * any target that is now a spared avian, unless the bearer has struck it, in
 * which case its revenge keeps it locked on.
 */
@Mixin(Mob.class)
public class AvianForgetTargetMixin {

    @Inject(method = "serverAiStep", at = @At("RETURN"))
    private void mmsOrigins$forgetAvian(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (!(mob instanceof Enemy) || mob.level().dimension() != Level.OVERWORLD) {
            return;
        }
        LivingEntity target = mob.getTarget();
        if (!(target instanceof Player player) || mob.getLastHurtByMob() == target) {
            return;
        }
        if (MmsOriginsPowers.AVIAN_PEACE.isActive(player)) {
            mob.setTarget(null);
        }
    }
}
