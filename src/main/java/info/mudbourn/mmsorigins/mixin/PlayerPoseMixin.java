package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.power.PosePower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies {@link PosePower} after the player has chosen its own pose for the
 * tick, replacing the newer Apoli {@code pose} power type that Apoli-Legacy
 * omits. Injecting here, rather than at the end of {@code LivingEntity.tick},
 * keeps {@code updatePlayerPose} from overwriting the forced pose.
 */
@Mixin(Player.class)
public class PlayerPoseMixin {

    @Inject(method = "updatePlayerPose", at = @At("TAIL"))
    private void mmsOrigins$forcePose(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        for (PosePower power : PowerHolderComponent.getPowers(self, PosePower.class)) {
            if (power.isActive()) {
                self.setPose(power.getPose());
            }
        }
    }
}
