package info.mudbourn.mmsorigins.client.wings;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Per-player butterfly flap state, ported from Fantastic Wings' AnimatorInsectoid.
 *
 * <p>The wings sweep about a resting yaw of -42 degrees with a 35 degree amplitude;
 * the flap rate eases from a gentle idle toward a fast lift rate while the player is
 * airborne, so grounded fairies rest their wings and flying ones beat them. Each player
 * keeps its own cycle, advanced once per client tick, and the layer samples it with the
 * frame's partial tick for a smooth sweep. Airborne is read from {@code onGround}, which
 * is synced for every player, so remote fairies flap correctly without extra state.
 */
public final class ButterflyFlap {

    private static final float IDLE_FLAP_RATE = 0.05F;
    private static final float LIFT_FLAP_RATE = 1.2F;
    private static final float REST_DEGREES = -42.0F;
    private static final float AMPLITUDE_DEGREES = 35.0F;

    private static final Map<Player, ButterflyFlap> STATES = new WeakHashMap<>();

    private float flapRate;
    private float flapCycle;
    private float prevFlapCycle;

    private ButterflyFlap() {
    }

    /** Advances every tracked player's flap one tick, choosing the rate from airborne state. */
    public static void tickAll(Iterable<? extends Player> players) {
        for (Player player : players) {
            STATES.computeIfAbsent(player, key -> new ButterflyFlap()).tick(!player.onGround());
        }
    }

    /** The wing yaw in degrees for a player this frame, or the resting yaw if untracked. */
    public static float degreesFor(Player player, float partialTick) {
        ButterflyFlap flap = STATES.get(player);
        return flap == null ? REST_DEGREES : flap.degrees(partialTick);
    }

    private void tick(boolean airborne) {
        float target = airborne ? LIFT_FLAP_RATE : IDLE_FLAP_RATE;
        this.prevFlapCycle = this.flapCycle;
        this.flapCycle += this.flapRate;
        this.flapRate += (target - this.flapRate) * 0.4F;
    }

    private float degrees(float partialTick) {
        float cycle = Mth.lerp(partialTick, this.prevFlapCycle, this.flapCycle);
        return Mth.sin(cycle) * AMPLITUDE_DEGREES + REST_DEGREES;
    }
}
