package info.mudbourn.mmsorigins.power;

/**
 * A non-player that flies on a flight power. The power reads its hunger and flight state
 * through this and applies the same rules it applies to a player: it clears {@link #setFlying}
 * when the rules no longer allow flight, and reports the cost of staying aloft to
 * {@link #exhaust}. Taking off, steering and landing stay with the mob.
 */
public interface FlyingMob {

    /** The hunger the power's food rules read, as a player's food level would be. */
    int foodLevel();

    /** Whether the mob is in the air on its wings. */
    boolean flying();

    /** Puts the mob in or out of the air; the power clears it when flight is not allowed. */
    void setFlying(boolean flying);

    /** Takes the exhaustion that flying costs a fed holder each tick. */
    default void exhaust(float amount) {
    }
}
