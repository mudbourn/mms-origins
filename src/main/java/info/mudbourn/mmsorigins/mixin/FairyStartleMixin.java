package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.CooldownPower;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Startles a fairy from the air when a single blow costs it eight or more
 * health. Apoli's {@code action_when_hit} reads the raw incoming damage, before
 * armour and before Fragile Frame amplifies it, so the amount a player actually
 * feels is measured here instead: the fairy's health and absorption are noted
 * before the blow and again after it, and their loss triggers the Anxious Heart
 * cooldown that {@code fairy_flight} is gated on.
 */
@Mixin(LivingEntity.class)
public class FairyStartleMixin {

    @Unique
    private float mmsOrigins$reserveBeforeHit;

    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void mmsOrigins$noteReserve(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        mmsOrigins$reserveBeforeHit = self.getHealth() + self.getAbsorptionAmount();
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void mmsOrigins$startleOnHeavyBlow(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || !(((Object) this) instanceof Player player)) {
            return;
        }
        float taken = mmsOrigins$reserveBeforeHit - (player.getHealth() + player.getAbsorptionAmount());
        if (taken < MmsOriginsPowers.STARTLE_DAMAGE_THRESHOLD) {
            return;
        }
        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(player);
        if (component == null) {
            return;
        }
        CooldownPower anxiousHeart = component.getPower(MmsOriginsPowers.ANXIOUS_HEART_TIMER);
        if (anxiousHeart != null) {
            anxiousHeart.use();
        }
    }
}
