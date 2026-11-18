package info.mudbourn.mmsorigins.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import info.mudbourn.mmsorigins.MmsOrigins;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** The Grovekeeper's right arm drawn in first person for the player riding it. */
public final class GrovekeeperArm {

    private static final Identifier TEXTURE =
        Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "textures/entity/grovekeeper.png");
    private static final Identifier LEAVES_TEXTURE =
        Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "textures/entity/grovekeeper_layer.png");
    private static final float SWING_DEGREES = 70.0f;

    private static ModelPart arm;

    /** Submits the arm held out ahead and low on the right, swung forward by the swing progress. */
    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, float swing) {
        ModelPart part = arm();
        poseStack.pushPose();
        poseStack.translate(0.6f, -0.75f, -0.95f);
        poseStack.scale(0.04f, -0.04f, 0.04f);
        poseStack.rotate(Axis.XP.rotationDegrees(-75.0f - (float) Math.sin(swing * Math.PI) * SWING_DEGREES));
        collector.submitModelPart(
            part,
            poseStack,
            RenderTypes.entityCutout(TEXTURE),
            light,
            OverlayTexture.NO_OVERLAY,
            null);
        collector.submitModelPart(
            part,
            poseStack,
            RenderTypes.entityCutout(LEAVES_TEXTURE),
            light,
            OverlayTexture.NO_OVERLAY,
            null);
        poseStack.popPose();
    }

    private static ModelPart arm() {
        if (arm == null) {
            MeshDefinition mesh = new MeshDefinition();
            mesh.getRoot().addOrReplaceChild(
                "arm",
                CubeListBuilder.create()
                    .texOffs(22, 13)
                    .addBox(-2.0f, -1.5f, -1.5f, 3.0f, 21.0f, 3.0f)
                    .texOffs(46, 0)
                    .addBox(-2.0f, 19.5f, -1.5f, 3.0f, 4.0f, 3.0f),
                PartPose.ZERO);
            arm = LayerDefinition.create(mesh, 64, 64).bakeRoot().getChild("arm");
        }
        return arm;
    }

    private GrovekeeperArm() {
    }
}
