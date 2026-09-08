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
 * <p>Every attack-targeting goal funnels its candidate through
 * {@code TargetingConditions.test}, so failing the test here denies fresh
 * acquisition without touching revenge: a mob already fixed on the phantom, or
 * one the phantom has struck, keeps its quarry because those paths set the target
 * directly rather than asking to acquire one. The mob stays blind until the
 * phantom gives itself away by attacking.
 */
@Mixin(TargetingConditions.class)
public class PhantomUnseenMixin {

    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$hidePhantom(ServerLevel level, LivingEntity attacker, LivingEntity target,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (!(attacker instanceof Mob mob) || !(target instanceof Player)) {
            return;
        }
        if (mob.getTarget() == target || mob.getLastHurtByMob() == target) {
            return;
        }
        if (PowerHolderComponent.hasPower(target, InvisibilityPower.class, InvisibilityPower::isActive)) {
            cir.setReturnValue(false);
        }
    }
}
