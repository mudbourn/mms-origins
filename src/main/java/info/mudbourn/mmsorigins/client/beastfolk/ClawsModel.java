package info.mudbourn.mmsorigins.client.beastfolk;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Three claws under one of a beastfolk's hands, drawn once per arm while claw mode is on.
 *
 * <p>The claws stand in a front-to-back row centred on the arm's pivot, hanging from
 * the bottom of the hand, so the renderer moves into an arm's pose and out to the
 * hand's outer side before drawing them.
 */
public final class ClawsModel extends Model<AvatarRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("mms_origins", "beastfolk_claws"), "main");

    public ClawsModel(ModelPart root) {
        super(root, RenderTypes::entityCutoutNoCull);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        float[] zs = {-1.0F, 0.0F, 1.0F};
        for (int i = 0; i < zs.length; i++) {
            mesh.getRoot().addOrReplaceChild(
                    "claw" + i,
                    CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                    PartPose.offset(0.0F, 10.0F, zs[i]).withScale(0.5F));
        }
        return LayerDefinition.create(mesh, 16, 16);
    }
}
