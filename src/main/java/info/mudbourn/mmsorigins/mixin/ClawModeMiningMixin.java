package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.fur.FurResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stops a survival beastfolk from breaking blocks while claw mode is on, so a left
 * click is always a claw slash. Both the client and the server ask this before
 * breaking, which keeps the two in step.
 */
@Mixin(Player.class)
public abstract class ClawModeMiningMixin {

    @Inject(method = "blockActionRestricted", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$noMiningWithClaws(Level level, BlockPos pos, GameType gameType,
                                              CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (gameType.isSurvival() && FurResolver.clawsOut(player)) {
            cir.setReturnValue(true);
        }
    }
}
