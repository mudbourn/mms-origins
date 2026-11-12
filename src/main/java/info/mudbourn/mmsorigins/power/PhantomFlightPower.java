package info.mudbourn.mmsorigins.power;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;

/**
 * Lets a phantom in phantom form fly with no timer and no cost. Flight is granted while
 * the power's condition holds. The server only grants and revokes {@code mayfly};
 * vanilla's double-tap-jump toggles {@code flying}, the holder's client sets the flying
 * speed from {@link SurgingFlight}, and the winged movement itself is the holder's client
 * physics. Vanilla's sprint doubles the speed, and sprinting aloft for a moment eases it
 * up until the sprint pace matches an avian's surge.
 */
public final class PhantomFlightPower extends Power implements SurgingFlight {

    private static final float FLYING_SPEED = 0.04F;
    private static final float NORMAL_FLYING_SPEED = 0.05F;
    private static final float TOP_FLYING_SPEED = 0.0878F;

    public PhantomFlightPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
        this.setTicking(true);
    }

    @Override
    public void tick() {
        if (!(entity instanceof Player player)) {
            return;
        }
        if (player.isCreative() || player.isSpectator() || player.level().isClientSide()) {
            return;
        }
        Abilities abilities = player.getAbilities();
        if (isActive()) {
            abilities.setFlyingSpeed(FLYING_SPEED);
            if (!abilities.mayfly) {
                abilities.mayfly = true;
                player.onUpdateAbilities();
            }
        } else {
            revoke(player);
        }
    }

    @Override
    public void onLost() {
        if (entity instanceof Player player && !player.isCreative() && !player.isSpectator()) {
            revoke(player);
        }
    }

    @Override
    public float cruiseSpeed(Player player) {
        return FLYING_SPEED;
    }

    @Override
    public float surgeSpeed(Player player, float build) {
        return FLYING_SPEED + (TOP_FLYING_SPEED - FLYING_SPEED) * build;
    }

    @Override
    public boolean surging(Player player) {
        return player.isSprinting();
    }

    private static void revoke(Player player) {
        Abilities abilities = player.getAbilities();
        if (abilities.mayfly || abilities.flying || abilities.getFlyingSpeed() != NORMAL_FLYING_SPEED) {
            abilities.mayfly = false;
            abilities.flying = false;
            abilities.setFlyingSpeed(NORMAL_FLYING_SPEED);
            player.onUpdateAbilities();
        }
    }
}
