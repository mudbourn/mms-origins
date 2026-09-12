package info.mudbourn.mmsorigins.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets a blazeborn move through lava the way a fishman moves through water.
 * Vanilla splits fluid travel into {@code travelInWater} and {@code travelInLava}
 * with the same shape, and lava's variant is sluggish and sinking. When the
 * bearer holds the blazeborn fire power, this routes their lava travel through
 * the water path instead, so they swim and steer in lava with a fish's ease.
 */
@Mixin(LivingEntity.class)
public abstract class LavaSwimMixin {

    private static final Identifier FIRE_POWER =
        Identifier.fromNamespaceAndPath("originstweaks", "fire_power");

    private static final double SINK_PER_TICK = 0.05;
    private static final double RISE_PER_TICK = 0.05;

    @Shadow
    protected boolean jumping;

    @Shadow
    protected abstract void travelInWater(Vec3 movementInput, double gravity, boolean falling, double speed);

    @Inject(method = "travelInLava", at = @At("HEAD"), cancellable = true)
    private void mmsOrigins$swimLava(
            Vec3 movementInput,
            double gravity,
            boolean falling,
            double speed,
            CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player)) {
            return;
        }
        for (VariableIntPower power : PowerHolderComponent.getPowers(self, VariableIntPower.class)) {
            if (power.getType().getIdentifier().equals(FIRE_POWER)) {
                travelInWater(movementInput, gravity, falling, speed);
                mmsOrigins$holdDepth(self);
                ci.cancel();
                return;
            }
        }
    }

    private void mmsOrigins$holdDepth(LivingEntity self) {
        Vec3 motion = self.getDeltaMovement();
        double vertical = motion.y;
        if (self.isCrouching()) {
            // Sneaking dives, the way a fishman sinks in water.
            vertical -= SINK_PER_TICK;
        } else if (jumping) {
            // Holding jump swims upward and breaks the surface.
            vertical = RISE_PER_TICK;
        } else {
            // Idle holds depth, so the bearer neither sinks nor drifts to the surface.
            vertical = 0.0;
        }
        self.setDeltaMovement(motion.x, vertical, motion.z);
    }
}
