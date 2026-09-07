package info.mudbourn.mmsorigins.client.fur;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;

/**
 * Draws a player's fur over the vanilla model.
 *
 * <p>Two additive passes, both over the player's own skin. The body overlay is the
 * vanilla player model redrawn with the fur's skin-layout texture on a translucent
 * render type, so its painted pixels composite over the opaque base skin while its
 * transparent pixels leave the skin showing. The geo fins are extra bedrock geometry
 * submitted on top. A fur may have either pass, both, or (for an overlay-only fur)
 * just the overlay.
 */
public class FurFeatureRenderer extends RenderLayer<AvatarRenderState, PlayerModel> {

    public FurFeatureRenderer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light,
                       AvatarRenderState state, float yRot, float xRot) {
        FurModels.Fur fur = FurModels.resolve(((FurState) state).mmsOrigins$furOrigin());
        if (fur == null) {
            return;
        }
        Identifier overlay = overlayFor(fur, state);
        if (overlay != null) {
            // Third int is the outline color, not the tint; this overload tints white.
            collector.submitModel(this.getParentModel(),
                    state,
                    poseStack,
                    RenderTypes.entityTranslucent(overlay),
                    light,
                    LivingEntityRenderer.getOverlayCoords(state, 0.0F),
                    state.outlineColor,
                    null);
        }
        if (fur.emissive() != null) {
            collector.submitModel(this.getParentModel(),
                    state,
                    poseStack,
                    RenderTypes.entityTranslucentEmissive(fur.emissive()),
                    LightTexture.FULL_BRIGHT,
                    LivingEntityRenderer.getOverlayCoords(state, 0.0F),
                    state.outlineColor,
                    null);
        }
        if (fur.model() != null) {
            poseStack.pushPose();
            float[] offset = fur.offset();
            if (offset != null) {
                poseStack.translate(offset[0], offset[1], offset[2]);
            }
            fur.model().submit(poseStack, collector,
                    RenderTypes.entityCutoutNoCull(fur.texture()),
                    this.getParentModel().root(),
                    light,
                    OverlayTexture.NO_OVERLAY,
                    0xFFFFFFFF,
                    state.ageInTicks,
                    state.walkAnimationPos,
                    state.walkAnimationSpeed);
            poseStack.popPose();
        }
    }

    /** The overlay for this model, the slim variant when the skin is slim and one exists. */
    private static Identifier overlayFor(FurModels.Fur fur, AvatarRenderState state) {
        if (fur.overlaySlim() != null && state.skin != null
                && state.skin.model() == PlayerModelType.SLIM) {
            return fur.overlaySlim();
        }
        return fur.overlay();
    }
}
