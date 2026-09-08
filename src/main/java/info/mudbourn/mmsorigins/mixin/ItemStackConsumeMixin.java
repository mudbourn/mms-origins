package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.power.EdibleItemPower;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
        cir.setReturnValue(power.getConsumable().onConsume(level, entity, self));
    }
}
