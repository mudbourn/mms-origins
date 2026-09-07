package info.mudbourn.mmsorigins.client.fur;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Draws a player's fur over the vanilla model.
 *
 * <p>A feature layer rather than a model replacement because the ported furs are
 * additive: merling is a set of fins and a face card meant to sit on top of the
 * player's own skin, which is exactly what a feature layer over the base model is.
 * Full-body furs that need the vanilla model hidden are a later, per-fur concern.
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
        // The scaled torso and tail are a vanilla-skin-layout texture drawn onto the
        // player model itself, under the geo fins.
        renderColoredCutoutModel(this.getParentModel(), fur.overlay(), poseStack, collector,
                light, state, 0xFFFFFFFF, state.outlineColor);
        fur.model().submit(poseStack, collector,
                RenderTypes.entityCutoutNoCull(fur.texture()),
                this.getParentModel().root(),
                light,
                OverlayTexture.NO_OVERLAY,
                0xFFFFFFFF);
    }
}
