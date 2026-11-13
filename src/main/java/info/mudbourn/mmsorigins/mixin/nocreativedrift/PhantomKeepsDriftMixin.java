package info.mudbourn.mmsorigins.mixin.nocreativedrift;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps No Creative Drift from zeroing a phantom's momentum, so phantom wings can glide
 * and brake along their own curve while the mod still stops other flyers dead.
 */
@Pseudo
@Mixin(targets = "com.github.darkpred.nocreativedrift.DriftUtil", remap = false)
public abstract class PhantomKeepsDriftMixin {

    @Inject(method = "stopDrift", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$phantomKeepsDrift(LocalPlayer player, CallbackInfo ci) {
        if (MmsOriginsPowers.phantomFormComplete(player)) {
            ci.cancel();
        }
    }
}
