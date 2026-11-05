package info.mudbourn.mmsorigins.power.action;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Lifts the entity to the lowest spot within {@code max} blocks above it where its standing
 * hitbox touches no solid block, and as a condition reports whether such a spot exists.
 */
public final class SurfaceAction {

    private static final double STEP = 0.0625;

    public static SerializableData data() {
        return new SerializableData()
            .add("max", SerializableDataTypes.DOUBLE, 1.0);
    }

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        return findLift(entity, data.getDouble("max")) >= 0;
    }

    public static void action(SerializableData.Instance data, Entity entity) {
        double lift = findLift(entity, data.getDouble("max"));
        if (lift > 0) {
            entity.teleportTo(entity.getX(), entity.getY() + lift, entity.getZ());
        }
    }

    private static double findLift(Entity entity, double max) {
        AABB standing = entity.getDimensions(Pose.STANDING).makeBoundingBox(entity.position());
        for (double lift = 0; lift <= max; lift += STEP) {
            if (isClear(entity.level(), standing.move(0, lift, 0))) {
                return lift;
            }
        }
        return -1;
    }

    private static boolean isClear(Level level, AABB box) {
        AABB inset = box.deflate(0.001);
        for (BlockPos pos : BlockPos.betweenClosed(
            BlockPos.containing(inset.minX, inset.minY, inset.minZ),
            BlockPos.containing(inset.maxX, inset.maxY, inset.maxZ))) {
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private SurfaceAction() {
    }
}
