package info.mudbourn.mmsorigins.power.condition;

import info.mudbourn.mmsorigins.entity.FloranHenchman;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Counts the floran henchmen an entity currently owns nearby and compares that
 * tally against a fixed number. Sunlit Fury reads it to burn Sun Power faster
 * the larger the pack the caster is leaning on.
 */
public final class HenchmenCountCondition {

    private static final double SEARCH_RADIUS = 96.0;

    public static SerializableData data() {
        return new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
            .add("compare_to", SerializableDataTypes.INT, 1);
    }

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof LivingEntity owner) || !(entity.level() instanceof Level level)) {
            return false;
        }
        AABB search = owner.getBoundingBox().inflate(SEARCH_RADIUS);
        int count = level.getEntitiesOfClass(
            FloranHenchman.class, search, henchman -> owner.equals(henchman.getOwner())).size();
        Comparison comparison = data.get("comparison");
        return comparison.compare(count, data.getInt("compare_to"));
    }

    private HenchmenCountCondition() {
    }
}
