package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets a leaf-form floran's speed bonuses carry through the air and while phasing through blocks.
 */
@Mixin(Player.class)
public abstract class LeafFormAirSpeedMixin {

    @Unique
    private static final float AIR_SPEED_PER_SPEED = 0.2F;

    @Inject(method = "getFlyingSpeed", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$leafAirSpeed(CallbackInfoReturnable<Float> cir) {
        Player self = (Player) (Object) this;
        if (!self.getAbilities().flying && MmsOriginsPowers.leafFormComplete(self)) {
            cir.setReturnValue(self.getSpeed() * AIR_SPEED_PER_SPEED);
        }
    }
}
