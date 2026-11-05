package info.mudbourn.mmsorigins.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import info.mudbourn.mmsorigins.client.GuardianBeamClient;
import info.mudbourn.mmsorigins.client.GuardianBeamState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws a charging fishman beam from its target back to the caster, after the body, as a guardian does.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class GuardianBeamSubmitMixin {

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
        at = @At("RETURN")
    )
    private void mmsOrigins$extractBeam(LivingEntity entity, LivingEntityRenderState state, float partialTick,
                                        CallbackInfo ci) {
        GuardianBeamClient.stamp(entity, (GuardianBeamState) state, partialTick);
    }

    @Inject(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
        at = @At("RETURN")
    )
    private void mmsOrigins$submitBeam(LivingEntityRenderState state, PoseStack pose,
                                       SubmitNodeCollector collector, CameraRenderState camera,
                                       CallbackInfo ci) {
        GuardianBeamState beam = (GuardianBeamState) state;
        Vec3 vector = beam.mmsOrigins$beamVector();
        if (vector == null) {
            return;
        }
        float time = beam.mmsOrigins$beamTime();
        pose.pushPose();
        pose.translate(0.0F, beam.mmsOrigins$beamHeight(), 0.0F);
        GuardianRendererInvoker.mmsOrigins$renderBeam(
            pose,
            collector,
            vector,
            time * 0.5F % 1.0F,
            time,
            beam.mmsOrigins$beamScale());
        pose.popPose();
    }
}
