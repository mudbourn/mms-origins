package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.client.LavaViewState;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fades lava's surface so a submerged view can read the world above it. Every
 * fluid vertex flows through the shared {@code vertex} helper with a hardcoded
 * full alpha, so this flags a lava tesselation for the length of that call and
 * softens only its vertex alpha, leaving water untouched. The flag is per thread
 * because chunk meshes build on worker threads. The fade only applies while the
 * viewer is submerged, per {@link LavaViewState}, so lava stays opaque from above.
 */
@Mixin(LiquidBlockRenderer.class)
public class LavaSurfaceAlphaMixin {

    private static final float LAVA_ALPHA = 0.55f;
    private static final ThreadLocal<Boolean> TESSELATING_LAVA = ThreadLocal.withInitial(() -> false);

    @Inject(method = "tesselate", at = @At("HEAD"))
    private void mmsOrigins$markLava(
            net.minecraft.world.level.BlockAndTintGetter level,
            net.minecraft.core.BlockPos pos,
            com.mojang.blaze3d.vertex.VertexConsumer consumer,
            net.minecraft.world.level.block.state.BlockState blockState,
            FluidState fluidState,
            CallbackInfo ci) {
        TESSELATING_LAVA.set(fluidState.is(FluidTags.LAVA));
    }

    @Inject(method = "tesselate", at = @At("RETURN"))
    private void mmsOrigins$clearLava(
            net.minecraft.world.level.BlockAndTintGetter level,
            net.minecraft.core.BlockPos pos,
            com.mojang.blaze3d.vertex.VertexConsumer consumer,
            net.minecraft.world.level.block.state.BlockState blockState,
            FluidState fluidState,
            CallbackInfo ci) {
        TESSELATING_LAVA.set(false);
    }

    @ModifyArg(
        method = "vertex",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setColor(FFFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"),
        index = 3)
    private float mmsOrigins$fadeLava(float alpha) {
        return TESSELATING_LAVA.get() && LavaViewState.isSubmerged() ? LAVA_ALPHA : alpha;
    }
}
