package info.mudbourn.mmsorigins.mixin;

import io.github.apace100.origins.entity.EnderianPearlEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Enderian teleport lands the thrower on a surface rather than buried in a block.
 *
 * <p>The pearl teleports its owner to the pearl's own resting position, which can
 * sit flush against or inside the block it struck, leaving the thrower a block
 * deep in the ground. After the vanilla teleport runs, this lifts the owner
 * straight up until they no longer overlap solid blocks, so the landing is
 * always clear instead of relying on the power's suffocation immunity.
 */
@Mixin(EnderianPearlEntity.class)
public abstract class EnderianPearlLandingMixin {

    /** Most blocks a teleport can bury the thrower before we give up lifting. */
    private static final int MMS_MAX_LIFT = 3;

    @Inject(method = "onHit", at = @At("RETURN"))
    private void mmsOrigins$liftClearOfBlocks(HitResult hitResult, CallbackInfo ci) {
        EnderianPearlEntity self = (EnderianPearlEntity) (Object) this;
        Level level = self.level();
        if (level.isClientSide()) {
            return;
        }
        Entity owner = self.getOwner();
        if (owner == null) {
            return;
        }
        for (int lift = 0; lift < MMS_MAX_LIFT; lift++) {
            AABB box = owner.getBoundingBox();
            if (level.noCollision(owner, box)) {
                break;
            }
            owner.setPos(owner.getX(), owner.getY() + 1.0, owner.getZ());
        }
    }
}
