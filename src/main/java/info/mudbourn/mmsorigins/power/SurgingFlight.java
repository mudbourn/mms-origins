package info.mudbourn.mmsorigins.power;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * A flight power whose pace builds while its holder keeps surging. The holder's
 * own client reads it each tick and sets its flying speed locally, so the pace
 * never rides on an abilities packet. A mob that implements {@link FlyingMob}
 * asks the same power whether it may fly and how fast.
 */
public interface SurgingFlight {

    int FIRST_RAMP_TICKS = 60;
    int HOLD_TICKS = 100;
    int SECOND_RAMP_TICKS = 40;
    float FIRST_TOP_SPEED = 0.09F;
    float SECOND_TOP_SPEED = 0.10125F;

    /** Whether the power's rules let the holder fly right now. */
    boolean allowed(LivingEntity entity);

    /** The flying speed while not surging. */
    float cruiseSpeed(LivingEntity entity);

    /** Whether the holder is surging. */
    default boolean surging(LivingEntity entity) {
        return entity.isSprinting();
    }

    /** The flying speed after the given ticks of surging, starting from the cruise speed. */
    static float surgeSpeed(float cruise, int surgeTicks) {
        if (surgeTicks <= FIRST_RAMP_TICKS) {
            return lerp(cruise, FIRST_TOP_SPEED, surgeTicks / (float) FIRST_RAMP_TICKS);
        }
        float second = (surgeTicks - FIRST_RAMP_TICKS - HOLD_TICKS) / (float) SECOND_RAMP_TICKS;
        return lerp(FIRST_TOP_SPEED, SECOND_TOP_SPEED, second);
    }

    /** The hunger a holder's food rules read: a player's food level, or a mob's own. */
    static int foodLevel(LivingEntity entity) {
        if (entity instanceof Player player) {
            return player.getFoodData().getFoodLevel();
        }
        return entity instanceof FlyingMob mob ? mob.foodLevel() : 20;
    }

    private static float lerp(float from, float to, float progress) {
        float eased = Math.clamp(progress, 0.0F, 1.0F);
        eased = eased * eased * (3.0F - 2.0F * eased);
        return Math.max(from, from + (to - from) * eased);
    }
}
