package info.mudbourn.mmsorigins.compat;

import com.github.thedeathlycow.frostiful.item.attribute.FrostResistanceComponent;
import com.github.thedeathlycow.frostiful.registry.FDataComponentTypes;
import java.util.List;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class HeavyNetheriteFrostResistance {
    private static final List<String> PIECES = List.of(
        "heavy_netherite_helmet",
        "heavy_netherite_chestplate",
        "heavy_netherite_leggings",
        "heavy_netherite_boots"
    );

    private HeavyNetheriteFrostResistance() {
    }

    // Gives Expanded Weaponry heavy netherite the frost resistance Frostiful gives vanilla netherite.
    public static void register() {
        DefaultItemComponentEvents.MODIFY.register(context -> {
            for (String piece : PIECES) {
                BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath("expanded_weaponry", piece))
                    .ifPresent(item -> context.modify(
                        item,
                        builder -> builder.set(FDataComponentTypes.FROST_RESISTANCE, FrostResistanceComponent.PROTECTIVE)
                    ));
            }
        });
    }
}
