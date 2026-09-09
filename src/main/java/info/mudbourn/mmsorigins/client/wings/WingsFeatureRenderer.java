package info.mudbourn.mmsorigins.client.wings;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

/**
 * Draws an elytrian's chosen Icarus wing on the vanilla elytra model.
 *
 * <p>Ported from Icarus Wings' own layer. The wing is stamped onto the render state from
 * the {@code elytrian_options} choice, then drawn here in place of the datapack elytra:
 * folded while grounded, and for a synapse wing swapped to its spread texture and scaled
 * by its expansion factor while the player is gliding. A wearer with a real elytra in the
 * chest slot is left to the vanilla elytra layer, matching the origin's own condition.
 */
public class WingsFeatureRenderer extends RenderLayer<AvatarRenderState, PlayerModel> {

    private final ElytraModel model;

    public WingsFeatureRenderer(RenderLayerParent<AvatarRenderState, PlayerModel> parent, ModelPart root) {
        super(parent);
        this.model = new ElytraModel(root);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light,
                       AvatarRenderState state, float yRot, float xRot) {
        if (state.isInvisible) {
            return;
        }
        if (state.chestEquipment.is(Items.ELYTRA)) {
            return;
        }
        WingModels.Wing wing = WingModels.resolve(((WingState) state).mmsOrigins$wingOption());
        if (wing == null) {
            return;
        }
        boolean expanded = state.isFallFlying && wing.expandable();
        Identifier texture = expanded ? wing.reversed() : wing.folded();
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.125F);
        if (expanded) {
            float factor = wing.expansionFactor();
            poseStack.scale(factor, factor, 1.0F);
        }
        this.model.setupAnim(state);
        collector.submitModel(this.model,
                state,
                poseStack,
                RenderTypes.armorCutoutNoCull(texture),
                light,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor,
                null);
        poseStack.popPose();
    }
}
