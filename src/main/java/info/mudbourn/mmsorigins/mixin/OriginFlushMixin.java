package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.OriginFlush;
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
 * Hard-flushes stale origin state after every origin assignment.
 */
@Mixin(PlayerOriginComponent.class)
public abstract class OriginFlushMixin {

    @Shadow
    private Player player;

    @Inject(method = "setOrigin", at = @At("RETURN"))
    private void mmsOrigins$flush(OriginLayer layer, Origin origin, CallbackInfo ci) {
        if (this.player instanceof ServerPlayer serverPlayer) {
            OriginFlush.flush(serverPlayer);
        }
    }
}
