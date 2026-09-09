package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Kinsmen: piglins and brutes treat the bearer as one of their own.
 *
 * <p>Piglin hostility is brain-driven, so blocking fresh acquisition is not
 * enough; the brain re-picks the target every step. After each server AI step
 * this clears any piglin's lock on a kinsman and wipes the anger memory that
 * would re-arm it. A zombified kinsman is exempt: their own kind shun them, so
 * the piglin keeps its target.
 */
@Mixin(Mob.class)
public class PiglinKinshipMixin {

    @Inject(method = "serverAiStep", at = @At("RETURN"))
    private void mmsOrigins$sparePiglinKin(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (!(mob instanceof AbstractPiglin)) {
            return;
        }
        LivingEntity target = mob.getTarget();
        if (!(target instanceof Player player)) {
            return;
        }
        if (!MmsOriginsPowers.KINSMEN.isActive(player) || MmsOriginsPowers.ZOMBIFIED.isActive(player)) {
            return;
        }
        mob.setTarget(null);
        mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        mob.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
    }
}
