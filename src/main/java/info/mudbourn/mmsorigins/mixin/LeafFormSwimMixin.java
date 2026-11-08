package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Moves a leaf-form floran as a swimmer moves: a fixed pace toward the input with no
 * walking or sprinting, hovering while buried in natural blocks, rising on jump and
 * sinking on sneak.
 */
@Mixin(LivingEntity.class)
public abstract class LeafFormSwimMixin {

    @Unique
    private static final TagKey<Block> LEAF_CLIMBABLE =
        TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("mms_origins", "leaf_climbable"));

    @Unique
    private static final double SURFACE_SPEED = 0.24;

    @Unique
    private static final double BURIED_SPEED = 0.56;

    @Unique
    private static final double VERTICAL_SPEED = 0.3;

    @Unique
    private static final double ACCELERATION = 0.35;

    @Unique
    private static final double GRAVITY = 0.08;

    @Unique
    private static final double AIR_DRAG = 0.98;

    @Shadow
    protected boolean jumping;

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$leafSwim(Vec3 input, CallbackInfo ci) {
        if (!((Object) this instanceof Player player)
                || player.getAbilities().flying
                || !MmsOriginsPowers.leafFormComplete(player)) {
            return;
        }
        boolean buried = mmsOrigins$buried(player);
        double speed = buried ? BURIED_SPEED : SURFACE_SPEED;
        Vec3 wish = mmsOrigins$wish(input, player.getYRot()).scale(speed);
        Vec3 motion = player.getDeltaMovement();
        double x = Mth.lerp(ACCELERATION, motion.x, wish.x);
        double z = Mth.lerp(ACCELERATION, motion.z, wish.z);
        double y;
        if (player.isShiftKeyDown() && (buried || mmsOrigins$onNatural(player))) {
            y = -VERTICAL_SPEED;
        } else if (buried && this.jumping) {
            y = VERTICAL_SPEED;
        } else if (buried) {
            y = 0.0;
        } else {
            y = (motion.y - GRAVITY) * AIR_DRAG;
        }
        player.setDeltaMovement(x, y, z);
        player.move(MoverType.SELF, player.getDeltaMovement());
        player.resetFallDistance();
        ci.cancel();
    }

    /** The unit-length world direction of the movement input, turned by the player's yaw. */
    @Unique
    private static Vec3 mmsOrigins$wish(Vec3 input, float yaw) {
        Vec3 flat = new Vec3(input.x, 0.0, input.z);
        if (flat.lengthSqr() < 1.0E-7) {
            return Vec3.ZERO;
        }
        flat = flat.normalize();
        double sin = Mth.sin(yaw * Mth.DEG_TO_RAD);
        double cos = Mth.cos(yaw * Mth.DEG_TO_RAD);
        return new Vec3(flat.x * cos - flat.z * sin, 0.0, flat.z * cos + flat.x * sin);
    }

    /** Whether the player's body is inside a natural block. */
    @Unique
    private static boolean mmsOrigins$buried(Player player) {
        BlockPos feet = player.blockPosition();
        return player.level().getBlockState(feet).is(LEAF_CLIMBABLE)
                || player.level().getBlockState(feet.above()).is(LEAF_CLIMBABLE);
    }

    /** Whether the player stands on a natural block they can sink into. */
    @Unique
    private static boolean mmsOrigins$onNatural(Player player) {
        return player.level().getBlockState(player.blockPosition().below()).is(LEAF_CLIMBABLE);
    }
}
