package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.client.fur.FurModels;
import info.mudbourn.mmsorigins.client.fur.FurState;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Retextures the worn elytra with the wearer's fur elytra.
 *
 * <p>The wings layer already routes a per-wearer custom elytra texture, when one exists,
 * through the equipment renderer in place of the item's own layers. Overriding that
 * lookup lets a fur supply its own elytra skin without touching the equipment pipeline;
 * a wearer with no fur, or a fur that declares no elytra, is left untouched.
 */
@Mixin(WingsLayer.class)
public abstract class WingsElytraTextureMixin {

    @Inject(method = "getPlayerElytraTexture(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)Lnet/minecraft/resources/Identifier;",
            at = @At("RETURN"),
            cancellable = true)
    private static void mmsOrigins$furElytra(HumanoidRenderState state,
                                             CallbackInfoReturnable<Identifier> cir) {
        if (!(state instanceof FurState furState)) {
            return;
        }
        FurModels.Fur fur = FurModels.resolve(furState.mmsOrigins$furOrigin());
        if (fur != null && fur.elytra() != null) {
            cir.setReturnValue(fur.elytra());
        }
    }
}
