package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.power.PosePower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Slows the local player while a {@link PosePower} forces a crouch. On the
 * client, movement speed keys off {@code isMovingSlowly}, which keys off
 * {@code isCrouching}; {@code LocalPlayer} answers that from its own shift-key
 * input rather than the pose, so a forced crouch pose alone lowers the camera
 * without slowing the player. Reporting the crouch here makes vanilla apply its
 * own sneaking speed, so the forced pose behaves like a real sneak.
 */
@Mixin(LocalPlayer.class)
public class LocalPlayerCrouchMixin {

    @Inject(method = "isCrouching", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$forceCrouch(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        for (PosePower power : PowerHolderComponent.getPowers(self, PosePower.class)) {
            if (power.isActive() && power.getPose() == Pose.CROUCHING) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
