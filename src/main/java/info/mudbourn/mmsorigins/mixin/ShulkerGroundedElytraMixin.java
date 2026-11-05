package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.ShulkerGrounding;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps a player grounded by a shulk's bullet from opening an elytra.
 */
@Mixin(Player.class)
public abstract class ShulkerGroundedElytraMixin {

    @Inject(method = "tryToStartFallFlying", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$blockElytra(CallbackInfoReturnable<Boolean> cir) {
        if (ShulkerGrounding.isGrounded((Player) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
