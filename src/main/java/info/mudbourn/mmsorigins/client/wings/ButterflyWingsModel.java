package info.mudbourn.mmsorigins.client.wings;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * The fairy's butterfly wings, ported from Fantastic Wings' insectoid wing model.
 *
 * <p>Two zero-thickness cards, one per wing, textured back to back on a 64x64 sheet.
 * The cards ride the player's body bone and sweep about their inner edge; the sweep
 * angle is supplied per frame by {@link ButterflyFlap} through the render state, so the
 * model itself only reads an angle and never tracks time.
 */
public final class ButterflyWingsModel extends Model<AvatarRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("mms_origins", "butterfly_wings"), "main");

    private final ModelPart wingLeft;
    private final ModelPart wingRight;

    public ButterflyWingsModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.wingLeft = root.getChild("left_wing");
        this.wingRight = root.getChild("right_wing");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "left_wing",
                CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -8.0F, 0.0F, 19.0F, 24.0F, 0.0F),
                PartPose.offset(0.0F, 2.0F, 2.5F));
        root.addOrReplaceChild(
                "right_wing",
                CubeListBuilder.create().texOffs(0, 24).addBox(-19.0F, -8.0F, 0.0F, 19.0F, 24.0F, 0.0F),
                PartPose.offset(0.0F, 2.0F, 2.5F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(AvatarRenderState state) {
        super.setupAnim(state);
        float radians = (float) Math.toRadians(((WingState) state).mmsOrigins$wingFlapDegrees());
        this.wingLeft.yRot = radians;
        this.wingRight.yRot = -radians;
    }
}
