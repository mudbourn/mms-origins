package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.client.LavaViewState;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Renders lava on the translucent chunk layer while the viewer is submerged in
 * it. Lava is solid-layered, so its surface reads as an opaque wall from below,
 * hiding the world above once submerged; the translucent layer lets the lowered
 * vertex alpha in {@code LavaSurfaceAlphaMixin} show through. It is gated on
 * {@link LavaViewState} so lava stays solid when viewed from above, and the
 * state change forces the re-tesselation that swaps the layer.
 */
@Mixin(ItemBlockRenderTypes.class)
public class LavaTranslucentLayerMixin {

    @Inject(method = "getRenderLayer", at = @At("HEAD"), cancellable = true)
    private static void mmsOrigins$lavaTranslucent(
            FluidState fluidState,
            CallbackInfoReturnable<ChunkSectionLayer> cir) {
        if (LavaViewState.isSubmerged() && fluidState.is(FluidTags.LAVA)) {
            cir.setReturnValue(ChunkSectionLayer.TRANSLUCENT);
        }
    }
}
