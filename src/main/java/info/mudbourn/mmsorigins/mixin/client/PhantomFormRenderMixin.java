package info.mudbourn.mmsorigins.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws a morphed phantom as a vanilla phantom, through the vanilla phantom renderer, in place of
 * their body, armor and held items. The player's Apoli powers ride along so translucency and
 * invisibility apply to the phantom body too.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class PhantomFormRenderMixin {

    @Unique
    private static final PhantomRenderState PHANTOM = new PhantomRenderState();

    @Unique
    private static final int PHANTOM_SIZE = 2;

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
        mmsOrigins$copy(state, PHANTOM);
        Minecraft.getInstance()
            .getEntityRenderDispatcher()
            .getRenderer(PHANTOM)
            .submit(PHANTOM, pose, collector, camera);
        ci.cancel();
    }

    /** Fills the phantom render state from the player's so the phantom stands where the player does. */
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
        to.bodyRot = from.bodyRot;
        to.yRot = from.yRot;
        to.xRot = from.xRot;
        to.deathTime = from.deathTime;
        to.walkAnimationPos = from.walkAnimationPos;
        to.walkAnimationSpeed = from.walkAnimationSpeed;
        to.scale = from.scale;
        to.ageScale = from.ageScale;
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
