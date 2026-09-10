package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.PiglinKinship;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Kinsmen: a piglin will not pick a spared kinsman as its attack target.
 *
 * <p>The kinsman still fills the ordinary targetable-player memory, so the piglin
 * keeps seeing them as someone to barter with; only the choice of who to fight is
 * refused here. That keeps kinship from breaking bartering the way emptying the
 * shared memory did, and it never sets an attack target, so there is no flicker.
 * A provoked kinsman is not spared, so retaliation still lands.
 *
 * <p>A zombified player is refused as a target too, while they hold no weapon
 * against the piglin: a piglin flees the zombified rather than fighting them, and
 * the fight activity outranks the avoid one, so leaving the target unset lets the
 * flee take hold. A zombified player who strikes the pack is no longer spared and
 * is hunted. Brutes share the truce through their own mixin, but never flee.
 */
@Mixin(PiglinAi.class)
public class PiglinAttackTargetMixin {

    @Inject(method = "findNearestValidAttackTarget", at = @At("RETURN"), cancellable = true)
    private static void mmsOrigins$spareKin(ServerLevel level, Piglin piglin,
                                            CallbackInfoReturnable<Optional<? extends LivingEntity>> cir) {
        Optional<? extends LivingEntity> found = cir.getReturnValue();
        if (found.isPresent() && found.get() instanceof Player player
                && (PiglinKinship.isSpared(piglin, player) || PiglinKinship.sparesZombified(piglin, player))) {
            cir.setReturnValue(Optional.empty());
        }
    }
}
