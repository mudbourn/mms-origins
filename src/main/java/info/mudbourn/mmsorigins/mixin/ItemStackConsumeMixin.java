package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.power.EdibleItemPower;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets an {@link EdibleItemPower} make its target item eatable by routing use,
 * use duration and completion through the power's {@code Consumable}. The food is
 * applied here since the stack carries no food component of its own.
 */
@Mixin(ItemStack.class)
public class ItemStackConsumeMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$startEating(
            Level level,
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack self = (ItemStack) (Object) this;
        EdibleItemPower power = EdibleItemPower.find(player, self);
        // The stack has no food component of its own, so vanilla's hunger gate cannot
        // see it; refuse to start eating on a full belly rather than waste the item.
        if (power != null && player.canEat(false)) {
            cir.setReturnValue(power.getConsumable().startConsuming(player, self, hand));
        }
    }

    @Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$eatAnimation(CallbackInfoReturnable<ItemUseAnimation> cir) {
        ItemUseAnimation animation = EdibleItemPower.animationFor((ItemStack) (Object) this);
        if (animation != null) {
            cir.setReturnValue(animation);
        }
    }

    @Inject(method = "onUseTick", at = @At("HEAD"))
    private void mmsOrigins$eatTick(Level level, LivingEntity entity, int remainingTicks, CallbackInfo ci) {
        ItemStack self = (ItemStack) (Object) this;
        EdibleItemPower power = EdibleItemPower.find(entity, self);
        // The stack has no consumable component, so vanilla's onUseTick skips the chewing
        // sounds and particles; drive them from the power's own Consumable on the same beat.
        if (power == null) {
            return;
        }
        Consumable consumable = power.getConsumable();
        int times = power.getConsumeSoundTimes();
        // A fixed count spaces the consume sound evenly across the bite instead of the vanilla cadence.
        if (times > 0) {
            int total = consumable.consumeTicks();
            int consumed = total - remainingTicks;
            for (int i = 1; i <= times; i++) {
                if (consumed == Math.round((float) total * i / (times + 1))) {
                    consumable.emitParticlesAndSounds(entity.getRandom(), entity, self, 5);
                    break;
                }
            }
            return;
        }
        if (!power.loopsConsumeEffects()) {
            return;
        }
        if (consumable.shouldEmitParticlesAndSounds(remainingTicks)) {
            consumable.emitParticlesAndSounds(entity.getRandom(), entity, self, 5);
        }
    }

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$eatDuration(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        EdibleItemPower power = EdibleItemPower.find(entity, self);
        if (power != null) {
            cir.setReturnValue(power.getConsumable().consumeTicks());
        }
    }

    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$finishEating(
            Level level,
            LivingEntity entity,
            CallbackInfoReturnable<ItemStack> cir) {
        ItemStack self = (ItemStack) (Object) this;
        EdibleItemPower power = EdibleItemPower.find(entity, self);
        if (power == null) {
            return;
        }
        if (!level.isClientSide() && entity instanceof Player player) {
            player.getFoodData().eat(power.getFood().nutrition(), power.getFood().saturation());
        }
        // Blazeborn stoke themselves on the fuel; the flame is thematic since they shrug off burning.
        if (power.getFireSeconds() > 0.0f) {
            entity.igniteForSeconds(power.getFireSeconds());
        }
        // Our stack has no food component, so the vanilla finish burp never fires; play it here.
        SoundEvent finishSound = power.getFinishSound();
        if (finishSound != null && !level.isClientSide()) {
            level.playSound(
                null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                finishSound,
                entity.getSoundSource(),
                0.5f,
                level.getRandom().nextFloat() * 0.1f + 0.9f);
        }
        cir.setReturnValue(power.getConsumable().onConsume(level, entity, self));
    }
}
