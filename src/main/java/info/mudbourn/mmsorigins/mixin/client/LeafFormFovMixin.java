package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Holds a leaf-form floran's field of view steady while the form's speed modifiers switch on and off.
 */
@Mixin(AbstractClientPlayer.class)
public abstract class LeafFormFovMixin {

    @Inject(method = "getFieldOfViewModifier", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$steadyFov(boolean firstPerson, float fovEffectScale, CallbackInfoReturnable<Float> cir) {
        if (MmsOriginsPowers.LEAF_FORM.isActive((AbstractClientPlayer) (Object) this)) {
            cir.setReturnValue(1.0F);
        }
    }
}
