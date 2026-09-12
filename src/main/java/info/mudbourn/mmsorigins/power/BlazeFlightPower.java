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
 * {@code mayfly} is set; vanilla's double-tap-jump toggles {@code flying}.
 */
public final class BlazeFlightPower extends Power {

    private static final int FLIGHT_DRAIN_PER_TICK = 1;
    private static final int DRAIN_INTERVAL = 2;
    private static final int LAVA_CHARGE_PER_TICK = 1;
    private static final int FLIGHT_FIRE_TICKS = 20;

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
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        Abilities abilities = player.getAbilities();
        VariableIntPower fire = fuel();
        boolean fueled = fire != null && fire.getValue() > 0;
        if (isActive() && fueled) {
            if (!abilities.mayfly) {
                abilities.mayfly = true;
                player.onUpdateAbilities();
            }
            if (abilities.flying && !player.level().isClientSide()) {
                flightTicks++;
                if (player.isInLava()) {
                    // Flying through lava feeds the bar instead of spending it.
                    fire.setValue(Math.min(fire.getMax(), fire.getValue() + LAVA_CHARGE_PER_TICK));
                } else if (flightTicks % DRAIN_INTERVAL == 0) {
                    // Staying aloft spends the fire power, but only once every few ticks to keep it cheap.
                    fire.setValue(Math.max(fire.getMin(), fire.getValue() - FLIGHT_DRAIN_PER_TICK));
                }
                PowerHolderComponent.syncPower(player, fire.getType());
                // Burning openly marks the fire power being spent to fly.
                player.setRemainingFireTicks(Math.max(player.getRemainingFireTicks(), FLIGHT_FIRE_TICKS));
            }
        } else if (abilities.mayfly || abilities.flying) {
            abilities.mayfly = false;
            abilities.flying = false;
            player.onUpdateAbilities();
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
        Abilities abilities = player.getAbilities();
        if (abilities.mayfly || abilities.flying) {
            abilities.mayfly = false;
            abilities.flying = false;
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
