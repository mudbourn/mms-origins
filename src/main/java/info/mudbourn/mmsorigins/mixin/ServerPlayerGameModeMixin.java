package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.power.EnchantmentMiningContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Publishes the breaking player to {@link EnchantmentMiningContext} for the span
 * of a block break, so a {@code ModifyEnchantmentLevelPower} can raise the tool's
 * silk touch or fortune level during loot generation.
 */
@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Shadow
    protected ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void mmsOrigins$captureMiner(net.minecraft.core.BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        EnchantmentMiningContext.set(player);
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void mmsOrigins$releaseMiner(net.minecraft.core.BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        EnchantmentMiningContext.clear();
    }
}
