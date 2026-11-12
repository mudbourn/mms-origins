package info.mudbourn.mmsorigins.power;

import net.minecraft.world.entity.player.Player;

/**
 * A flight power whose pace builds while its holder keeps surging. The holder's
 * own client reads it each tick and sets its flying speed locally, so the pace
 * never rides on an abilities packet.
 */
public interface SurgingFlight {

    int BOOST_DELAY_TICKS = 30;
    int BOOST_RAMP_TICKS = 40;

    /** The flying speed while not surging. */
    float cruiseSpeed(Player player);

    /** The flying speed while surging, for a build from 0 to 1. */
    float surgeSpeed(Player player, float build);

    /** Whether the holder is surging. */
    boolean surging(Player player);

    /** How far the surge has built after the given ticks of surging, eased from 0 to 1. */
    static float build(int surgeTicks) {
        float progress = Math.clamp((surgeTicks - BOOST_DELAY_TICKS) / (float) BOOST_RAMP_TICKS, 0.0F, 1.0F);
        return progress * progress * (3.0F - 2.0F * progress);
    }
}
