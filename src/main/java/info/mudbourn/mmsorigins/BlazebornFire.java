package info.mudbourn.mmsorigins;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.world.entity.Entity;

/**
 * Remembers which entities are burning from a Blazeborn's flame, so that fire hurts them
 * without heating them toward Scorchful's heat stroke. The mark fades once they stop burning.
 */
public final class BlazebornFire {

    private static final Set<Entity> MARKED = Collections.newSetFromMap(new WeakHashMap<>());

    /** Sets the entity alight for the given seconds and marks the flame as a Blazeborn's. */
    public static void ignite(Entity entity, float seconds) {
        entity.igniteForSeconds(seconds);
        mark(entity);
    }

    public static void mark(Entity entity) {
        MARKED.add(entity);
    }

    /** Whether the entity is still burning from a Blazeborn's flame. */
    public static boolean burning(Entity entity) {
        if (!MARKED.contains(entity)) {
            return false;
        }
        if (!entity.isOnFire()) {
            MARKED.remove(entity);
            return false;
        }
        return true;
    }

    private BlazebornFire() {
    }
}
