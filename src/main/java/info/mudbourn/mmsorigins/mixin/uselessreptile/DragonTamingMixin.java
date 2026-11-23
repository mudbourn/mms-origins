package info.mudbourn.mmsorigins.mixin.uselessreptile;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

// Only a Beastmaster, by class or by job, can tame a Useless Reptile dragon. Anyone else offering it a taming item is
// turned away and the item is kept; creative players are let through. Dragons already tamed are unaffected. The job is
// only known on the server, so the check runs there; the client lets the interaction through to ask it.
@Pseudo
@Mixin(targets = "nordmods.uselessreptile.common.entity.base.URDragonEntity", remap = false)
public abstract class DragonTamingMixin {

    private static Method tamingItem;

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$beastmastersOnly(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        TamableAnimal dragon = (TamableAnimal) (Object) this;
        if (player.level().isClientSide() || dragon.isTame() || player.isCreative()
                || info.mudbourn.mmsorigins.Beastmasters.mayTame(player) || !isTamingItem(dragon, player.getItemInHand(hand))) {
            return;
        }
        player.sendOverlayMessage(Component.literal("Only a Beastmaster can tame dragons."));
        cir.setReturnValue(InteractionResult.FAIL);
    }

    // URDragonEntity.getTamingItem(stack) is null for anything that isn't one of this dragon's taming items.
    private static boolean isTamingItem(TamableAnimal dragon, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        try {
            if (tamingItem == null) {
                tamingItem = dragon.getClass().getMethod("getTamingItem", ItemStack.class);
            }
            return tamingItem.invoke(dragon, stack) != null;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}
