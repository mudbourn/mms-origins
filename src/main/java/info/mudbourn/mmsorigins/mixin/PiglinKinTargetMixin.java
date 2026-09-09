package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Kinsmen: a piglin never picks a kinsman out as a target in the first place.
 *
 * <p>Both the anger and the attack behaviours ask
 * {@code getNearestVisibleTargetablePlayer} who the nearest fair game is. Clearing
 * the answer here, rather than dropping the target after the fact, keeps the
 * piglin from ever flickering into a combat pose, so it stays calm and free to
 * barter. A zombified kinsman is fair game again: their own kind turn on them.
 */
@Mixin(PiglinAi.class)
public class PiglinKinTargetMixin {

    @Inject(method = "getNearestVisibleTargetablePlayer", at = @At("RETURN"), cancellable = true)
    private static void mmsOrigins$overlookKin(CallbackInfoReturnable<Optional<Player>> cir) {
        Optional<Player> found = cir.getReturnValue();
        if (found.isEmpty()) {
            return;
        }
        Player player = found.get();
        if (MmsOriginsPowers.KINSMEN.isActive(player) && !MmsOriginsPowers.ZOMBIFIED.isActive(player)) {
            cir.setReturnValue(Optional.empty());
        }
    }
}
