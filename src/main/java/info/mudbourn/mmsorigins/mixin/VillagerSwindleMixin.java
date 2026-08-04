package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Language Barrier: villagers cannot understand a fishman and mark their goods up.
 *
 * <p>{@code updateSpecialPrices} is where vanilla already applies its reputation
 * discount and the Hero of the Village discount, both by adding to each offer's
 * special price diff — a positive diff makes a trade dearer. Injecting after all
 * of that means the markup composes with reputation instead of fighting it: a
 * fishman who has worked hard for a village still ends up better off than one
 * who has not, just never as well off as anyone else.
 */
@Mixin(Villager.class)
public abstract class VillagerSwindleMixin {

    /** Fraction of the base cost added on top, rounded up, minimum one item. */
    private static final float MMS_SWINDLE_RATE = 0.25F;

    @Shadow
    public abstract MerchantOffers getOffers();

    @Inject(method = "updateSpecialPrices", at = @At("RETURN"))
    private void mmsOrigins$swindleTheUnintelligible(Player player, CallbackInfo ci) {
        if (!MmsOriginsPowers.LANGUAGE_BARRIER.isActive(player)) {
            return;
        }
        for (MerchantOffer offer : this.getOffers()) {
            int base = offer.getBaseCostA().getCount();
            offer.addToSpecialPriceDiff(Math.max(1, (int) Math.ceil(base * MMS_SWINDLE_RATE)));
        }
    }
}
