package info.mudbourn.mmsorigins.client;

import net.minecraft.world.phys.Vec3;

/**
 * Carries a fishman's charging guardian beam onto a living render state.
 *
 * <p>The vector runs from the target's middle, {@code height} above its feet, to the
 * caster's eyes, and is null when no beam
 * is charging; time and scale feed the guardian beam renderer exactly as a
 * guardian's attack time and attack animation scale do.
 */
public interface GuardianBeamState {

    Vec3 mmsOrigins$beamVector();

    void mmsOrigins$setBeamVector(Vec3 vector);

    float mmsOrigins$beamHeight();

    void mmsOrigins$setBeamHeight(float height);

    float mmsOrigins$beamTime();

    void mmsOrigins$setBeamTime(float time);

    float mmsOrigins$beamScale();

    void mmsOrigins$setBeamScale(float scale);
}
