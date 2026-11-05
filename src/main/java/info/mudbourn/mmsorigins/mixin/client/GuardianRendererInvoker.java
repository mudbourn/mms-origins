package info.mudbourn.mmsorigins.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes the guardian's own beam drawing so the fishman beam renders identically.
 */
@Mixin(GuardianRenderer.class)
public interface GuardianRendererInvoker {

    @Invoker("renderBeam")
    static void mmsOrigins$renderBeam(PoseStack pose, SubmitNodeCollector collector, Vec3 beam,
                                      float scroll, float attackTime, float attackScale) {
        throw new AssertionError();
    }
}
