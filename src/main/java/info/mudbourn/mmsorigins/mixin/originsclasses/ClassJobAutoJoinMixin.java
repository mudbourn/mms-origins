package info.mudbourn.mmsorigins.mixin.originsclasses;

import info.mudbourn.mmsorigins.ClassJobs;
import io.github.apace100.origins.component.PlayerOriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Picking an Origins class joins you to the matching Jobs+ job.
 */
@Mixin(PlayerOriginComponent.class)
public abstract class ClassJobAutoJoinMixin {

    @Shadow
    private Player player;

    @Inject(method = "setOrigin", at = @At("RETURN"))
    private void mmsOrigins$joinPairedJob(OriginLayer layer, Origin origin, CallbackInfo ci) {
        if (this.player instanceof ServerPlayer serverPlayer) {
            ClassJobs.grant(serverPlayer, origin);
        }
    }
}
