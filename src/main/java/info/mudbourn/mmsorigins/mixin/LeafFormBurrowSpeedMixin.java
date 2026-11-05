package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Speeds a leaf-form floran's climb and sink while they are buried in natural blocks.
 */
@Mixin(LivingEntity.class)
public abstract class LeafFormBurrowSpeedMixin {

    @Unique
    private static final TagKey<Block> LEAF_CLIMBABLE =
        TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("mms_origins", "leaf_climbable"));

    @Unique
    private static final double BURROW_LIFT = 0.35;

    @Unique
    private static final double BURROW_SINK = -0.35;

    @Inject(method = "travel", at = @At("RETURN"))
    private void mmsOrigins$burrowSpeed(Vec3 input, CallbackInfo ci) {
        if (!((Object) this instanceof Player player) || !mmsOrigins$buried(player)) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        if (player.isShiftKeyDown()) {
            player.setDeltaMovement(motion.x, BURROW_SINK, motion.z);
        } else if (motion.y > 0.0 && (player.horizontalCollision || player.onClimbable())) {
            player.setDeltaMovement(motion.x, BURROW_LIFT, motion.z);
        }
    }

    /** Whether the player is fully in leaf form with their body inside a natural block. */
    @Unique
    private static boolean mmsOrigins$buried(Player player) {
        Power power = MmsOriginsPowers.LEAF_FORM_MORPH.get(player);
        if (!MmsOriginsPowers.LEAF_FORM.isActive(player)
                || !(power instanceof VariableIntPower morph)
                || morph.getValue() < MmsOriginsPowers.LEAF_FORM_MORPH_TICKS) {
            return false;
        }
        BlockPos feet = player.blockPosition();
        return player.level().getBlockState(feet).is(LEAF_CLIMBABLE)
                || player.level().getBlockState(feet.above()).is(LEAF_CLIMBABLE)
                || player.level().getBlockState(feet.below()).is(LEAF_CLIMBABLE) && player.isShiftKeyDown();
    }
}
