package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Refuses an armed melee attack from a player in phantom form who lacks the soul energy to
 * will their weapon, so the hit lands no damage, knockback or on-hit effect. Bare hands pass.
 */
@Mixin(Player.class)
public abstract class PhantomArmedAttackMixin {

    @Unique
    private static final int STRIKE_COST = 3;

    @Unique
    private static final String ENERGY_PATH = "soulbound_energy";

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$refuseUnwilledStrike(Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (player.getMainHandItem().isEmpty() || !MmsOriginsPowers.phantomFormComplete(player)) {
            return;
        }
        for (VariableIntPower power : PowerHolderComponent.getPowers(player, VariableIntPower.class)) {
            if (power.getType().getIdentifier().getPath().equals(ENERGY_PATH)
                    && power.getValue() < STRIKE_COST) {
                ci.cancel();
                return;
            }
        }
    }
}
