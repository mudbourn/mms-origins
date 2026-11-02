package info.mudbourn.mmsorigins.mixin.originsclasses;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.originsclasses.power.TreeFellingPower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Tree felling treats a leaves-tagged block without a persistent property as
 * persistent, so modded leaves like Fish of Thieves coconut fronds no longer crash it.
 */
@Mixin(TreeFellingPower.class)
public abstract class TreeFellingLeafGuardMixin {

    @WrapOperation(
        method = "lambda$new$3",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"
        )
    )
    private static Comparable<?> mmsOrigins$skipLeavesWithoutPersistent(
        BlockState state,
        Property<?> property,
        Operation<Comparable<?>> original
    ) {
        if (!state.hasProperty(property)) {
            return Boolean.TRUE;
        }
        return original.call(state, property);
    }
}
