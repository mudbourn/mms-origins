package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fairy Charm: villagers are taken with a fairy and discount their goods.
 *
 * <p>The mirror of {@code VillagerSwindleMixin}. Injecting after
 * {@code updateSpecialPrices} has applied vanilla's reputation and Hero of the
 * Village discounts means the charm composes with them: a negative special price
 * diff makes each trade cheaper, and vanilla clamps the final price so a trade
 * never costs less than one item.
 */
@Mixin(Villager.class)
public class VillagerCharmMixin {

    /** Fraction of the base cost taken off, rounded up, minimum one item. */
    private static final float MMS_CHARM_RATE = 0.25F;

    @Inject(method = "updateSpecialPrices", at = @At("RETURN"))
    private void mmsOrigins$charmTheSmitten(Player player, CallbackInfo ci) {
        if (!MmsOriginsPowers.FAIRY_CHARM.isActive(player)) {
            return;
        }
        for (MerchantOffer offer : ((Merchant) (Object) this).getOffers()) {
            int base = offer.getBaseCostA().getCount();
            offer.addToSpecialPriceDiff(-Math.max(1, (int) Math.ceil(base * MMS_CHARM_RATE)));
        }
    }
}
