package info.mudbourn.mmsorigins.power;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;

/**
 * Grants creative-style flight to a fed player and takes it away once they grow
 * too hungry. Reproduces the flight of Fantastic Wings' insectoid wings, which
 * this project cannot depend on. The power only sets {@code mayfly}; vanilla's
 * own double-tap-jump handling toggles {@code flying} from there, and flying
 * slowly drains hunger so the ability costs food rather than nothing.
 */
public final class FairyFlightPower extends Power {

    private static final int REQUIRED_FOOD_LEVEL = 6;
    private static final float EXHAUSTION_PER_FLYING_TICK = 1.0E-4F;
    private static final float FAINT_HEALTH_THRESHOLD = 6.0F;
    private static final float NORMAL_FLYING_SPEED = 0.05F;
    private static final float FAINT_FLYING_SPEED = 0.025F;

    public FairyFlightPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
        // Tick even while a condition holds this inactive, so Anxious Heart can strip flight in flight.
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
        boolean fedEnough = isActive() && player.getFoodData().getFoodLevel() >= REQUIRED_FOOD_LEVEL;
        if (fedEnough) {
            // A fairy down to three hearts or less flies at a faltering pace.
            float wantedSpeed = player.getHealth() <= FAINT_HEALTH_THRESHOLD ? FAINT_FLYING_SPEED : NORMAL_FLYING_SPEED;
            boolean changed = !abilities.mayfly || abilities.getFlyingSpeed() != wantedSpeed;
            abilities.mayfly = true;
            abilities.setFlyingSpeed(wantedSpeed);
            if (changed) {
                player.onUpdateAbilities();
            }
            if (abilities.flying) {
                player.getFoodData().addExhaustion(EXHAUSTION_PER_FLYING_TICK);
            }
        } else if (abilities.mayfly || abilities.flying) {
            abilities.mayfly = false;
            abilities.flying = false;
            abilities.setFlyingSpeed(NORMAL_FLYING_SPEED);
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
            abilities.setFlyingSpeed(NORMAL_FLYING_SPEED);
            player.onUpdateAbilities();
        }
    }
}
