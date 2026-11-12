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
 * only draws on it. When it empties the wings fail and the blaze drops. The
 * server only grants and revokes {@code mayfly}; vanilla's double-tap-jump
 * toggles {@code flying}, and the holder's client sets the flying speed from
 * {@link SurgingFlight}. Sprinting aloft for a moment eases the speed up until
 * it follows the shared surge curve.
 */
public final class BlazeFlightPower extends Power implements SurgingFlight {

    private static final int FLIGHT_DRAIN_PER_TICK = 1;
    private static final int DRAIN_INTERVAL = 2;
    private static final int LAVA_CHARGE_PER_TICK = 1;
    private static final int FLIGHT_FIRE_TICKS = 20;
    private static final float NORMAL_FLYING_SPEED = 0.05F;

    private final PowerType<?> resource;
    private int flightTicks;

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
            abilities.setFlyingSpeed(NORMAL_FLYING_SPEED);
            if (!abilities.mayfly) {
                abilities.mayfly = true;
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

    @Override
    public float cruiseSpeed(Player player) {
        return NORMAL_FLYING_SPEED;
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
