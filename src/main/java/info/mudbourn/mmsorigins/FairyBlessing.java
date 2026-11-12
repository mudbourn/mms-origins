package info.mudbourn.mmsorigins;

import io.github.apace100.calio.data.SerializableData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Fairy's Blessing: other players near fairies find their nurturing powers
 * strengthened. Each nearby fairy adds a share of the boost, which is full once
 * {@link #FAIRIES_FOR_FULL_BLESSING} of them are near. Read by the growth,
 * harvest and diet powers that fairies boost.
 */
public final class FairyBlessing {

    private static final double RADIUS = 12.0;

    /** How many nearby fairies give the full blessing. */
    public static final int FAIRIES_FOR_FULL_BLESSING = 3;

    /** The blessing's strength, from 0 with no fairy near to 1 at full stack. */
    public static float strength(Entity entity) {
        double radiusSqr = RADIUS * RADIUS;
        int fairies = 0;
        for (Player player : entity.level().players()) {
            if (player == entity || !player.isAlive() || player.isSpectator()) {
                continue;
            }
            if (player.distanceToSqr(entity) <= radiusSqr && MmsOriginsPowers.FAIRY_BLESSING.isActive(player)) {
                fairies++;
            }
        }
        return Math.min(1.0F, fairies / (float) FAIRIES_FOR_FULL_BLESSING);
    }

    /** Passes with a chance equal to the blessing's strength, usable as an entity condition. */
    public static boolean condition(SerializableData.Instance data, Entity entity) {
        return entity.getRandom().nextFloat() < strength(entity);
    }

    private FairyBlessing() {
    }
}
