package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps an arachnid sprinting while they climb. Vanilla ends a run sprint on any
 * hard wall collision, which is always true against a climbed wall, so the climb
 * never reached sprint speed. While climbing, only the usual sprint conditions
 * (enough food, forward input) can end it.
 */
@Mixin(LocalPlayer.class)
public abstract class ArachnidClimbSprintMixin {

    @Shadow
    public ClientInput input;

    @Shadow
    protected abstract boolean isSprintingPossible(boolean flying);

    @Inject(method = "shouldStopRunSprinting", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$sprintWhileClimbing(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        if (!self.onClimbable() || !MmsOriginsPowers.holds(self, MmsOriginsPowers.ARACHNID_CLIMBING)) {
            return;
        }
        cir.setReturnValue(!isSprintingPossible(self.getAbilities().flying) || !input.hasForwardImpulse());
    }
}
