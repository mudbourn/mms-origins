package info.mudbourn.mmsorigins.power;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;

/**
 * Lets a blazeborn take wing for as long as it burns. Flight is granted only
 * while the bearer is alight, and staying aloft saps that fire far faster than
 * it would ebb on the ground, so a blaze must keep stoking itself in lava or
 * flame to stay in the air. When the last of the fire dies the wings fail and
 * the blaze drops. Only {@code mayfly} is set; vanilla's double-tap-jump toggles
 * {@code flying} from there.
 */
public final class BlazeFlightPower extends Power {

    private static final int EXTRA_FIRE_DRAIN_PER_TICK = 3;

    public BlazeFlightPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
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
        boolean alight = player.getRemainingFireTicks() > 0;
        if (isActive() && alight) {
            if (!abilities.mayfly) {
                abilities.mayfly = true;
                player.onUpdateAbilities();
            }
            if (abilities.flying) {
                // Staying aloft burns through the fire that fuels the flight.
                int remaining = player.getRemainingFireTicks();
                player.setRemainingFireTicks(Math.max(0, remaining - EXTRA_FIRE_DRAIN_PER_TICK));
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
}
