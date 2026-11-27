package info.mudbourn.mmsorigins;

import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Set;

/**
 * The class to Jobs+ job pairing, in one place.
 *
 * <p>Every class in the {@code mms_origins:class} layer is meant to line up with a Jobs+ job
 * of the same name. Three do not share an id, and one (nitwit) has no job at all. Both the
 * selection-screen synergy line and the auto-join on class pick read the mapping from here,
 * so the two can never disagree about which job a class means.
 *
 * <p>Not a mixin, and deliberately outside every mixin package. Mixin claims each class
 * under a config's {@code package} and refuses to load it normally.
 */
public final class OriginsClasses {

    /** Namespace of the class origins. */
    public static final String NAMESPACE = "mms_origins";

    /** Namespace Jobs+ registers its jobs under. */
    public static final String JOBS_NAMESPACE = "jobsplus";

    /** Every class origin in the class layer. */
    private static final Set<String> CLASSES = Set.of(
        "archer",
        "beastmaster",
        "blacksmith",
        "cleric",
        "cook",
        "explorer",
        "farmer",
        "lumberjack",
        "merchant",
        "miner",
        "nitwit",
        "rancher",
        "rogue",
        "warrior"
    );

    /** Classes whose Jobs+ job id differs from the class id. */
    private static final Map<String, String> JOB_ALIASES = Map.of(
        "archer", "hunter",
        "blacksmith", "smith",
        "cleric", "alchemist"
    );

    private OriginsClasses() {
    }

    /**
     * @param originId any origin id
     * @return the Jobs+ job id this class pairs with, or {@code null} if the
     *         origin is not a class. A non-null result is not a promise that the
     *         job exists. Nitwit maps to {@code jobsplus:nitwit}, which does
     *         not, so callers must still check with the job registry.
     */
    public static Identifier jobIdFor(Identifier originId) {
        if (originId == null
            || !NAMESPACE.equals(originId.getNamespace())
            || !CLASSES.contains(originId.getPath())) {
            return null;
        }
        String path = originId.getPath();
        return Identifier.tryBuild(JOBS_NAMESPACE, JOB_ALIASES.getOrDefault(path, path));
    }
}
