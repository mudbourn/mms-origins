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
 * this project cannot depend on. The server only grants and revokes
 * {@code mayfly}; vanilla's own double-tap-jump handling toggles {@code flying}
 * from there, the holder's client sets the flying speed from
 * {@link SurgingFlight}, and flying slowly drains hunger so the ability costs
 * food rather than nothing. It ticks while inactive so Anxious Heart can strip
 * flight in the air.
 */
public final class FairyFlightPower extends Power implements SurgingFlight {

    private static final int REQUIRED_FOOD_LEVEL = 6;
    private static final float EXHAUSTION_PER_FLYING_TICK = 1.0E-4F;
    private static final float FAINT_HEALTH_THRESHOLD = 6.0F;
    private static final float NORMAL_FLYING_SPEED = 0.05F;
    private static final float FAINT_FLYING_SPEED = 0.025F;
    private static final float HOMELAND_SPEED_MULTIPLIER = 1.5F;

    // The fairy homeland groves, where flight quickens.
    private static final TagKey<Biome> HOMELAND_BIOMES =
        TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("mms_origins", "fairy_homeland"));

    public FairyFlightPower(PowerType<?> type, LivingEntity entity) {
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
        boolean fedEnough = isActive() && player.getFoodData().getFoodLevel() >= REQUIRED_FOOD_LEVEL;
        if (fedEnough) {
            abilities.setFlyingSpeed(cruiseSpeed(player));
            if (!abilities.mayfly) {
                abilities.mayfly = true;
                player.onUpdateAbilities();
            }
            if (abilities.flying) {
                player.getFoodData().addExhaustion(EXHAUSTION_PER_FLYING_TICK);
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
        float speed = player.getHealth() <= FAINT_HEALTH_THRESHOLD ? FAINT_FLYING_SPEED : NORMAL_FLYING_SPEED;
        return inHomeland(player) ? speed * HOMELAND_SPEED_MULTIPLIER : speed;
    }


    @Override
    public boolean surging(Player player) {
        return player.isSprinting();
    }

    private static void revoke(Player player) {
        Abilities abilities = player.getAbilities();
        if (abilities.mayfly || abilities.flying) {
            abilities.mayfly = false;
            abilities.flying = false;
            abilities.setFlyingSpeed(NORMAL_FLYING_SPEED);
            player.onUpdateAbilities();
        }
    }

    private static boolean inHomeland(Player player) {
        return player.level().getBiome(player.blockPosition()).is(HOMELAND_BIOMES);
    }
}
