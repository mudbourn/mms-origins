package info.mudbourn.mmsorigins.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PreventItemUsePower;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Refuses to finish using an item that a prevent_item_use power forbids, however the use began.
@Mixin(LivingEntity.class)
public abstract class PreventItemUseFinishMixin {

    @Shadow
    protected ItemStack useItem;

    @Shadow
    public abstract void stopUsingItem();

    @Inject(method = "completeUsingItem", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$refuseForbiddenUse(CallbackInfo ci) {
        if (!((Object) this instanceof Player player) || useItem.isEmpty()) {
            return;
        }
        for (PreventItemUsePower power : PowerHolderComponent.getPowers(player, PreventItemUsePower.class)) {
            if (power.doesPrevent(useItem)) {
                stopUsingItem();
                ci.cancel();
                return;
            }
        }
    }
}
