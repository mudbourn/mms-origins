package info.mudbourn.mmsorigins.power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A slow nature spell cast on a held item. The floran holds the primary key
 * with a crafted item in their off hand, or sneaks with an empty off hand to
 * offer the main-hand item instead, which is the only way to offer a two-handed
 * weapon; a charging note sounds after the first second, and once the full
 * channel elapses the item is unmade into its parts.
 *
 * <p>The floran's strength follows a tide of day and night. A cast costs a share
 * of the full sun reserve that falls from {@code dawn_cost} at sunrise and sunset
 * to {@code noon_cost} at noon, never below {@code shaded_cost} away from open sky
 * so the hour alone grants nothing underground, and each cast is followed by a hard cooldown. The
 * reserve the floran can hold shrinks toward {@code night_capacity} of its maximum
 * as the sun sets and swells back as it rises, spilling any sun above it. The power
 * also warns the floran once a day as noon approaches. The reclaim itself is in
 * {@link NatureReclaim}.
 */
public final class NatureReclaimPower extends Power implements Active {

    private static final int HOLD_GRACE_TICKS = 3;

    private static final Identifier ROOTED_ID = Identifier.fromNamespaceAndPath("mms_origins", "nature_reclaim_rooted");

    private static final double NOON = 6000.0;

    private static final double HALF_DAY = 6000.0;

    private static final long DAY_LENGTH = 24000L;

    private static final String LAST_CAST_KEY = "LastCast";

    private final int castTicks;
    private final int soundDelay;
    private final SoundEvent chargingSound;
    private final SoundEvent finishSound;
    private final PowerType<?> sunResource;
    private final double dawnCost;
    private final double noonCost;
    private final double shadedCost;
    private final int cooldown;
    private final double nightCapacity;
    private final SoundEvent noonSound;
    private final int noonWarning;

    private Key key = new Key();
    private int ticksSinceUse = HOLD_GRACE_TICKS + 1;
    private int charge;
    private boolean awaitingRelease;
    private long lastWarnedDay = -1L;
    private long lastCast = Long.MIN_VALUE;

    public NatureReclaimPower(
            PowerType<?> type,
            LivingEntity entity,
            int castTicks,
            int soundDelay,
            SoundEvent chargingSound,
            SoundEvent finishSound,
            PowerType<?> sunResource,
            double dawnCost,
            double noonCost,
            double shadedCost,
            int cooldown,
            double nightCapacity,
            SoundEvent noonSound,
            int noonWarning) {
        super(type, entity);
        this.castTicks = castTicks;
        this.soundDelay = soundDelay;
        this.chargingSound = chargingSound;
        this.finishSound = finishSound;
        this.sunResource = sunResource;
        this.dawnCost = dawnCost;
        this.noonCost = noonCost;
        this.shadedCost = shadedCost;
        this.cooldown = cooldown;
        this.nightCapacity = nightCapacity;
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
        ebbReserve(player, level);
        channel(player, level);
        root(player, charge > 0);
    }

    @Override
    public Tag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putLong(LAST_CAST_KEY, lastCast);
        return tag;
    }

    @Override
    public void fromTag(Tag tag) {
        if (tag instanceof CompoundTag compound) {
            lastCast = compound.getLongOr(LAST_CAST_KEY, Long.MIN_VALUE);
        }
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
        if (subject(player).isEmpty()) {
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

    /** The stack offered for unmaking: the off hand, or the main hand while sneaking with an empty off hand. */
    private static ItemStack subject(Player player) {
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() || !player.isShiftKeyDown()) {
            return offhand;
        }
        return player.getMainHandItem();
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
        long dayTime = level.getOverworldClockTime();
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
        long elapsed = level.getGameTime() - lastCast;
        if (elapsed >= 0 && elapsed < cooldown) {
            player.sendOverlayMessage(Component.translatable(
                "message.mms_origins.nature_reclaim.cooldown",
                (cooldown - elapsed + 19) / 20));
            return;
        }
        VariableIntPower sun = resource(player, sunResource);
        int sunCost = sun == null ? 0 : (int) Math.ceil(sun.getMax() * costShare(player, daylight));
        if (sun != null && sun.getValue() < sunCost) {
            message(player, "message.mms_origins.nature_reclaim.no_sun");
            return;
        }
        NatureReclaim.Result result = NatureReclaim.reclaim(player, level, subject(player));
        switch (result) {
            case SUCCESS -> {
                spend(player, sun, sunCost);
                lastCast = level.getGameTime();
                if (finishSound != null) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        finishSound, SoundSource.PLAYERS, 0.8f, 1.0f);
                }
                level.sendParticles(ParticleTypes.COMPOSTER,
                    player.getX(), player.getY() + 1.0, player.getZ(), 24, 0.4, 0.6, 0.4, 0.1);
                player.sendOverlayMessage(
                    Component.translatable("message.mms_origins.nature_reclaim.success", sunCost));
            }
            case NOT_ENOUGH -> message(player, "message.mms_origins.nature_reclaim.not_enough");
            default -> message(player, "message.mms_origins.nature_reclaim.no_recipe");
        }
    }

    private static double daylight(ServerLevel level) {
        double time = level.getOverworldClockTime() % DAY_LENGTH;
        return Math.max(0.0, 1.0 - Math.abs(time - NOON) / HALF_DAY);
    }

    /** The share of the full reserve one cast costs, sliding from dawn to noon and back, floored when shaded. */
    private double costShare(Player player, double daylight) {
        double share = dawnCost + (noonCost - dawnCost) * daylight;
        if (player.level().canSeeSky(player.blockPosition().above())) {
            return share;
        }
        return Math.max(share, shadedCost);
    }

    /** Spills any sun above the reserve the hour allows, which ebbs at dusk and swells at dawn. */
    private void ebbReserve(Player player, ServerLevel level) {
        VariableIntPower sun = resource(player, sunResource);
        if (sun == null) {
            return;
        }
        double share = nightCapacity + (1.0 - nightCapacity) * daylight(level);
        int capacity = (int) Math.round(sun.getMax() * share);
        if (sun.getValue() > capacity) {
            sun.setValue(capacity);
            PowerHolderComponent.syncPower(player, sun.getType());
        }
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
        player.sendOverlayMessage(Component.translatable(key));
    }
}
