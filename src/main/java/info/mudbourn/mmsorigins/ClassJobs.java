package info.mudbourn.mmsorigins;

import com.daqem.jobsplus.config.JobsPlusConfig;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.player.JobsServerPlayer;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.registry.ModComponents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Grants the Jobs+ job paired with a player's Origins class, free of charge,
 * both when the class is picked and on login for classes picked earlier.
 */
public final class ClassJobs {

    private ClassJobs() {}

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            ModComponents.ORIGIN.get(player).getOrigins().values().forEach(origin -> grant(player, origin));
        });
    }

    /** Joins the job paired with {@code origin}, if it is a class and the job is not already held. */
    public static void grant(ServerPlayer player, Origin origin) {
        if (origin == null || !(player instanceof JobsServerPlayer jobsPlayer)) return;

        Identifier jobId = OriginsClasses.jobIdFor(origin.getIdentifier());
        if (jobId == null) return;

        JobInstance instance = JobInstance.of(jobId);
        if (instance == null || jobsPlayer.jobsplus$getJob(instance) != null) return;

        if (jobsPlayer.jobsplus$getJobs().size() >= JobsPlusConfig.maxJobs.get()) {
            tell(player, Component.literal("You are already working as many jobs as you can, so ")
                .append(instance.getName())
                .append(Component.literal(" was not joined.")));
            return;
        }

        jobsPlayer.jobsplus$addNewJob(instance);
        tell(player, Component.literal("You took up the ")
            .append(instance.getName())
            .append(Component.literal(" job, free with your class.")));
    }

    private static void tell(ServerPlayer player, Component message) {
        player.sendSystemMessage(message.copy().withStyle(ChatFormatting.YELLOW));
    }
}
