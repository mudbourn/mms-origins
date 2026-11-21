package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets a fishman start swimming in water one block deep. Vanilla only starts a
 * swim once the eyes are under water, which shallow water never reaches; for
 * the bearer of the fishman water power, water at the feet is enough.
 */
@Mixin(Entity.class)
public abstract class ShallowSwimMixin {

    @Inject(method = "updateSwimming", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$swimShallowWater(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player player)
            || player.isSwimming()
            || !player.isSprinting()
            || !player.isInWater()
            || player.isPassenger()
            || !player.level().getFluidState(player.blockPosition()).is(FluidTags.WATER)
            || !MmsOriginsPowers.holds(player, MmsOriginsPowers.WATER_POWER)) {
            return;
        }
        player.setSwimming(true);
        ci.cancel();
    }
}
