package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import info.mudbourn.mmsorigins.PiglinKinship;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A zombified piglin is shunned by its own kind, as any zombified piglin is.
 *
 * <p>Vanilla flees a nearby zombified piglin through the {@code AVOID} activity,
 * driven off its {@code AVOID_TARGET} memory. A zombified kinsman is not a mob, so
 * that memory is set here instead: while one stands within reach, the piglin marks
 * them the thing to run from, and refuses to pick up the gold it would barter for.
 * Brutes run {@code PiglinBruteAi} and never fear the zombified, so leaving this on
 * {@code PiglinAi} spares them the same way vanilla spares them from real zombified
 * piglins.
 */
@Mixin(PiglinAi.class)
public class PiglinFleeZombifiedMixin {

    @Unique
    private static final double MMS_FLEE_RANGE = 8.0;

    @Unique
    private static final int MMS_FLEE_DURATION = 60;

    @Inject(method = "updateActivity", at = @At("HEAD"))
    private static void mmsOrigins$fleeZombified(Piglin piglin, CallbackInfo ci) {
        Player bearer = mmsOrigins$nearbyZombified(piglin);
        if (bearer != null && PiglinKinship.sparesZombified(piglin, bearer)) {
            Brain<Piglin> brain = piglin.getBrain();
            brain.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, bearer, MMS_FLEE_DURATION);
        }
    }

    @Inject(method = "wantsToPickup", at = @At("HEAD"), cancellable = true)
    private static void mmsOrigins$refuseBarter(Piglin piglin, ItemStack stack,
                                                CallbackInfoReturnable<Boolean> cir) {
        if ((stack.is(Items.GOLD_INGOT) || stack.is(Items.GOLD_BLOCK))
                && mmsOrigins$nearbyZombified(piglin) != null) {
            cir.setReturnValue(false);
        }
    }

    // The nearest zombified kinsman within fleeing range, or null if none is close.
    @Unique
    private static Player mmsOrigins$nearbyZombified(Piglin piglin) {
        Player player = piglin.level().getNearestPlayer(piglin, MMS_FLEE_RANGE);
        if (player != null && MmsOriginsPowers.ZOMBIFIED.isActive(player)) {
            return player;
        }
        return null;
    }
}
