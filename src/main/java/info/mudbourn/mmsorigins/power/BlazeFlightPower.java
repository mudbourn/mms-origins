package info.mudbourn.mmsorigins.power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;

/**
 * Lets a blazeborn take wing for as long as their fire power lasts. Flight is
 * granted while the shared fire power bar holds any charge, and staying aloft
 * spends it, so a blaze must keep stoking the bar in lava or flame to stay up.
 * The bar itself grows and cools from heat contact in the datapack; this power
 * only draws on it. When it empties the wings fail and the blaze drops. Only
 * {@code mayfly} and the flying speed are set; vanilla's double-tap-jump toggles
 * {@code flying}. Sprinting aloft for a moment eases the speed up until the
 * sprint pace matches an avian's surge.
 */
public final class BlazeFlightPower extends Power {

    private static final int FLIGHT_DRAIN_PER_TICK = 1;
    private static final int DRAIN_INTERVAL = 2;
    private static final int LAVA_CHARGE_PER_TICK = 1;
    private static final int FLIGHT_FIRE_TICKS = 20;
    private static final float NORMAL_FLYING_SPEED = 0.05F;
    private static final float TOP_FLYING_SPEED = 0.0878F;
    private static final int SPRINT_BOOST_DELAY_TICKS = 30;
    private static final int SPRINT_BOOST_RAMP_TICKS = 40;
    private static final int SPEED_STEPS = 4;

    private final PowerType<?> resource;
    private int flightTicks;
    private int sprintFlyTicks;

    public BlazeFlightPower(PowerType<?> type, LivingEntity entity, PowerType<?> resource) {
        super(type, entity);
        this.resource = resource;
        this.setTicking();
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
        VariableIntPower fire = fuel();
        boolean fueled = fire != null && fire.getValue() > 0;
        if (isActive() && fueled) {
            float wantedSpeed = sprintSpeed(player, abilities);
            if (!abilities.mayfly || abilities.getFlyingSpeed() != wantedSpeed) {
                abilities.mayfly = true;
                abilities.setFlyingSpeed(wantedSpeed);
                player.onUpdateAbilities();
            }
            if (abilities.flying) {
                flightTicks++;
                int before = fire.getValue();
                if (player.isInLava()) {
                    fire.setValue(Math.min(fire.getMax(), before + LAVA_CHARGE_PER_TICK));
                } else if (flightTicks % DRAIN_INTERVAL == 0) {
                    fire.setValue(Math.max(fire.getMin(), before - FLIGHT_DRAIN_PER_TICK));
                }
                if (fire.getValue() != before) {
                    PowerHolderComponent.syncPower(player, fire.getType());
                }
                player.setRemainingFireTicks(Math.max(player.getRemainingFireTicks(), FLIGHT_FIRE_TICKS));
            }
        } else {
            sprintFlyTicks = 0;
            revoke(player);
        }
    }

    @Override
    public void onLost() {
        if (!(entity instanceof Player player)) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        revoke(player);
    }

    private float sprintSpeed(Player player, Abilities abilities) {
        if (!abilities.flying || !player.isSprinting()) {
            sprintFlyTicks = 0;
            return NORMAL_FLYING_SPEED;
        }
        sprintFlyTicks++;
        float progress = Math.clamp((sprintFlyTicks - SPRINT_BOOST_DELAY_TICKS) / (float) SPRINT_BOOST_RAMP_TICKS, 0.0F, 1.0F);
        float eased = Math.round(progress * progress * (3.0F - 2.0F * progress) * SPEED_STEPS) / (float) SPEED_STEPS;
        return NORMAL_FLYING_SPEED + (TOP_FLYING_SPEED - NORMAL_FLYING_SPEED) * eased;
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

    private VariableIntPower fuel() {
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
