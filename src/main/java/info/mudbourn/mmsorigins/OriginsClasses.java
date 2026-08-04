package info.mudbourn.mmsorigins;

import net.minecraft.resources.Identifier;

import java.util.Map;

/**
 * The Origins Classes ↔ Jobs+ pairing, in one place.
 *
 * <p>Every class in the {@code origins-classes:class} layer is meant to line up
 * with a Jobs+ job of the same name. Three do not share an id, and one (nitwit)
 * has no job at all. Both the selection-screen synergy line and the auto-join on
 * class pick read the mapping from here, so the two can never disagree about
 * which job a class means.
 *
 * <p>Not a mixin, and deliberately outside every mixin package — Mixin claims
 * each class under a config's {@code package} and refuses to load it normally.
 */
public final class OriginsClasses {

    /** Namespace of the origins that participate in the class/job pairing. */
    public static final String NAMESPACE = "origins-classes";

    /** Namespace Jobs+ registers its jobs under. */
    public static final String JOBS_NAMESPACE = "jobsplus";

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
     *         job exists — nitwit maps to {@code jobsplus:nitwit}, which does
     *         not — so callers must still check with the job registry.
     */
    public static Identifier jobIdFor(Identifier originId) {
        if (originId == null || !NAMESPACE.equals(originId.getNamespace())) {
            return null;
        }
        String path = originId.getPath();
        return Identifier.tryBuild(JOBS_NAMESPACE, JOB_ALIASES.getOrDefault(path, path));
    }
}
