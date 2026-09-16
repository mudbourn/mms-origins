package info.mudbourn.mmsorigins;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import java.lang.reflect.Method;

/**
 * Reads whether AbsolutRevive has a player downed, without a compile dependency
 * on the mod.
 *
 * <p>AbsolutRevive makes every player a {@code AbsolutReviveDamageModelHolder}
 * carrying a {@code PlayerDamageModel}; a downed player's model reports itself
 * unconscious or in critical condition. Those are the mod's own class and method
 * names, not Minecraft mappings, so they survive remapping and reflection on
 * them is stable. Everything resolves once and degrades to a no-op when the mod
 * is absent, so origins gate on the downed state only when AbsolutRevive is in
 * play.
 */
public final class AbsolutReviveState {

    private static final String HOLDER_CLASS =
        "goetic.mods.absolutrevive.common.AbsolutReviveDamageModelHolder";
    private static final String MODEL_CLASS =
        "goetic.mods.absolutrevive.common.damagesystem.PlayerDamageModel";

    private static final Method GET_MODEL = resolveGetModel();
    private static final Method IS_UNCONSCIOUS = resolveModelMethod("isUnconscious");
    private static final Method IS_CRITICAL = resolveModelMethod("isCriticalConditionActive");

    public static boolean isIncapacitated(Entity entity) {
        if (GET_MODEL == null || IS_UNCONSCIOUS == null || !(entity instanceof Player)) {
            return false;
        }
        try {
            Object model = GET_MODEL.invoke(entity);
            if (model == null) {
                return false;
            }
            if ((Boolean) IS_UNCONSCIOUS.invoke(model)) {
                return true;
            }
            return IS_CRITICAL != null && (Boolean) IS_CRITICAL.invoke(model);
        } catch (ReflectiveOperationException | ClassCastException e) {
            return false;
        }
    }

    private static Method resolveGetModel() {
        if (!FabricLoader.getInstance().isModLoaded("absolutrevive")) {
            return null;
        }
        try {
            return Class.forName(HOLDER_CLASS).getMethod("absolutrevive$getDamageModelNullable");
        } catch (ReflectiveOperationException e) {
            MmsOrigins.LOGGER.warn("AbsolutRevive present but its damage-model bridge could not be found; the downed gate is off.", e);
            return null;
        }
    }

    private static Method resolveModelMethod(String name) {
        if (!FabricLoader.getInstance().isModLoaded("absolutrevive")) {
            return null;
        }
        try {
            return Class.forName(MODEL_CLASS).getMethod(name);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private AbsolutReviveState() {
    }
}
