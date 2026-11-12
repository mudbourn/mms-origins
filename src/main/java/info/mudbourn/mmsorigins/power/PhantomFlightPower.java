package info.mudbourn.mmsorigins.power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;

/**
 * Lets a morphed phantom fly for as long as its soul energy lasts. Flight is
 * granted while the power's condition holds and the soul bar is not empty, and
 * staying aloft spends one point of it each second. Only {@code mayfly} and the
 * base flying speed are set; vanilla's double-tap-jump toggles {@code flying}
 * and vanilla's sprint doubles the speed in {@code Player.getFlyingSpeed}.
 * Sprinting aloft for a moment eases the base speed up until the sprint pace
 * matches an avian's surge.
 */
public final class PhantomFlightPower extends Power {

    private static final float FLYING_SPEED = 0.04F;
    private static final float NORMAL_FLYING_SPEED = 0.05F;
    private static final float TOP_FLYING_SPEED = 0.0878F;
    private static final int DRAIN_INTERVAL = 20;
    private static final int SPRINT_BOOST_DELAY_TICKS = 30;
    private static final int SPRINT_BOOST_RAMP_TICKS = 40;

    private final PowerType<?> resource;
    private int flightTicks;
    private int sprintFlyTicks;

    public PhantomFlightPower(PowerType<?> type, LivingEntity entity, PowerType<?> resource) {
        super(type, entity);
        this.resource = resource;
        this.setTicking(true);
    }

    @Override
    public void tick() {
        if (!(entity instanceof Player player)) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        Abilities abilities = player.getAbilities();
        VariableIntPower soul = soul();
        if (isActive() && soul != null && soul.getValue() > 0) {
            float wantedSpeed = sprintSpeed(player, abilities);
            if (!abilities.mayfly || abilities.getFlyingSpeed() != wantedSpeed) {
                abilities.mayfly = true;
                abilities.setFlyingSpeed(wantedSpeed);
                player.onUpdateAbilities();
            }
            if (abilities.flying && !player.level().isClientSide()) {
                flightTicks++;
                if (flightTicks % DRAIN_INTERVAL == 0) {
                    soul.setValue(Math.max(soul.getMin(), soul.getValue() - 1));
                    PowerHolderComponent.syncPower(player, soul.getType());
                }
            }
        } else {
            flightTicks = 0;
            sprintFlyTicks = 0;
            revoke(player);
        }
    }

    @Override
    public void onLost() {
        if (entity instanceof Player player && !player.isCreative() && !player.isSpectator()) {
            revoke(player);
        }
    }

    private float sprintSpeed(Player player, Abilities abilities) {
        if (!abilities.flying || !player.isSprinting()) {
            sprintFlyTicks = 0;
            return FLYING_SPEED;
        }
        sprintFlyTicks++;
        float progress = Math.clamp((sprintFlyTicks - SPRINT_BOOST_DELAY_TICKS) / (float) SPRINT_BOOST_RAMP_TICKS, 0.0F, 1.0F);
        float eased = progress * progress * (3.0F - 2.0F * progress);
        return FLYING_SPEED + (TOP_FLYING_SPEED - FLYING_SPEED) * eased;
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

    private VariableIntPower soul() {
        if (resource == null) {
            return null;
        }
        for (VariableIntPower power : PowerHolderComponent.getPowers(entity, VariableIntPower.class)) {
            if (power.getType().getIdentifier().equals(resource.getIdentifier())) {
                return power;
            }
        }
        return null;
    }
}
