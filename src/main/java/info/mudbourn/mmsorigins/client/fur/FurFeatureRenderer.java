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
 * Draws a player's fur geometry over the vanilla model.
 *
 * <p>A feature layer rather than a model replacement because the ported furs' geo is
 * additive: a set of fins and a face card meant to sit on top of the player's body.
 * The body overlay is not drawn here; it replaces the player's skin texture on the
 * render state during extraction, so a fur with no geo (truffle) draws nothing here.
 */
public class FurFeatureRenderer extends RenderLayer<AvatarRenderState, PlayerModel> {

    public FurFeatureRenderer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light,
                       AvatarRenderState state, float yRot, float xRot) {
        FurModels.Fur fur = FurModels.resolve(((FurState) state).mmsOrigins$furOrigin());
        if (fur == null || fur.model() == null) {
            return;
        }
        fur.model().submit(poseStack, collector,
                RenderTypes.entityCutoutNoCull(fur.texture()),
                this.getParentModel().root(),
                light,
                OverlayTexture.NO_OVERLAY,
                0xFFFFFFFF);
    }
}
