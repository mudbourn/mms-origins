package info.mudbourn.mmsorigins.client.wings;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * The table of Icarus wing options, keyed by the {@code elytrian_options} origin they belong to.
 *
 * <p>Ported from LocusAzzurro's Icarus Wings, which renders every wing on the vanilla
 * elytra model. Feather-style wings have a single texture and never change shape. Synapse
 * wings are expandable: while the wearer is gliding they swap to a spread {@code reversed}
 * texture and scale up by their own factor, so the compact folded shard grows into a full
 * wing. The vanilla-elytra and no-render options are absent, since the base elytra layer
 * draws the first and nothing draws the second.
 */
public final class WingModels {

    /**
     * A resolved wing: the folded texture, the spread texture, and how it expands.
     *
     * <p>{@code reversed} is the gliding texture and equals {@code folded} for a wing that
     * does not change shape; {@code expandable} is true only for the synapse wings, whose
     * {@code expansionFactor} scales the model along the wingspan while gliding.
     */
    public record Wing(Identifier folded,
                       Identifier reversed,
                       boolean expandable,
                       float expansionFactor) {}

    private static final Map<Identifier, Wing> WINGS = new HashMap<>();

    static {
        feather("feathered_wings");
        feather("golden_wings");
        feather("colored_wings");
        feather("paper_wings");
        feather("magic_wings");
        feather("flandre_wings");
        synapse("ikaros_wings", 2.0F);
        synapse("nymph_wings", 1.2F);
        synapse("astraea_wings", 1.4F);
        synapse("chaos_wings", 1.8F);
        synapse("hiyori_wings", 2.0F);
        synapse("melan_wings", 2.0F);
    }

    private WingModels() {
    }

    /** The wing for an elytrian option, or null if the option draws no custom wing. */
    public static Wing resolve(Identifier option) {
        return option == null ? null : WINGS.get(option);
    }

    /** Registers a fixed-shape feather wing with a single texture. */
    private static void feather(String name) {
        Identifier texture = texture(base(name));
        WINGS.put(option(name), new Wing(texture, texture, false, 1.0F));
    }

    /** Registers an expandable synapse wing with a spread gliding texture. */
    private static void synapse(String name, float expansionFactor) {
        String base = base(name);
        WINGS.put(option(name),
                new Wing(texture(base), texture(base + "_reversed"), true, expansionFactor));
    }

    /** The texture base name for an option, dropping the shared {@code _wings} suffix. */
    private static String base(String name) {
        return name.substring(0, name.length() - "_wings".length());
    }

    private static Identifier option(String name) {
        return Identifier.fromNamespaceAndPath("originstweaks", name);
    }

    private static Identifier texture(String base) {
        return Identifier.fromNamespaceAndPath("originstweaks",
                "textures/entity/icaruswings/" + base + ".png");
    }
}
