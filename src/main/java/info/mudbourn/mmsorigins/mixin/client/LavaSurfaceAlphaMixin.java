package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.client.LavaViewState;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fades lava's surface so a submerged view can read the world above it. Every
 * fluid vertex flows through the shared {@code vertex} helper with a full alpha
 * color, so this flags a lava tesselation for the length of that call and
 * softens only its vertex alpha, leaving water untouched. The flag is per thread
 * because chunk meshes build on worker threads. The fade only applies while the
 * viewer is submerged, per {@link LavaViewState}, so lava stays opaque from above.
 */
@Mixin(FluidRenderer.class)
public class LavaSurfaceAlphaMixin {

    private static final int LAVA_ALPHA = 0x8C;
    private static final ThreadLocal<Boolean> TESSELATING_LAVA = ThreadLocal.withInitial(() -> false);

    @Inject(method = "tesselate", at = @At("HEAD"))
    private void mmsOrigins$markLava(
            BlockAndTintGetter level,
            net.minecraft.core.BlockPos pos,
            FluidRenderer.Output output,
            net.minecraft.world.level.block.state.BlockState blockState,
            FluidState fluidState,
            CallbackInfo ci) {
        TESSELATING_LAVA.set(fluidState.is(FluidTags.LAVA));
    }

    @Inject(method = "tesselate", at = @At("RETURN"))
    private void mmsOrigins$clearLava(
            BlockAndTintGetter level,
            net.minecraft.core.BlockPos pos,
            FluidRenderer.Output output,
            net.minecraft.world.level.block.state.BlockState blockState,
            FluidState fluidState,
            CallbackInfo ci) {
        TESSELATING_LAVA.set(false);
    }

    @ModifyVariable(method = "vertex", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int mmsOrigins$fadeLava(int color) {
        return LavaViewState.isSubmerged() && TESSELATING_LAVA.get() ? LAVA_ALPHA << 24 | color & 0xFFFFFF : color;
    }
}
