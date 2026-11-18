package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ender Kinship: an enderman never reads a bearer's stare as a provocation.
 *
 * <p>The anger goals decide to target a player through
 * {@code Enderman.isBeingStaredBy}, so answering false here for a bearer keeps
 * the enderman from ever acquiring them by gaze. Anger from other causes, a
 * struck enderman among them, runs through separate paths and is untouched.
 */
@Mixin(Enderman.class)
public class EndermanKinshipMixin {

    @Inject(method = "isBeingStaredBy", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$spareKin(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (MmsOriginsPowers.ENDER_KINSHIP.isActive(player)) {
            cir.setReturnValue(false);
        }
    }
}
