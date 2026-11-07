package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps a leaf-form floran from sprinting, so the form moves at its own fixed pace.
 */
@Mixin(LocalPlayer.class)
public abstract class LeafFormNoSprintMixin {

    @Inject(method = "canStartSprinting", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$noSprintStart(CallbackInfoReturnable<Boolean> cir) {
        if (MmsOriginsPowers.leafFormComplete((LocalPlayer) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void mmsOrigins$stopSprint(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        if (self.isSprinting() && MmsOriginsPowers.leafFormComplete(self)) {
            self.setSprinting(false);
        }
    }
}
