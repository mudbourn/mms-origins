package info.mudbourn.mmsorigins.client.entity;

import info.mudbourn.mmsorigins.entity.FloranHenchman;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.animation.definitions.CreakingAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.creaking.CreakingModel;
import net.minecraft.client.renderer.entity.state.CreakingRenderState;

/**
 * The creaking model with its keyframes played back gentler: a slower, shallower
 * walk and a drawn-out swing in place of the creaking's jittery lunge.
 */
public class GrovekeeperModel extends CreakingModel {

    private static final float WALK_TIME_SCALE = 0.7f;
    private static final float WALK_AMPLITUDE_SCALE = 0.6f;

    private final ModelPart head;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;

    public GrovekeeperModel(ModelPart root) {
        super(root);
        ModelPart body = root.getChild("root");
        this.head = body.getChild("upper_body").getChild("head");
        this.walkAnimation = CreakingAnimation.CREAKING_WALK.bake(body);
        this.attackAnimation = CreakingAnimation.CREAKING_ATTACK.bake(body);
    }

    @Override
    public void setupAnim(CreakingRenderState state) {
        this.resetPose();
        this.head.xRot = state.xRot * ((float) Math.PI / 180.0f);
        this.head.yRot = state.yRot * ((float) Math.PI / 180.0f);
        if (state.canMove) {
            this.walkAnimation.applyWalk(
                state.walkAnimationPos,
                state.walkAnimationSpeed,
                WALK_TIME_SCALE,
                WALK_AMPLITUDE_SCALE);
        }
        this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks, FloranHenchman.ATTACK_ANIMATION_SPEED);
    }
}
