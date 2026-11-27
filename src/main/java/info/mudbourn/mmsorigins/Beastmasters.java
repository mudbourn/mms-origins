package info.mudbourn.mmsorigins;

import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.player.JobsServerPlayer;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/**
 * Who counts as a Beastmaster for Useless Reptile's dragons. The class ({@code mms_origins:beastmaster}) makes wild
 * dragons leave the player be and lets them tame one; the Jobs+ Beastmaster job alone lets them tame one, but wild
 * dragons still hunt them.
 */
public final class Beastmasters {

    public static final Identifier CLASS = Identifier.fromNamespaceAndPath("mms_origins", "beastmaster");

    private Beastmasters() {
    }

    /** Has the Beastmaster class. */
    public static boolean hasClass(Player player) {
        OriginComponent origins = ModComponents.ORIGIN.maybeGet(player).orElse(null);
        if (origins == null) {
            return false;
        }
        for (Origin origin : origins.getOrigins().values()) {
            if (origin != null && CLASS.equals(origin.getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    /** Has the Beastmaster job. Only known on the server; false on the client. */
    public static boolean hasJob(Player player) {
        if (!(player instanceof JobsServerPlayer jobs)) {
            return false;
        }
        Identifier jobId = OriginsClasses.jobIdFor(CLASS);
        JobInstance job = jobId == null ? null : JobInstance.of(jobId);
        return job != null && jobs.jobsplus$getJob(job) != null;
    }

    /** May tame a dragon: the class or the job. */
    public static boolean mayTame(Player player) {
        return hasClass(player) || hasJob(player);
    }
}
