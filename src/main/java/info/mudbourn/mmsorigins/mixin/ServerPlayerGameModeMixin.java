package info.mudbourn.mmsorigins.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import info.mudbourn.mmsorigins.power.EnchantmentMiningContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Publishes the breaking player to {@link EnchantmentMiningContext} for the span
 * of a block break, so a {@code ModifyEnchantmentLevelPower} can raise the tool's
 * silk touch or fortune level during loot generation.
 */
@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {

    @Shadow
    protected ServerPlayer player;

    @WrapMethod(method = "destroyBlock")
    private boolean mmsOrigins$scopeMiner(BlockPos pos, Operation<Boolean> original) {
        EnchantmentMiningContext.set(player);
        try {
            return original.call(pos);
        } finally {
            EnchantmentMiningContext.clear();
        }
    }
}
