package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Beastly: villagers will not deal with a piglin at all.
 *
 * <p>Where Language Barrier only marks a fishman's goods up, a piglin is turned
 * away outright. This cancels the villager interaction before the trade screen
 * can open, so no offer is touched and other players trade the same villager
 * normally. A carved pumpkin worn on the head hides the piglin's face and lets
 * the trade proceed.
 */
@Mixin(Villager.class)
public class VillagerBeastlyRefusalMixin {

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$refuseTheBeast(Player player, InteractionHand hand,
                                           CallbackInfoReturnable<InteractionResult> cir) {
        if (!MmsOriginsPowers.BEASTLY.isActive(player)) {
            return;
        }
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(Items.CARVED_PUMPKIN)) {
            return;
        }
        cir.setReturnValue(InteractionResult.PASS);
    }
}
