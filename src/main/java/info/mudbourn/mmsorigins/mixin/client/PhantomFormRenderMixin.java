package info.mudbourn.mmsorigins.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import info.mudbourn.mmsorigins.client.PhantomFlightPose;
import info.mudbourn.mmsorigins.client.PhantomFormState;
import io.github.apace100.apoli.util.ApoliLivingEntityRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws a morphed phantom as a vanilla phantom, through the vanilla phantom renderer, in place of
 * their body, armor and held items. The phantom's render state is extracted once from a template
 * phantom so the fields other mods add to it hold a real phantom's values, then the player's own
 * state is copied over it each frame. The player's Apoli powers ride along so translucency and
 * invisibility apply to the phantom body too. The body is turned by the smoothed
 * {@link PhantomFlightPose} so it follows its motion and banks into turns.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class PhantomFormRenderMixin {

    @Unique
    private static PhantomRenderState mmsOrigins$phantom;

    @Unique
    private static final int PHANTOM_SIZE = 0;

    @Unique
    private static final float FACING_FRONT_BODY_ROT = 180.0F;

    @Inject(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void mmsOrigins$submitPhantomForm(LivingEntityRenderState state, PoseStack pose,
                                              SubmitNodeCollector collector, CameraRenderState camera,
                                              CallbackInfo ci) {
        if (!(state instanceof PhantomFormState form) || !form.mmsOrigins$phantomForm()) {
            return;
        }
        PhantomRenderState phantom = mmsOrigins$phantomState();
        mmsOrigins$copy(state, phantom);
        PhantomFlightPose.Angles angles = form.mmsOrigins$phantomPose();
        float pivot = state.boundingBoxHeight * 0.5F;
        pose.pushPose();
        pose.translate(0.0F, pivot, 0.0F);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - angles.yaw()));
        pose.mulPose(Axis.XP.rotationDegrees(angles.pitch()));
        pose.mulPose(Axis.ZP.rotationDegrees(-angles.roll()));
        pose.translate(0.0F, -pivot, 0.0F);
        Minecraft.getInstance()
            .getEntityRenderDispatcher()
            .getRenderer(phantom)
            .submit(phantom, pose, collector, camera);
        pose.popPose();
        ci.cancel();
    }

    /** The shared phantom render state, extracted from a template phantom on first use. */
    @Unique
    private static PhantomRenderState mmsOrigins$phantomState() {
        if (mmsOrigins$phantom == null) {
            Minecraft client = Minecraft.getInstance();
            Phantom template = new Phantom(EntityType.PHANTOM, client.level);
            mmsOrigins$phantom = (PhantomRenderState) client.getEntityRenderDispatcher()
                .getRenderer(template)
                .createRenderState(template, 0.0F);
        }
        return mmsOrigins$phantom;
    }

    /** Fills the phantom render state from the player's, leaving every rotation to the pose stack. */
    @Unique
    private static void mmsOrigins$copy(LivingEntityRenderState from, PhantomRenderState to) {
        to.entityType = EntityType.PHANTOM;
        to.x = from.x;
        to.y = from.y;
        to.z = from.z;
        to.ageInTicks = from.ageInTicks;
        to.boundingBoxWidth = from.boundingBoxWidth;
        to.boundingBoxHeight = from.boundingBoxHeight;
        to.eyeHeight = from.eyeHeight;
        to.distanceToCameraSq = from.distanceToCameraSq;
        to.isInvisible = from.isInvisible;
        to.isDiscrete = from.isDiscrete;
        to.displayFireAnimation = from.displayFireAnimation;
        to.lightCoords = from.lightCoords;
        to.outlineColor = from.outlineColor;
        to.passengerOffset = from.passengerOffset;
        to.nameTag = from.nameTag;
        to.nameTagAttachment = from.nameTagAttachment;
        to.bodyRot = FACING_FRONT_BODY_ROT;
        to.yRot = 0.0F;
        to.xRot = 0.0F;
        to.deathTime = from.deathTime;
        to.walkAnimationPos = from.walkAnimationPos;
        to.walkAnimationSpeed = from.walkAnimationSpeed;
        to.scale = 1.0F;
        to.ageScale = 1.0F;
        to.isFullyFrozen = from.isFullyFrozen;
        to.isInWater = from.isInWater;
        to.hasRedOverlay = from.hasRedOverlay;
        to.isInvisibleToPlayer = from.isInvisibleToPlayer;
        to.pose = Pose.STANDING;
        to.flapTime = from.ageInTicks;
        to.size = PHANTOM_SIZE;
        ((ApoliLivingEntityRenderState) to).apoli$setPowerHolder(
            ((ApoliLivingEntityRenderState) from).apoli$getPowerHolder());
    }
}
