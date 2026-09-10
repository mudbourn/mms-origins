package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.client.ZombieShakeState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes a half-zombified piglin quiver like a body caught in powder snow.
 *
 * <p>The renderer already jitters the body yaw when {@link LivingEntityRenderer#isShaking}
 * is true, which vanilla reserves for a fully frozen entity. A player whose
 * zombification has passed the halfway mark carries the shake flag on its render
 * state, and this forces the same quiver on without disturbing the freezing case.
 */
@Mixin(LivingEntityRenderer.class)
public class ZombieShakeMixin {

    @Inject(method = "isShaking", at = @At("RETURN"), cancellable = true)
    private void mmsOrigins$zombieQuiver(LivingEntityRenderState state,
                                         CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()
                && state instanceof ZombieShakeState shake
                && shake.mmsOrigins$zombieShaking()) {
            cir.setReturnValue(true);
        }
    }
}
