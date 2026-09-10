package info.mudbourn.mmsorigins.power.condition;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * True when the collision below the entity's feet is at least {@code min} blocks
 * away. Apoli's {@code distance_to_ground} counts whole blocks, so it cannot
 * tell walking a dirt path (a 15/16 block, a sliver of air underfoot) from
 * being airborne. This casts straight down and reports a miss, so a fractional
 * gap like half a block is expressible.
 */
public final class HeightAboveGroundCondition {

    public static SerializableData data() {
        return new SerializableData()
            .add("min", SerializableDataTypes.DOUBLE, 0.5);
    }

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        double min = data.getDouble("min");
        Vec3 from = entity.position();
        Vec3 to = from.subtract(0.0, min, 0.0);
        HitResult hit = entity.level().clip(
            new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        return hit.getType() == HitResult.Type.MISS;
    }

    private HeightAboveGroundCondition() {
    }
}
