package info.mudbourn.mmsorigins.mixin.uselessreptile;

import info.mudbourn.mmsorigins.Beastmasters;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// A wild Lightning Chaser is tamed by killing it. When a player who may tame dragons lands the killing blow, the
// dragon does not die: it comes back at full health, tamed to that player. Killed by anyone else, it dies as normal.
// Feeding is blocked in LightningChaserFeedMixin.
@Mixin(LivingEntity.class)
public abstract class LightningChaserTamingMixin {

    private static final String LIGHTNING_CHASER = "nordmods.uselessreptile.common.entity.LightningChaser";

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$reviveTamed(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide() || !self.getClass().getName().equals(LIGHTNING_CHASER)
                || !(self instanceof TamableAnimal dragon) || dragon.isTame()
                || !(source.getEntity() instanceof Player player)
                || !(player.isCreative() || Beastmasters.mayTame(player))) {
            return;
        }
        ci.cancel();
        dragon.setHealth(dragon.getMaxHealth());
        dragon.removeAllEffects();
        dragon.clearFire();
        dragon.setTarget(null);
        dragon.tame(player);
        dragon.setPersistenceRequired();
        try {
            dragon.getClass().getMethod("setSurrendered", boolean.class).invoke(dragon, false);
        } catch (ReflectiveOperationException ignored) {
        }
        dragon.level().broadcastEntityEvent(dragon, (byte) 7);
        player.sendOverlayMessage(Component.literal("The Lightning Chaser yields to you."));
    }
}
