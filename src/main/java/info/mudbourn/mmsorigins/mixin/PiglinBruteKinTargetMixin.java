package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinBruteAi;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Kinsmen, for the brutes: a piglin brute discounts a kinsman as a valid target.
 *
 * <p>Brutes hunt on their own logic rather than the shared targetable-player
 * lookup, so their attack test is refused directly. A zombified kinsman is left
 * huntable, matching the way the rest of the mob turns on them.
 */
@Mixin(PiglinBruteAi.class)
public class PiglinBruteKinTargetMixin {

    @Inject(method = "isNearestValidAttackTarget", at = @At("HEAD"), cancellable = true)
    private static void mmsOrigins$spareKin(ServerLevel level, AbstractPiglin brute, LivingEntity target,
                                            CallbackInfoReturnable<Boolean> cir) {
        if (!(target instanceof Player player)) {
            return;
        }
        if (MmsOriginsPowers.KINSMEN.isActive(player) && !MmsOriginsPowers.ZOMBIFIED.isActive(player)) {
            cir.setReturnValue(false);
        }
    }
}
