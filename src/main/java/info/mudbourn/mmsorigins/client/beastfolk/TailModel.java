package info.mudbourn.mmsorigins.client.beastfolk;

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
import net.minecraft.util.Mth;

/**
 * A single bushy canine tail, the one-tail form of ModelFluffyTail from Kihira's
 * Tails mod (MIT).
 *
 * <p>The tail swings from the body's motion the same way the vanilla cape does, so the
 * motion angles are rebuilt from the cape lean and flap already on the render state.
 * The idle sway runs on wall-clock time offset by the entity id, as in the source mod.
 */
public final class TailModel extends Model<AvatarRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("mms_origins", "beastfolk_tail"), "main");

    private static final int SEGMENTS = 6;

    private final ModelPart[] segments = new ModelPart[SEGMENTS];

    public TailModel(ModelPart root) {
        super(root, RenderTypes::entityCutoutNoCull);
        ModelPart part = root;
        for (int i = 0; i < SEGMENTS; i++) {
            part = part.getChild("tail" + i);
            this.segments[i] = part;
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        int[][] tex = {{0, 0}, {10, 0}, {0, 5}, {0, 13}, {0, 26}, {12, 26}};
        float[][] box = {
            {-1.0F, 2.0F, 3.0F},
            {-1.5F, 3.0F, 2.0F},
            {-2.0F, 4.0F, 4.0F},
            {-2.5F, 5.0F, 8.0F},
            {-2.0F, 4.0F, 2.0F},
            {-1.5F, 3.0F, 2.0F}
        };
        float[] offset = {0.0F, 1.5F, 1.5F, 3.0F, 7.4F, 1.4F};
        for (int i = 0; i < SEGMENTS; i++) {
            float min = box[i][0];
            float size = box[i][1];
            part = part.addOrReplaceChild(
                    "tail" + i,
                    CubeListBuilder.create().texOffs(tex[i][0], tex[i][1]).addBox(min, min, 0.0F, size, size, box[i][2]),
                    PartPose.offset(0.0F, 0.0F, offset[i]));
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(AvatarRenderState state) {
        super.setupAnim(state);
        float time = (float) (((state.id + System.currentTimeMillis()) % 4000L) / 4000.0 * Math.PI * 2.0);
        double pitch;
        double yaw;
        double roll;
        double sway;
        if (state.isPassenger) {
            pitch = Math.toRadians(22.0);
            yaw = 0.0;
            roll = 0.0;
            sway = 0.5;
        } else {
            pitch = Mth.clamp(Math.toRadians(state.capeLean / 2.5F + state.capeFlap) * 0.6, -1.0, 0.45);
            yaw = Math.toRadians(-state.capeLean2 / 20.0F);
            roll = Mth.clamp(Math.toRadians(state.capeLean2 / 2.0F), -0.5, 0.5);
            sway = 1.0 - pitch * 2.0;
        }
        this.root().xRot = (float) Math.toRadians(-20.0);
        double bend = Math.abs(roll / 2.0);
        rotate(0, pitch, (-roll / 2.0 + Math.cos(time + 1.0) / 8.0) * sway + yaw, -roll / 8.0);
        rotate(1, Math.toRadians(-15.0) + pitch + bend, (-roll / 2.0 + Math.cos(time) / 8.0) * sway, -roll / 8.0);
        rotate(2, Math.toRadians(-15.0) + pitch / 2.0, (-roll / 2.0 + Math.cos(time - 0.5) / 8.0) * sway, -roll / 8.0);
        rotate(3, Math.toRadians(-25.0) + pitch / 2.0, (-roll / 2.0 + Math.cos(time - 1.0) / 20.0) * sway, -roll / 20.0);
        rotate(4, Math.toRadians(15.0) - pitch / 2.0, (-roll / 2.0 + Math.cos(time - 2.0) / 8.0) * sway, 0.0);
        rotate(5, Math.toRadians(15.0) - pitch / 2.5, (-roll / 2.0 + Math.cos(time - 3.0) / 8.0) * sway, 0.0);
    }

    private void rotate(int segment, double x, double y, double z) {
        ModelPart part = this.segments[segment];
        part.xRot = (float) x;
        part.yRot = (float) y;
        part.zRot = (float) z;
    }
}
