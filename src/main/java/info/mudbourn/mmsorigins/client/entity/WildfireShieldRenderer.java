package info.mudbourn.mmsorigins.client.entity;

import com.faboslav.friendsandfoes.common.init.FriendsAndFoesEntityModelLayers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import info.mudbourn.mmsorigins.entity.WildfireShield;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Draws a flung wildfire shield with the front shield plate of Friends and Foes' wildfire
 * model and texture, facing along its flight and spinning about it, lit at full brightness.
 */
public class WildfireShieldRenderer extends EntityRenderer<WildfireShield, WildfireShieldRenderer.State> {

    private static final Identifier TEXTURE =
        Identifier.fromNamespaceAndPath("friendsandfoes", "textures/entity/wildfire/wildfire.png");
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final float MODEL_SCALE = 1.0f / 16.0f;
    private static final float SPIN_DEGREES_PER_TICK = 30.0f;

    private final ModelPart plate;

    public WildfireShieldRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.plate = context.bakeLayer(FriendsAndFoesEntityModelLayers.WILDFIRE_LAYER)
            .getChild("shields")
            .getChild("frontShield");
        this.plate.x = 0.0f;
        this.plate.y = 0.0f;
        this.plate.z = 0.0f;
        this.plate.xRot = 0.0f;
        this.plate.yRot = 0.0f;
        this.plate.zRot = 0.0f;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WildfireShield shield, State state, float partialTick) {
        super.extractRenderState(shield, state, partialTick);
        state.yRot = shield.getYRot(partialTick);
        state.xRot = shield.getXRot(partialTick);
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.rotate(Axis.YP.rotationDegrees(state.yRot));
        pose.rotate(Axis.XP.rotationDegrees(-state.xRot));
        pose.rotate(Axis.ZP.rotationDegrees(state.ageInTicks * SPIN_DEGREES_PER_TICK));
        pose.scale(MODEL_SCALE, -MODEL_SCALE, MODEL_SCALE);
        pose.translate(0.0f, -12.0f, 8.5f);
        collector.submitModelPart(
            this.plate,
            pose,
            RenderTypes.entityCutout(TEXTURE),
            FULL_BRIGHT,
            OverlayTexture.NO_OVERLAY,
            null);
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }

    /** The shield's facing along its flight. */
    public static class State extends EntityRenderState {

        float yRot;
        float xRot;
    }
}
