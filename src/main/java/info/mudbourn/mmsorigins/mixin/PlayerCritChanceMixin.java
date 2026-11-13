package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.power.CritChancePower;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Rolls the {@code mms_origins:crit_chance} power on every fully charged hit. On a
 * hit that would not crit on its own, a successful roll takes the vanilla crit
 * path with its damage, particles and sound. On a hit that already crits, a
 * successful roll stacks a second crit multiplier and plays the arrow hit ding.
 */
@Mixin(Player.class)
public abstract class PlayerCritChanceMixin {

    @Unique
    private boolean mmsOrigins$stackedCrit;

    @Inject(method = "canCriticalAttack", at = @At("RETURN"), cancellable = true)
    private void mmsOrigins$rollCritChance(Entity target, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        mmsOrigins$stackedCrit = false;
        if (player.level().isClientSide()) {
            return;
        }
        float chance = CritChancePower.totalChance(player);
        if (chance <= 0.0F || player.getRandom().nextFloat() >= chance) {
            return;
        }
        if (!cir.getReturnValueZ()) {
            cir.setReturnValue(true);
            return;
        }
        mmsOrigins$stackedCrit = true;
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundGameEventPacket(
                    ClientboundGameEventPacket.PLAY_ARROW_HIT_SOUND,
                    0.0F
            ));
        }
    }

    @ModifyConstant(method = "attack", constant = @Constant(floatValue = 1.5F))
    private float mmsOrigins$stackCritMultiplier(float multiplier) {
        if (!mmsOrigins$stackedCrit) {
            return multiplier;
        }
        mmsOrigins$stackedCrit = false;
        return multiplier * multiplier;
    }
}
