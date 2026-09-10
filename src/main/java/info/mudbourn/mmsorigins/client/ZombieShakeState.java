package info.mudbourn.mmsorigins.client;

/**
 * Carries onto a living render state whether the player's zombification has
 * passed the halfway mark. Stamped during player extraction and read back when
 * the renderer decides whether the body should shake, the same way the freezing
 * quiver of powder snow is driven.
 */
public interface ZombieShakeState {

    boolean mmsOrigins$zombieShaking();

    void mmsOrigins$setZombieShaking(boolean shaking);
}
