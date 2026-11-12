package info.mudbourn.mmsorigins.client;

/**
 * Carries onto a living render state whether a phantom has finished morphing into phantom form,
 * and the orientation its phantom body is drawn at.
 */
public interface PhantomFormState {

    boolean mmsOrigins$phantomForm();

    void mmsOrigins$setPhantomForm(boolean phantomForm);

    PhantomFlightPose.Angles mmsOrigins$phantomPose();

    void mmsOrigins$setPhantomPose(PhantomFlightPose.Angles pose);
}
