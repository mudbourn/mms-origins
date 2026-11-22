package info.mudbourn.mmsorigins;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.TogglePower;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Forces a downed player back out of a shapeshift (floran leaf form, phantom form). "Downed" is AbsolutRevive's
 * unconscious state, read by reflection so that mod stays optional.
 */
public final class DownedUnshift {

    private static final List<PowerType<?>> FORMS = List.of(MmsOriginsPowers.LEAF_FORM, MmsOriginsPowers.PHANTOM_FORM);
    private static final Method GET_MODEL;
    private static final Method IS_UNCONSCIOUS;

    static {
        Method getModel = null;
        Method isUnconscious = null;
        try {
            Class<?> holder = Class.forName("goetic.mods.absolutrevive.common.AbsolutReviveDamageModelHolder");
            getModel = holder.getMethod("absolutrevive$getDamageModelNullable");
            isUnconscious = getModel.getReturnType().getMethod("isUnconscious");
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // AbsolutRevive isn't installed, so nobody is ever downed.
        }
        GET_MODEL = getModel;
        IS_UNCONSCIOUS = isUnconscious;
    }

    private DownedUnshift() {
    }

    public static void register() {
        if (GET_MODEL == null) return;
        ServerTickEvents.END_LEVEL_TICK.register(DownedUnshift::onEndLevelTick);
    }

    private static void onEndLevelTick(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            if (!downed(player)) continue;
            for (PowerType<?> form : FORMS) {
                Power power = form.get(player);
                if (power instanceof TogglePower toggle && toggle.isActive()) {
                    toggle.onUse();
                    PowerHolderComponent.syncPower(player, form);
                }
            }
        }
    }

    private static boolean downed(Player player) {
        if (!GET_MODEL.getDeclaringClass().isInstance(player)) return false;
        try {
            Object model = GET_MODEL.invoke(player);
            return model != null && (boolean) IS_UNCONSCIOUS.invoke(model);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}
