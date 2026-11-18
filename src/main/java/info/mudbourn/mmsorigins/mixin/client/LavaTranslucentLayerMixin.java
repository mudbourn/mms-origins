package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.client.LavaViewState;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Renders lava on the translucent chunk layer while the viewer is submerged in
 * it. Lava is solid-layered, so its surface reads as an opaque wall from below,
 * hiding the world above once submerged; the translucent layer lets the lowered
 * vertex alpha in {@code LavaSurfaceAlphaMixin} show through. It is gated on
 * {@link LavaViewState} so lava stays solid when viewed from above, and the
 * state change forces the re-tesselation that swaps the layer.
 */
@Mixin(FluidStateModelSet.class)
public class LavaTranslucentLayerMixin {

    @ModifyReturnValue(method = "get", at = @At("RETURN"))
    private FluidModel mmsOrigins$lavaTranslucent(FluidModel model, FluidState fluidState) {
        if (LavaViewState.isSubmerged() && fluidState.is(FluidTags.LAVA)) {
            return new FluidModel(
                ChunkSectionLayer.TRANSLUCENT,
                model.stillMaterial(),
                model.flowingMaterial(),
                model.overlayMaterial(),
                model.tintSource());
        }
        return model;
    }
}
