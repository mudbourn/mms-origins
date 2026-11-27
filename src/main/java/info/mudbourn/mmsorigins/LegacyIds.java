package info.mudbourn.mmsorigins;

import net.minecraft.resources.Identifier;

import java.util.Set;

/**
 * Maps the ids that saved players still carry onto their {@code mms_origins} replacements.
 *
 * <p>Every origin, layer and power used to live in {@code originstweaks}, {@code origins} or
 * {@code origins-classes}. Player data names those ids, so the readers in the migration mixins
 * pass each saved id through here and a player keeps their origin and power state.
 *
 * <p>Not a mixin, and deliberately outside every mixin package. Mixin claims each class
 * under a config's {@code package} and refuses to load it normally.
 */
public final class LegacyIds {

    private static final String CURRENT = "mms_origins";

    private static final String TWEAKS = "originstweaks";

    private static final String CLASSES = "origins-classes";

    private static final String ORIGINS = "origins";

    /** Base Origins origins that became {@code mms_origins} origins. */
    private static final Set<String> ORIGINS_ORIGINS = Set.of("human", "fishman");

    /** Base Origins powers that became {@code mms_origins} powers. */
    private static final Set<String> ORIGINS_POWERS = Set.of(
        "air_from_potions",
        "burn_in_daylight",
        "carnivore",
        "conduit_power_on_land",
        "damage_from_potions",
        "damage_from_snowballs",
        "ender_particles",
        "flame_particles",
        "fresh_air",
        "hotblooded",
        "lay_eggs",
        "like_water",
        "like_air",
        "master_of_webs_no_slowdown",
        "more_exhaustion",
        "more_kinetic_damage",
        "nether_spawn",
        "no_shield",
        "pumpkin_hate",
        "shulker_inventory",
        "strong_arms",
        "strong_arms_break_speed",
        "tailwind",
        "vegetarian",
        "velvet_paws",
        "water_breathing"
    );

    private LegacyIds() {
    }

    /** The current id of a saved origin id, or the id itself when it is not a legacy one. */
    public static Identifier origin(Identifier id) {
        if (id == null) {
            return null;
        }
        String namespace = id.getNamespace();
        boolean legacy = namespace.equals(TWEAKS)
            || namespace.equals(CLASSES)
            || namespace.equals(ORIGINS) && ORIGINS_ORIGINS.contains(id.getPath());
        return legacy ? Identifier.fromNamespaceAndPath(CURRENT, id.getPath()) : id;
    }

    /** The current id of a saved layer id, or the id itself when it is not a legacy one. */
    public static Identifier layer(Identifier id) {
        if (id == null) {
            return null;
        }
        String namespace = id.getNamespace();
        return namespace.equals(TWEAKS) || namespace.equals(CLASSES)
            ? Identifier.fromNamespaceAndPath(CURRENT, id.getPath())
            : id;
    }

    /** The current id of a saved power id, or the id itself when it is not a legacy one. */
    public static Identifier power(Identifier id) {
        if (id == null) {
            return null;
        }
        String namespace = id.getNamespace();
        boolean legacy = namespace.equals(TWEAKS)
            || namespace.equals(CLASSES)
            || namespace.equals(ORIGINS) && ORIGINS_POWERS.contains(id.getPath());
        return legacy ? Identifier.fromNamespaceAndPath(CURRENT, id.getPath()) : id;
    }
}
