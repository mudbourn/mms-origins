package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.PiglinKinship;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinBruteAi;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Kinsmen, for the brutes: a brute will not pick a spared kinsman to fight.
 *
 * <p>Brutes hunt on their own lookup, so their attack choice is refused the same
 * way. A provoked kinsman is not spared, so a struck brute still answers.
 */
@Mixin(PiglinBruteAi.class)
public class PiglinBruteAttackTargetMixin {

    @Inject(method = "findNearestValidAttackTarget", at = @At("RETURN"), cancellable = true)
    private static void mmsOrigins$spareKin(ServerLevel level, AbstractPiglin brute,
                                            CallbackInfoReturnable<Optional<? extends LivingEntity>> cir) {
        Optional<? extends LivingEntity> found = cir.getReturnValue();
        if (found.isPresent() && found.get() instanceof Player player && PiglinKinship.isSpared(brute, player)) {
            cir.setReturnValue(Optional.empty());
        }
    }
}
