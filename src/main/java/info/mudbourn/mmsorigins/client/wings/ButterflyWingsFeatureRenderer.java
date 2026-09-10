package info.mudbourn.mmsorigins.client.wings;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Draws a fairy's chosen butterfly wing on the ported insectoid model.
 *
 * <p>Ported from Fantastic Wings' ModWingsLayer. The wings ride the body bone, nudged
 * back a little so they clear the torso, and flap by the angle {@link ButterflyFlap}
 * stamped onto the render state. A wearer with a chest item gets an extra nudge so the
 * wings clear the armor, matching the source mod.
 */
public class ButterflyWingsFeatureRenderer extends RenderLayer<AvatarRenderState, PlayerModel> {

    private final ButterflyWingsModel model;

    public ButterflyWingsFeatureRenderer(RenderLayerParent<AvatarRenderState, PlayerModel> parent, ModelPart root) {
        super(parent);
        this.model = new ButterflyWingsModel(root);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light,
                       AvatarRenderState state, float yRot, float xRot) {
        if (state.isInvisible) {
            return;
        }
        Identifier texture = ButterflyWingModels.resolve(((WingState) state).mmsOrigins$wingOption());
        if (texture == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0F, -0.0625F, 0.0F);
        if (!state.chestEquipment.isEmpty()) {
            poseStack.translate(0.0F, 0.0F, 0.0625F);
        }
        this.getParentModel().body.translateAndRotate(poseStack);
        this.model.setupAnim(state);
        collector.submitModel(this.model,
                state,
                poseStack,
                RenderTypes.entityCutout(texture),
                light,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null);
        poseStack.popPose();
    }
}
