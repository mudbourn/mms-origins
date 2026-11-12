package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets a morphed phantom move at full pace. The morph forces the swimming pose
 * for its low hitbox, and vanilla reads that pose out of water as a crawl,
 * which cuts movement input to the sneaking speed and refuses to start a
 * sprint. Only a held sneak counts as moving slowly in phantom form.
 */
@Mixin(LocalPlayer.class)
public abstract class PhantomFormPaceMixin {

    @Inject(method = "isMovingSlowly", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$phantomPace(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        if (MmsOriginsPowers.phantomFormComplete(self)) {
            cir.setReturnValue(self.isCrouching());
        }
    }
}
