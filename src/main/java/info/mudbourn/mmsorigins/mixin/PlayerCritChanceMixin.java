package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.power.CritChancePower;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Rolls the {@code mms_origins:crit_chance} power when a fully charged hit would
 * not crit on its own, so a successful roll takes the vanilla crit path with its
 * damage, particles and sound. A hit that already crits is never rolled again.
 */
@Mixin(Player.class)
public abstract class PlayerCritChanceMixin {

    @Inject(method = "canCriticalAttack", at = @At("RETURN"), cancellable = true)
    private void mmsOrigins$rollCritChance(Entity target, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (cir.getReturnValueZ() || player.level().isClientSide()) {
            return;
        }
        float chance = CritChancePower.totalChance(player);
        if (chance > 0.0F && player.getRandom().nextFloat() < chance) {
            cir.setReturnValue(true);
        }
    }
}
