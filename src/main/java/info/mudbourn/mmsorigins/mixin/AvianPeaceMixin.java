package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

/**
 * Avian Peace: the overworld's hostile mobs will not pick out a bird-folk bearer.
 *
 * <p>Attack-targeting goals funnel their candidate through
 * {@code TargetingConditions.test}, so failing the test here denies fresh
 * acquisition. The truce is limited to overworld {@link Enemy} mobs, and a mob
 * the bearer has struck keeps fighting through its revenge memory; dropping a
 * lock a goal already holds is left to {@link AvianForgetTargetMixin}.
 */
@Mixin(TargetingConditions.class)
public class AvianPeaceMixin {

    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$spareAvian(ServerLevel level, LivingEntity attacker, LivingEntity target,
                                       CallbackInfoReturnable<Boolean> cir) {
        if (!(attacker instanceof Mob mob) || !(target instanceof Player)) {
            return;
        }
        if (!(mob instanceof Enemy) || level.dimension() != Level.OVERWORLD) {
            return;
        }
        if (mob.getLastHurtByMob() == target) {
            return;
        }
        if (MmsOriginsPowers.AVIAN_PEACE.isActive(target)) {
            cir.setReturnValue(false);
        }
    }
}
