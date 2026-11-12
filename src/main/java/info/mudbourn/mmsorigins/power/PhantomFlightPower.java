package info.mudbourn.mmsorigins.power;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;

/**
 * Lets a phantom in phantom form fly with no timer and no cost. Flight is granted while
 * the power's condition holds, and the form keeps {@code flying} on so a phantom never
 * walks. The holder's client sets the flying
 * speed from {@link SurgingFlight}, and the winged movement itself is the holder's client
 * physics. Vanilla's sprint doubles the speed, and sprinting aloft for a moment eases it
 * up along the shared surge curve in {@link SurgingFlight}.
 */
public final class PhantomFlightPower extends Power implements SurgingFlight {

    private static final float FLYING_SPEED = 0.04F;
    private static final float NORMAL_FLYING_SPEED = 0.05F;

    public PhantomFlightPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
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
        if (player.level().isClientSide()) {
            if (isActive() && abilities.mayfly && !abilities.flying && !player.isPassenger()) {
                abilities.flying = true;
                player.onUpdateAbilities();
            }
            return;
        }
        if (isActive() && !MmsOriginsPowers.downed(player)) {
            abilities.setFlyingSpeed(FLYING_SPEED);
            boolean airborne = !player.isPassenger();
            if (!abilities.mayfly || abilities.flying != airborne) {
                abilities.mayfly = true;
                abilities.flying = airborne;
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
