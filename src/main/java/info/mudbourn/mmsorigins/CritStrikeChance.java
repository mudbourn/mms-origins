package info.mudbourn.mmsorigins;

import info.mudbourn.mmsorigins.power.CritChancePower;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

// Feeds the crit chance power into Critical Strike's chance attribute.
public final class CritStrikeChance {

    private static final Identifier CHANCE = Identifier.fromNamespaceAndPath("critical_strike", "chance");
    private static final Identifier MODIFIER = Identifier.fromNamespaceAndPath("mms_origins", "crit_chance");

    private CritStrikeChance() {
    }

    public static void register() {
        ServerTickEvents.END_LEVEL_TICK.register(CritStrikeChance::onEndLevelTick);
    }

    private static void onEndLevelTick(ServerLevel level) {
        BuiltInRegistries.ATTRIBUTE.get(CHANCE).ifPresent(attribute -> {
            for (ServerPlayer player : level.players()) {
                AttributeInstance instance = player.getAttribute(attribute);
                if (instance == null) {
                    continue;
                }
                double bonus = CritChancePower.totalChance(player) * 100.0;
                AttributeModifier current = instance.getModifier(MODIFIER);
                if (current != null && current.amount() == bonus) {
                    continue;
                }
                instance.removeModifier(MODIFIER);
                if (bonus > 0.0) {
                    instance.addTransientModifier(new AttributeModifier(MODIFIER, bonus, AttributeModifier.Operation.ADD_VALUE));
                }
            }
        });
    }
}
