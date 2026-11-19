package info.mudbourn.mmsorigins.power.action;

import info.mudbourn.mmsorigins.compat.CrawlOnDemandBridge;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Settles the entity as it leaves leaf form. It is lifted to the lowest spot within {@code max}
 * blocks above where its standing hitbox touches no solid block and left standing. Only when no
 * such spot exists and its body is inside solid blocks is it lifted to the lowest spot where its
 * crawling hitbox fits and put into Crawl on Demand's crawl; a body that is in air, or only
 * partly in blocks, is left to stand with the game's own pose rules. As a condition it reports
 * whether a standing spot exists, or with {@code crawl} set and Crawl on Demand loaded, always.
 */
public final class SurfaceAction {

    private static final double STEP = 0.0625;
    private static final double EPSILON = 1.0E-7;

    public static SerializableData data() {
        return new SerializableData()
            .add("max", SerializableDataTypes.DOUBLE, 1.0)
            .add("crawl", SerializableDataTypes.BOOLEAN, false);
    }

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        if (data.getBoolean("crawl") && CrawlOnDemandBridge.present()) {
            return true;
        }
        return findLift(entity, Pose.STANDING, data.getDouble("max")) >= 0;
    }

    public static void action(SerializableData.Instance data, Entity entity) {
        settle(entity, data.getDouble("max"));
    }

    /** Returns whether the entity was left crawling. */
    public static boolean settle(Entity entity, double max) {
        entity.stopRiding();
        double stand = findLift(entity, Pose.STANDING, max);
        if (stand >= 0) {
            raise(entity, stand);
            crawl(entity, false);
            return false;
        }
        double crawl = findLift(entity, Pose.SWIMMING, max);
        if (crawl == 0) {
            crawl(entity, false);
            return false;
        }
        raise(entity, crawl);
        crawl(entity, true);
        return true;
    }

    private static void raise(Entity entity, double lift) {
        if (lift > 0) {
            entity.teleportTo(entity.getX(), entity.getY() + lift, entity.getZ());
        }
    }

    private static void crawl(Entity entity, boolean crawling) {
        if (entity instanceof Player player) {
            CrawlOnDemandBridge.setCrawling(player, crawling);
        }
    }

    private static double findLift(Entity entity, Pose pose, double max) {
        AABB box = entity.getDimensions(pose).makeBoundingBox(entity.position());
        for (double lift = 0; lift <= max; lift += STEP) {
            if (isClear(entity.level(), box.move(0, lift, 0))) {
                return lift;
            }
        }
        return -1;
    }

    private static boolean isClear(Level level, AABB box) {
        return level.noBlockCollision(null, box.deflate(EPSILON));
    }

    private SurfaceAction() {
    }
}
