package info.mudbourn.mmsorigins.client.wings;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * The table of butterfly wing textures, keyed by the {@code fairy_wings} option origin.
 *
 * <p>The choice lives in the {@code mms_origins:fairy_wings} layer on the player and is
 * stamped onto the render state during extraction; the feature layer resolves it here.
 * Null means the player picked no butterfly wing, so nothing is drawn.
 */
public final class ButterflyWingModels {

    private static final Map<Identifier, Identifier> TEXTURES = new HashMap<>();

    static {
        register("monarch_butterfly");
        register("blue_butterfly");
        register("pixie_wings");
        register("slime_wings");
        register("cirno_wings");
    }

    private ButterflyWingModels() {
    }

    /** The wing texture for a fairy_wings option, or null if the option draws no butterfly. */
    public static Identifier resolve(Identifier option) {
        return option == null ? null : TEXTURES.get(option);
    }

    private static void register(String name) {
        TEXTURES.put(
                Identifier.fromNamespaceAndPath("mms_origins", name),
                Identifier.fromNamespaceAndPath("mms_origins", "textures/entity/wings/" + name + ".png"));
    }
}
