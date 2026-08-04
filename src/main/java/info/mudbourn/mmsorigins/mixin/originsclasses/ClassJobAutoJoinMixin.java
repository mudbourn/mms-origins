package info.mudbourn.mmsorigins.mixin.originsclasses;

import com.daqem.jobsplus.config.JobsPlusConfig;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.player.JobsServerPlayer;
import info.mudbourn.mmsorigins.OriginsClasses;
import io.github.apace100.origins.component.PlayerOriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Picking an Origins class joins you to the matching Jobs+ job.
 *
 * <p>The pairing already existed but had to be completed by hand: choose the
 * class, then open the jobs menu and pick the same name again. Anyone who missed
 * the second half earned no job experience and had no way to tell, since nothing
 * about the class screen says a job is involved. This closes that gap — the class
 * pick is the whole action.
 *
 * <p>{@code setOrigin} is the single seam every assignment goes through — screen
 * choice, {@code /origin set}, random roll, orb. It is deliberately not called by
 * {@code readData}, so logging in does not re-trigger this.
 *
 * <p>The class job is always granted, never sold, but it does occupy a free job
 * slot like any other. With a stock {@code amount_of_free_jobs} of 2 that lays
 * out the ladder by itself: the class job free, a second job free on top of it,
 * and the third bought with coins earned levelling the first two. {@code maxJobs}
 * is still respected, and a refusal leaves the class selection intact and says
 * why — a class you have no room for a job in is still a class you may be.
 */
@Mixin(PlayerOriginComponent.class)
public abstract class ClassJobAutoJoinMixin {

    @Shadow
    private Player player;

    @Inject(method = "setOrigin", at = @At("RETURN"))
    private void mmsOrigins$joinPairedJob(OriginLayer layer, Origin origin, CallbackInfo ci) {
        if (origin == null) return;
        if (!(this.player instanceof ServerPlayer serverPlayer)) return;
        if (!(this.player instanceof JobsServerPlayer jobsPlayer)) return;

        Identifier jobId = OriginsClasses.jobIdFor(origin.getIdentifier());
        if (jobId == null) return;

        // Null for a class with no matching job, e.g. nitwit.
        JobInstance instance = JobInstance.of(jobId);
        if (instance == null) return;

        // Already held, including from an earlier pick of the same class.
        if (jobsPlayer.jobsplus$getJob(instance) != null) return;

        int held = jobsPlayer.jobsplus$getJobs().size();
        if (held >= JobsPlusConfig.maxJobs.get()) {
            mmsOrigins$tell(serverPlayer, Component.literal("You are already working as many jobs as you can, so ")
                .append(instance.getName())
                .append(Component.literal(" was not joined.")));
            return;
        }

        // Never charged. The class job is the first rung of the intended ladder —
        // class job free, a second job free on top of it, and only the third
        // bought with coins earned by levelling those two. Jobs+ gets there on
        // its own from a stock amount_of_free_jobs of 2, provided this one is
        // granted rather than sold: it still occupies a free slot, so it shifts
        // the paid job from the second to the third without touching the config.
        jobsPlayer.jobsplus$addNewJob(instance);
        mmsOrigins$tell(serverPlayer, Component.literal("You took up the ")
            .append(instance.getName())
            .append(Component.literal(" job, free with your class.")));
    }

    @Unique
    private static void mmsOrigins$tell(ServerPlayer player, Component message) {
        player.sendSystemMessage(message.copy().withStyle(ChatFormatting.YELLOW));
    }
}
