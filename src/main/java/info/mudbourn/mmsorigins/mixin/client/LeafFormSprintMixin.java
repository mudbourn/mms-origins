package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Lets a leaf-form floran start sprinting even though the form crawls.
 */
@Mixin(LocalPlayer.class)
public abstract class LeafFormSprintMixin {

    @Redirect(
        method = "canStartSprinting",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isMovingSlowly()Z")
    )
    private boolean mmsOrigins$sprintInLeafForm(LocalPlayer self) {
        return !MmsOriginsPowers.leafFormComplete(self) && self.isMovingSlowly();
    }
}
