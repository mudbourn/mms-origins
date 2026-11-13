package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.client.UnusableReason;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Puts the blocking power's name into Apoli's "Unusable" tooltip line. Runs at a late
 * priority so Apoli has already added the line.
 */
@Mixin(value = ItemStack.class, priority = 2000)
public abstract class UnusableReasonTooltipMixin {

    @Inject(method = "getTooltipLines", at = @At("RETURN"))
    private void mmsOrigins$nameUnusableReason(Item.TooltipContext context, Player player, TooltipFlag flag,
                                               CallbackInfoReturnable<List<Component>> cir) {
        UnusableReason.name((ItemStack) (Object) this, player, cir.getReturnValue());
    }
}
