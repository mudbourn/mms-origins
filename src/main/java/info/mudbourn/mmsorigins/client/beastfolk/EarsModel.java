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
 * A pair of pointed canine ears built from single-pixel blocks, ported from
 * ModelFoxEars in Kihira's Tails mod (MIT).
 *
 * <p>The blocks are laid out about the head's pivot, so the renderer only has to move
 * into the head's pose before drawing them.
 */
public final class EarsModel extends Model<AvatarRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath("mms_origins", "beastfolk_ears"), "main");

    // Per block: u, v, width, height, depth, x, y, z, and 1 when mirrored.
    private static final int[][] BLOCKS = {
        {0, 16, 1, 1, 1, 4, -11, 1, 1},
        {4, 16, 2, 2, 1, 3, -10, 1, 1},
        {0, 0, 1, 3, 1, 2, -10, 1, 1},
        {4, 0, 1, 1, 1, 3, -11, 1, 1},
        {4, 4, 1, 1, 1, 4, -12, 1, 1},
        {0, 8, 1, 3, 1, 5, -11, 1, 1},
        {10, 14, 2, 1, 1, 3, -8, 1, 1},
        {4, 8, 1, 3, 1, 4, -11, 2, 1},
        {8, 0, 1, 2, 1, 3, -10, 2, 1},
        {0, 19, 1, 1, 1, -5, -11, 1, 0},
        {4, 19, 2, 2, 1, -5, -10, 1, 0},
        {0, 4, 1, 3, 1, -3, -10, 1, 0},
        {4, 2, 1, 1, 1, -4, -11, 1, 0},
        {4, 6, 1, 1, 1, -5, -12, 1, 0},
        {0, 12, 1, 3, 1, -6, -11, 1, 0},
        {10, 12, 2, 1, 1, -5, -8, 1, 0},
        {4, 12, 1, 3, 1, -5, -11, 2, 0},
        {8, 3, 1, 2, 1, -4, -10, 2, 0}
    };

    public EarsModel(ModelPart root) {
        super(root, RenderTypes::entityCutoutNoCull);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        for (int i = 0; i < BLOCKS.length; i++) {
            int[] b = BLOCKS[i];
            mesh.getRoot().addOrReplaceChild(
                    "ear" + i,
                    CubeListBuilder.create().texOffs(b[0], b[1]).mirror(b[8] == 1).addBox(0.0F, 0.0F, 0.0F, b[2], b[3], b[4]),
                    PartPose.offset(b[5], b[6], b[7]));
        }
        return LayerDefinition.create(mesh, 16, 32);
    }
}
