package info.mudbourn.mmsorigins.power.action;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dashes the entity a fixed distance along its movement input, falling back to its look
 * direction when it is not pressing a movement key.
 */
public final class LaunchAction {

    private static final int DASH_TICKS = 4;

    private static final double EXIT_SPEED_FACTOR = 0.25;

    private static final Map<UUID, Dash> ACTIVE = new ConcurrentHashMap<>();

    private record Dash(Entity entity, Vec3 velocity, int ticksLeft) {
    }

    public static SerializableData data() {
        return new SerializableData()
            .add("distance", SerializableDataTypes.DOUBLE, 6.0);
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> tick());
    }

    public static void action(SerializableData.Instance data, Entity entity) {
        if (entity.level().isClientSide()) {
            return;
        }
        Vec3 direction = movementDirection(entity);
        Vec3 velocity = direction.scale(data.getDouble("distance") / DASH_TICKS);
        ACTIVE.put(entity.getUUID(), new Dash(entity, velocity, DASH_TICKS));
        push(entity, velocity);
    }

    private static void tick() {
        Iterator<Map.Entry<UUID, Dash>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Dash> entry = it.next();
            Dash dash = entry.getValue();
            int left = dash.ticksLeft() - 1;
            if (dash.entity().isRemoved() || left <= 0) {
                if (!dash.entity().isRemoved()) {
                    push(dash.entity(), dash.velocity().scale(EXIT_SPEED_FACTOR));
                }
                it.remove();
                continue;
            }
            push(dash.entity(), dash.velocity());
            entry.setValue(new Dash(dash.entity(), dash.velocity(), left));
        }
    }

    private static void push(Entity entity, Vec3 velocity) {
        entity.setDeltaMovement(velocity);
        entity.hurtMarked = true;
    }

    private static Vec3 movementDirection(Entity entity) {
        Vec3 look = entity.getLookAngle();
        if (!(entity instanceof ServerPlayer player)) {
            return look;
        }
        Input input = player.getLastClientInput();
        double forward = (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0);
        double strafe = (input.left() ? 1 : 0) - (input.right() ? 1 : 0);
        if (forward == 0 && strafe == 0) {
            return look;
        }
        Vec3 flatLook = new Vec3(look.x, 0, look.z).normalize();
        Vec3 left = new Vec3(flatLook.z, 0, -flatLook.x);
        Vec3 forwardAxis = forward > 0 ? look : flatLook;
        return forwardAxis.scale(forward).add(left.scale(strafe)).normalize();
    }

    private LaunchAction() {
    }
}
