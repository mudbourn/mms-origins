package info.mudbourn.mmsorigins.power;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
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
    private static final float HOMELAND_SPEED_MULTIPLIER = 1.5F;
    private static final int SPRINT_BOOST_DELAY_TICKS = 30;
    private static final int SPRINT_BOOST_RAMP_TICKS = 40;
    private static final float SPRINT_BOOST_MAX = 2.0F;

    private int sprintFlyTicks;

    // the fairy homeland groves, where flight quickens
    private static final TagKey<Biome> HOMELAND_BIOMES =
        TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("mms_origins", "fairy_homeland"));

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
            if (inHomeland(player)) {
                wantedSpeed *= HOMELAND_SPEED_MULTIPLIER;
            }
            wantedSpeed *= sprintBoost(player, abilities);
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

    private float sprintBoost(Player player, Abilities abilities) {
        if (!abilities.flying || !player.isSprinting()) {
            sprintFlyTicks = 0;
            return 1.0F;
        }
        sprintFlyTicks++;
        float progress = Math.clamp((sprintFlyTicks - SPRINT_BOOST_DELAY_TICKS) / (float) SPRINT_BOOST_RAMP_TICKS, 0.0F, 1.0F);
        float eased = progress * progress * (3.0F - 2.0F * progress);
        return 1.0F + (SPRINT_BOOST_MAX - 1.0F) * eased;
    }

    private boolean inHomeland(Player player) {
        return player.level().getBiome(player.blockPosition()).is(HOMELAND_BIOMES);
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
