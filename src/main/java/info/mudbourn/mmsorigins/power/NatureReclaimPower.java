package info.mudbourn.mmsorigins.power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

/**
 * A slow nature spell cast from the off hand. The floran holds the primary key
 * with a crafted item in their off hand; a charging note sounds after the first
 * second, and once the full channel elapses the item is unmade into its parts.
 *
 * <p>The spell works any time the sun is up. Each cast spends sun power and adds
 * strain to the floran's sun metabolism, both scaled by how far the sun is from
 * noon, so the strain budget allows the most casts at noon. The power also warns
 * the floran once a day as noon approaches. The reclaim itself is in
 * {@link NatureReclaim}.
 */
public final class NatureReclaimPower extends Power implements Active {

    // groves where the old magic runs strong and reclamation strains the floran less
    private static final TagKey<Biome> ENCHANTED_GROVES =
        TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("mms_origins", "enchanted_groves"));

    private static final int HOLD_GRACE_TICKS = 3;

    private static final Identifier ROOTED_ID = Identifier.fromNamespaceAndPath("mms_origins", "nature_reclaim_rooted");

    private static final double NOON = 6000.0;

    private static final double HALF_DAY = 6000.0;

    private static final long DAY_LENGTH = 24000L;

    private static final double MIN_DAYLIGHT = 0.05;

    private final int castTicks;
    private final int soundDelay;
    private final SoundEvent chargingSound;
    private final SoundEvent finishSound;
    private final PowerType<?> strainResource;
    private final int noonStrain;
    private final PowerType<?> sunResource;
    private final int noonSunCost;
    private final SoundEvent noonSound;
    private final int noonWarning;

    private Key key = new Key();
    private int ticksSinceUse = HOLD_GRACE_TICKS + 1;
    private int charge;
    private boolean awaitingRelease;
    private long lastWarnedDay = -1L;

    public NatureReclaimPower(
            PowerType<?> type,
            LivingEntity entity,
            int castTicks,
            int soundDelay,
            SoundEvent chargingSound,
            SoundEvent finishSound,
            PowerType<?> strainResource,
            int noonStrain,
            PowerType<?> sunResource,
            int noonSunCost,
            SoundEvent noonSound,
            int noonWarning) {
        super(type, entity);
        this.castTicks = castTicks;
        this.soundDelay = soundDelay;
        this.chargingSound = chargingSound;
        this.finishSound = finishSound;
        this.strainResource = strainResource;
        this.noonStrain = noonStrain;
        this.sunResource = sunResource;
        this.noonSunCost = noonSunCost;
        this.noonSound = noonSound;
        this.noonWarning = noonWarning;
        this.setTicking(true);
    }

    @Override
    public void onUse() {
        ticksSinceUse = 0;
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public void setKey(Key key) {
        this.key = key;
    }

    @Override
    public void tick() {
        if (!(entity instanceof Player player) || !(entity.level() instanceof ServerLevel level)) {
            return;
        }
        warnOfNoon(player, level);
        channel(player, level);
        root(player, charge > 0);
    }

    @Override
    public void onLost() {
        if (entity instanceof Player player) {
            root(player, false);
        }
    }

    private void channel(Player player, ServerLevel level) {
        boolean held = ticksSinceUse <= HOLD_GRACE_TICKS;
        ticksSinceUse++;
        if (!held) {
            charge = 0;
            awaitingRelease = false;
            return;
        }
        if (awaitingRelease) {
            return;
        }
        if (player.getOffhandItem().isEmpty()) {
            charge = 0;
            return;
        }
        charge++;
        if (charge == soundDelay && chargingSound != null) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                chargingSound, SoundSource.PLAYERS, 0.7f, 1.0f);
        }
        if (charge > soundDelay) {
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                player.getX(), player.getY() + 1.0, player.getZ(), 2, 0.3, 0.4, 0.3, 0.0);
        }
        if (charge >= castTicks) {
            cast(player, level);
            charge = 0;
            awaitingRelease = true;
        }
    }

    private static void root(Player player, boolean rooted) {
        rootAttribute(player, Attributes.MOVEMENT_SPEED, rooted);
        rootAttribute(player, Attributes.JUMP_STRENGTH, rooted);
    }

    private static void rootAttribute(Player player, Holder<Attribute> attribute, boolean rooted) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null || instance.hasModifier(ROOTED_ID) == rooted) {
            return;
        }
        if (rooted) {
            instance.addTransientModifier(new AttributeModifier(
                ROOTED_ID,
                -1.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else {
            instance.removeModifier(ROOTED_ID);
        }
    }

    private void warnOfNoon(Player player, ServerLevel level) {
        long dayTime = level.getDayTime();
        long time = dayTime % DAY_LENGTH;
        long day = dayTime / DAY_LENGTH;
        if (time < NOON - noonWarning || time >= NOON || day == lastWarnedDay) {
            return;
        }
        lastWarnedDay = day;
        if (noonSound != null && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(noonSound),
                SoundSource.PLAYERS,
                player.getX(),
                player.getY(),
                player.getZ(),
                1.0f,
                1.0f,
                level.getRandom().nextLong()));
        }
        message(player, "message.mms_origins.nature_reclaim.noon_near");
    }

    private void cast(Player player, ServerLevel level) {
        double daylight = daylight(level);
        if (daylight <= 0.0) {
            message(player, "message.mms_origins.nature_reclaim.not_day");
            return;
        }
        VariableIntPower strain = resource(player, strainResource);
        int strainCost = scaled(noonStrain, daylight, strain);
        if (level.getBiome(player.blockPosition()).is(ENCHANTED_GROVES)) {
            strainCost = Math.max(1, strainCost / 2);
        }
        if (strain != null && strain.getValue() + strainCost > strain.getMax()) {
            message(player, "message.mms_origins.nature_reclaim.strained");
            return;
        }
        VariableIntPower sun = resource(player, sunResource);
        int sunCost = scaled(noonSunCost, daylight, sun);
        if (sun != null && sun.getValue() < sunCost) {
            message(player, "message.mms_origins.nature_reclaim.no_sun");
            return;
        }
        NatureReclaim.Result result = NatureReclaim.reclaim(player, level);
        switch (result) {
            case SUCCESS -> {
                spend(player, strain, -strainCost);
                spend(player, sun, sunCost);
                if (finishSound != null) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        finishSound, SoundSource.PLAYERS, 0.8f, 1.0f);
                }
                level.sendParticles(ParticleTypes.COMPOSTER,
                    player.getX(), player.getY() + 1.0, player.getZ(), 24, 0.4, 0.6, 0.4, 0.1);
                int left = strain == null || strainCost <= 0
                    ? 0
                    : (strain.getMax() - strain.getValue()) / strainCost;
                player.displayClientMessage(
                    Component.translatable("message.mms_origins.nature_reclaim.success", left),
                    true);
            }
            case NOT_ENOUGH -> message(player, "message.mms_origins.nature_reclaim.not_enough");
            default -> message(player, "message.mms_origins.nature_reclaim.no_recipe");
        }
    }

    private static double daylight(ServerLevel level) {
        double time = level.getDayTime() % DAY_LENGTH;
        return Math.max(0.0, 1.0 - Math.abs(time - NOON) / HALF_DAY);
    }

    private static int scaled(int noonCost, double daylight, VariableIntPower cap) {
        int cost = (int) Math.round(noonCost / Math.max(daylight, MIN_DAYLIGHT));
        return cap == null ? cost : Math.min(cost, cap.getMax());
    }

    private static void spend(Player player, VariableIntPower power, int amount) {
        if (power == null || amount == 0) {
            return;
        }
        power.setValue(power.getValue() - amount);
        PowerHolderComponent.syncPower(player, power.getType());
    }

    private VariableIntPower resource(Player player, PowerType<?> resourceType) {
        if (resourceType == null) {
            return null;
        }
        for (VariableIntPower power : PowerHolderComponent.getPowers(player, VariableIntPower.class)) {
            if (power.getType().getIdentifier().equals(resourceType.getIdentifier())) {
                return power;
            }
        }
        return null;
    }

    private void message(Player player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
    }
}
