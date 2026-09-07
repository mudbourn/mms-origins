package info.mudbourn.mmsorigins.client.fur;

import info.mudbourn.mmsrendercommon.client.geo.BoneAnimator;
import net.minecraft.util.Mth;

import java.util.Set;

/**
 * Swings the piglin fur's ears exactly as vanilla swings a piglin's own ears.
 *
 * <p>The two ear bones ({@code bpL}, {@code bpR}) carry the roll {@code AbstractPiglinModel}
 * gives {@code leftEar} and {@code rightEar}: a rest splay of thirty degrees plus a cosine
 * idle flap whose amplitude opens up as the wearer walks. Vanilla writes that roll in the
 * ear's own model space; the fur geometry is authored mirrored, and the geo baker mirrors
 * a Bedrock rotation by negating its Z, so the roll is emitted here as its negation on the
 * Bedrock Z axis.
 */
public final class PiglinEarAnimator implements BoneAnimator {

    private static final float REST = 0.5235988F;
    private static final float DEGREES = 180.0F / (float) Math.PI;

    private static final Set<String> EARS = Set.of("bpL", "bpR");

    @Override
    public Set<String> animatedBones() {
        return EARS;
    }

    @Override
    public float[] rotation(String bone, float ageInTicks, float limbSwingPos, float limbSwingSpeed) {
        float phase = ageInTicks * 0.1F + limbSwingPos * 0.5F;
        float amplitude = 0.08F + limbSwingSpeed * 0.4F;
        return switch (bone) {
            case "bpL" -> zRotation(-REST - Mth.cos(phase * 1.2F) * amplitude);
            case "bpR" -> zRotation(REST + Mth.cos(phase) * amplitude);
            default -> null;
        };
    }

    /** A vanilla ear roll, in radians, as the Bedrock Z-degree vector the geo baker mirrors. */
    private static float[] zRotation(float roll) {
        return new float[] {0.0F, 0.0F, -roll * DEGREES};
    }
}
