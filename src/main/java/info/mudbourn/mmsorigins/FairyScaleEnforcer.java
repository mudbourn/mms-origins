package info.mudbourn.mmsorigins;

import info.mudbourn.mmsorigins.power.FlyingMob;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeReference;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Holds a fairy at its diminutive size, driven by the
 * {@code mms_origins:diminutive} marker.
 *
 * <p>A fairy stands one block shorter than a human: 0.8 blocks to a human's
 * 1.8. Apoli's {@code origins:attribute} applies its scale modifier once, when
 * the power is gained, and that single apply can lose the race on a fresh world
 * join so a fairy spawns full size. The scale is enforced here instead: every
 * server tick each diminutive player is checked, and the scale modifier is
 * re-added the moment it is missing, so a mis-timed join self-corrects within a
 * tick. Mobs that carry origins, such as mms-dungeons' faction humans, are
 * checked every half second.
 */
public final class FairyScaleEnforcer {

    private static final PowerType<?> MARKER =
        new PowerTypeReference<>(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "diminutive"));
    private static final Identifier SCALE_MODIFIER_ID =
        Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "fairy_scale");
    private static final double HUMAN_HEIGHT = 1.8;
    private static final double FAIRY_HEIGHT = 0.8;
    private static final double SCALE_MULTIPLIER = FAIRY_HEIGHT / HUMAN_HEIGHT - 1.0;
    private static final int MOB_CHECK_TICKS = 10;

    private FairyScaleEnforcer() {
    }

    public static void register() {
        ServerTickEvents.END_LEVEL_TICK.register(FairyScaleEnforcer::onEndLevelTick);
    }

    private static void onEndLevelTick(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            enforce(player);
        }
        if (level.getGameTime() % MOB_CHECK_TICKS == 0) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof FlyingMob && entity instanceof LivingEntity mob) {
                    enforce(mob);
                }
            }
        }
    }

    // Whether this creature is a fairy.
    static boolean fairy(LivingEntity entity) {
        return MmsOriginsPowers.holds(entity, MARKER);
    }

    private static void enforce(LivingEntity entity) {
        AttributeInstance scale = entity.getAttribute(Attributes.SCALE);
        if (scale == null) {
            return;
        }
        boolean fairy = fairy(entity);
        AttributeModifier applied = scale.getModifier(SCALE_MODIFIER_ID);
        if (fairy && (applied == null || applied.amount() != SCALE_MULTIPLIER)) {
            scale.removeModifier(SCALE_MODIFIER_ID);
            scale.addTransientModifier(new AttributeModifier(
                SCALE_MODIFIER_ID,
                SCALE_MULTIPLIER,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        } else if (!fairy && applied != null) {
            scale.removeModifier(SCALE_MODIFIER_ID);
        }
    }
}
