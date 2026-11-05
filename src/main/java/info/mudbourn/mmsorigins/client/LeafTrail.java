package info.mudbourn.mmsorigins.client;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Tracks the tail bush that trails a floran in leaf form, pulled along like a rope.
 */
public final class LeafTrail {

    private static final double LENGTH = 0.55;

    private static final double SNAP = 4.0;

    private static final float SLACK = 50.0F;

    private static final Map<Integer, Vec3> TAILS = new HashMap<>();

    private LeafTrail() {
    }

    /** Returns the tail's offset from the entity's rendered position. */
    public static Vec3 offset(LivingEntity entity, float partialTick) {
        Vec3 head = entity.getPosition(partialTick);
        Vec3 tail = TAILS.get(entity.getId());
        if (tail == null || tail.distanceTo(head) > SNAP) {
            Vec3 facing = Vec3.directionFromRotation(0.0F, entity.getYRot());
            tail = head.subtract(facing.scale(LENGTH));
        }
        Vec3 toTail = tail.subtract(head);
        if (toTail.length() > LENGTH) {
            toTail = toTail.normalize().scale(LENGTH);
        }
        float tailYaw = (float) Math.toDegrees(Math.atan2(-toTail.x, toTail.z));
        float behind = entity.getYHeadRot() + 180.0F;
        float drift = Mth.wrapDegrees(tailYaw - behind);
        if (Math.abs(drift) > SLACK) {
            float clamped = behind + Math.copySign(SLACK, drift);
            Vec3 flat = Vec3.directionFromRotation(0.0F, clamped).scale(Math.hypot(toTail.x, toTail.z));
            toTail = new Vec3(flat.x, toTail.y, flat.z);
        }
        tail = head.add(toTail);
        TAILS.put(entity.getId(), tail);
        return tail.subtract(head);
    }

    /** Forgets the tail of an entity that left leaf form. */
    public static void clear(Entity entity) {
        TAILS.remove(entity.getId());
    }
}
