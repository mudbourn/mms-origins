package info.mudbourn.mmsorigins.client;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

// Decides whether a player whose origin resizes them should stay hidden until that scale has actually reached the client, so they never flash at full size on join.
public final class OriginScaleGate {

    private static final double DEFAULT_SCALE = 1.0;
    private static final double SETTLED_EPSILON = 1.0E-3;
    private static final Set<Identifier> SCALE_POWERS = Set.of(
        Identifier.fromNamespaceAndPath("mms_origins", "diminutive"),
        Identifier.fromNamespaceAndPath("originstweaks", "slender_body"),
        Identifier.fromNamespaceAndPath("originstweaks", "arthropod"));

    private OriginScaleGate() {
    }

    // Whether this player carries a resizing origin whose scale has not yet propagated, so drawing them now would show the wrong size.
    public static boolean awaitingScale(Player player) {
        if (!hasScalePower(player)) {
            return false;
        }
        double scale = player.getAttributeValue(Attributes.SCALE);
        return Math.abs(scale - DEFAULT_SCALE) < SETTLED_EPSILON;
    }

    // Whether the player holds a power known to change their scale.
    private static boolean hasScalePower(Player player) {
        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(player);
        if (component == null) {
            return false;
        }
        for (PowerType<?> type : component.getPowerTypes(true)) {
            if (SCALE_POWERS.contains(type.getIdentifier())) {
                return true;
            }
        }
        return false;
    }
}
