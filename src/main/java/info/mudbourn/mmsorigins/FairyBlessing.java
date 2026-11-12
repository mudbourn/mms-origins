package info.mudbourn.mmsorigins;

import io.github.apace100.calio.data.SerializableData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Fairy's Blessing: other players near a fairy find their nurturing powers
 * strengthened. Read by the growth, harvest and diet powers that a fairy boosts.
 */
public final class FairyBlessing {

    private static final double RADIUS = 12.0;

    /** Whether a fairy other than the entity itself stands within {@link #RADIUS} blocks. */
    public static boolean isBlessed(Entity entity) {
        double radiusSqr = RADIUS * RADIUS;
        for (Player player : entity.level().players()) {
            if (player == entity || !player.isAlive() || player.isSpectator()) {
                continue;
            }
            if (player.distanceToSqr(entity) <= radiusSqr && MmsOriginsPowers.FAIRY_BLESSING.isActive(player)) {
                return true;
            }
        }
        return false;
    }

    /** Fairy's Blessing check usable as an entity condition. */
    public static boolean condition(SerializableData.Instance data, Entity entity) {
        return isBlessed(entity);
    }

    private FairyBlessing() {
    }
}
