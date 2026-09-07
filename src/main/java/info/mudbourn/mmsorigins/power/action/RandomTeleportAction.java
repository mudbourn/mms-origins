package info.mudbourn.mmsorigins.power.action;

import java.util.Optional;
import java.util.function.Consumer;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.Vec3;

/**
 * Teleports the entity to a random valid position within an area, mirroring the
 * newer Apoli {@code random_teleport}. Legacy Apoli lacks it, so this ships the
 * behaviour for the enderian projectile dodge.
 */
public final class RandomTeleportAction {

    private static final int MAX_ATTEMPTS = 64;

    public static SerializableData data() {
        return new SerializableData()
            .add("area_width", SerializableDataTypes.INT, 8)
            .add("area_height", SerializableDataTypes.INT, 8)
            .add("landing_offset", SerializableDataTypes.VECTOR, Vec3.ZERO)
            .add("landing_block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
            .add("success_action", ApoliDataTypes.ENTITY_ACTION, null)
            .add("failure_action", ApoliDataTypes.ENTITY_ACTION, null);
    }

    public static void action(SerializableData.Instance data, Entity entity) {
        Level level = entity.level();
        if (level.isClientSide()) {
            return;
        }
        int width = data.getInt("area_width");
        int height = data.getInt("area_height");
        Vec3 offset = data.get("landing_offset");
        Optional<ConditionFactory<BlockInWorld>.Instance> landing =
            Optional.ofNullable(data.get("landing_block_condition"));
        RandomSource random = entity.getRandom();
        BlockPos origin = entity.blockPosition();
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            int dx = random.nextInt(width * 2 + 1) - width;
            int dy = random.nextInt(height * 2 + 1) - height;
            int dz = random.nextInt(width * 2 + 1) - width;
            BlockPos target = origin.offset(dx, dy, dz);
            if (landing.isPresent()
                && !landing.get().test(new BlockInWorld(level, target, true))) {
                continue;
            }
            entity.teleportTo(
                target.getX() + 0.5 + offset.x,
                target.getY() + offset.y,
                target.getZ() + 0.5 + offset.z);
            runAction(data, "success_action", entity);
            return;
        }
        runAction(data, "failure_action", entity);
    }

    private static void runAction(SerializableData.Instance data, String field, Entity entity) {
        Consumer<Entity> onAction = data.get(field);
        if (onAction != null) {
            onAction.accept(entity);
        }
    }

    private RandomTeleportAction() {
    }
}
