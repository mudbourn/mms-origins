package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Beastly: villagers will not deal with a piglin.
 *
 * <p>Where Language Barrier only marks a fishman's goods up, a piglin is turned
 * away outright. This cancels the villager interaction before the trade screen
 * can open, so no offer is touched and other players trade the same villager
 * normally. Rather than passing silently, the villager plays the same refusal it
 * gives an idle, jobless one: it shakes its head and grunts, so the piglin is
 * plainly being denied service rather than finding a merchant with nothing to
 * sell. A carved pumpkin worn on the head hides the piglin's face and lets the
 * trade proceed.
 */
@Mixin(Villager.class)
public class VillagerBeastlyRefusalMixin {

    // Vanilla no-trade refusal: head-shake counter plus the villager "no" grunt.
    @Shadow
    private void setUnhappy() {
        throw new AssertionError();
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$refuseTheBeast(Player player, InteractionHand hand,
                                           CallbackInfoReturnable<InteractionResult> cir) {
        if (!MmsOriginsPowers.BEASTLY.isActive(player)) {
            return;
        }
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(Items.CARVED_PUMPKIN)) {
            return;
        }
        if (!((Villager) (Object) this).level().isClientSide()) {
            this.setUnhappy();
        }
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
