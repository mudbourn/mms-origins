package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.client.OriginScaleGate;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Holds a resizing origin's player unrendered until their scale has synced.
 *
 * <p>Powers like the fairy's diminutive marker and the enderian's slender body set the
 * scale attribute a tick or so after the player joins, so a freshly loaded player flashes
 * at full size before shrinking or stretching. While such a player still reads at the
 * default scale, the whole entity is culled here, which drops its model, layers, shadow,
 * and nametag together until the real scale arrives.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class OriginScaleHideMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$hideUntilScaled(LivingEntity entity, Frustum frustum,
                                            double x, double y, double z,
                                            CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player && OriginScaleGate.awaitingScale(player)) {
            cir.setReturnValue(false);
        }
    }
}
