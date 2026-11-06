package info.mudbourn.mmsorigins.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import info.mudbourn.mmsorigins.client.LeafFormState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws a floran in leaf form as a fern bush trailing a smaller fern tail, joined by grass,
 * in place of their body, armor and name.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LeafFormRenderMixin {

    @Unique
    private static final BlockState FERN = Blocks.FERN.defaultBlockState();

    @Unique
    private static final BlockState GRASS = Blocks.SHORT_GRASS.defaultBlockState();

    @Unique
    private static final float BUSH_SIZE = 0.9F;

    @Unique
    private static final float LINK_SIZE = 0.6F;

    @Unique
    private static final float TAIL_SIZE = 0.5F;

    @Unique
    private static final int FRONDS = 5;

    @Unique
    private static final float FROND_TILT = 35.0F;

    @Inject(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void mmsOrigins$submitLeafForm(LivingEntityRenderState state, PoseStack pose,
                                           SubmitNodeCollector collector, CameraRenderState camera,
                                           CallbackInfo ci) {
        if (!(state instanceof LeafFormState leaf) || !leaf.mmsOrigins$leafForm()) {
            return;
        }
        int light = state.lightCoords;
        Vec3 tail = leaf.mmsOrigins$leafTail();
        float tailYaw = (float) Math.toDegrees(Math.atan2(tail.x, tail.z));
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - (state.bodyRot + state.yRot)));
        mmsOrigins$bush(pose, collector, light, BUSH_SIZE);
        pose.popPose();
        pose.pushPose();
        pose.translate(tail.x * 0.5, tail.y * 0.5, tail.z * 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(tailYaw));
        mmsOrigins$plant(pose, collector, light, GRASS, LINK_SIZE);
        pose.popPose();
        pose.pushPose();
        pose.translate(tail.x, tail.y, tail.z);
        pose.mulPose(Axis.YP.rotationDegrees(tailYaw));
        mmsOrigins$bush(pose, collector, light, TAIL_SIZE);
        pose.popPose();
        ci.cancel();
    }

    /** Fans ferns out from a shared root so they read as one rounded bush. */
    @Unique
    private static void mmsOrigins$bush(PoseStack pose, SubmitNodeCollector collector, int light, float size) {
        mmsOrigins$plant(pose, collector, light, FERN, size);
        for (int i = 0; i < FRONDS; i++) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(i * 360.0F / FRONDS));
            pose.mulPose(Axis.XP.rotationDegrees(FROND_TILT));
            mmsOrigins$plant(pose, collector, light, FERN, size * 0.85F);
            pose.popPose();
        }
    }

    /** Submits one cross plant scaled about its base centre. */
    @Unique
    private static void mmsOrigins$plant(PoseStack pose, SubmitNodeCollector collector, int light,
                                         BlockState plant, float size) {
        pose.pushPose();
        pose.scale(size, size, size);
        pose.translate(-0.5F, 0.0F, -0.5F);
        collector.submitBlock(pose, plant, light, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }
}
