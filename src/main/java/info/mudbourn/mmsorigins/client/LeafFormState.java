package info.mudbourn.mmsorigins.client;

import net.minecraft.world.phys.Vec3;

/**
 * Carries onto a living render state whether a floran is folded into leaf form.
 */
public interface LeafFormState {

    boolean mmsOrigins$leafForm();

    void mmsOrigins$setLeafForm(boolean leafForm);

    Vec3 mmsOrigins$leafTail();

    void mmsOrigins$setLeafTail(Vec3 tail);
}
