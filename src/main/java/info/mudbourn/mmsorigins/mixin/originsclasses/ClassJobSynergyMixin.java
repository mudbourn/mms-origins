package info.mudbourn.mmsorigins.mixin.originsclasses;

import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobManager;
import com.daqem.jobsplus.player.JobsPlayer;
import com.daqem.jobsplus.player.job.Job;
import info.mudbourn.mmsorigins.OriginsClasses;
import io.github.apace100.origins.origin.Origin;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Appends a live Jobs+ status line to Origins Classes descriptions.
 *
 * <p>Every class in the {@code origins-classes:class} layer has a Jobs+ job of the
 * same name (see the lang override in {@code assets/origins-classes/lang}), but a
 * player choosing a class has no way to learn that.  The lang override states the
 * pairing statically; this mixin adds the part that has to be live — whether you
 * already hold the job and at what level.</p>
 *
 * <p>Hooking {@code getDescription()} rather than the screen's render method means
 * the existing text layout does the wrapping and scrolling, so this cannot break
 * the selection screen's geometry.  It also reaches every surface that shows a
 * description: choose-origin, view-origin, and badge tooltips.</p>
 *
 * <p>Client-only.  Degrades to the static lang line if it fails to apply.</p>
 */
@Mixin(Origin.class)
public class ClassJobSynergyMixin {

    @Inject(method = "getDescription", at = @At("RETURN"), cancellable = true)
    private void mmsOrigins$appendJobSynergy(CallbackInfoReturnable<MutableComponent> cir) {
        Identifier jobId = OriginsClasses.jobIdFor(((Origin) (Object) this).getIdentifier());
        if (jobId == null) return;

        MutableComponent status = mmsOrigins$statusLine(jobId);
        if (status == null) return;

        MutableComponent description = cir.getReturnValue();
        if (description == null) return;

        cir.setReturnValue(description.copy().append(Component.literal("\n")).append(status));
    }

    /**
     * @return the live status line, or {@code null} when there is nothing to add
     *         (unknown class, no local player, Jobs+ data not synced yet).
     */
    @Unique
    private static MutableComponent mmsOrigins$statusLine(Identifier jobId) {
        // The client only knows about jobs the server has synced.  If the id is
        // not a real job (e.g. nitwit) there is no synergy to report.
        Map<Identifier, JobInstance> known = JobManager.getInstance().getJobs();
        JobInstance instance = known.get(jobId);
        if (instance == null) return null;

        if (!(Minecraft.getInstance().player instanceof JobsPlayer jobsPlayer)) return null;

        Job held = jobsPlayer.jobsplus$getJob(jobId);
        if (held != null) {
            return Component.literal("§a✔ You have this job at level " + held.getLevel() + ".");
        }

        // Picking the class grants the job outright — never charged, whatever the
        // job's listed price says. See ClassJobAutoJoinMixin.
        return Component.literal("§7➜ Picking this class joins this job §8(free).");
    }
}
