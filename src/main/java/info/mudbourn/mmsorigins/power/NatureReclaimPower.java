package info.mudbourn.mmsorigins.power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biomes;

/**
 * A slow nature spell cast from the off hand. The floran holds the primary key
 * with a crafted item in their off hand; a charging note sounds after the first
 * second, and once the full channel elapses the item is unmade into its parts.
 *
 * <p>Apoli cannot count held ticks or reach the off-hand recipe, so the timing,
 * the noon window, and the twice-a-day cost all live here. The reclaim itself is
 * in {@link NatureReclaim}, and the daily allowance is a resource the power
 * spends on success.
 */
public final class NatureReclaimPower extends Power implements Active {

    private static final int HOLD_GRACE_TICKS = 3;

    private final int castTicks;
    private final int soundDelay;
    private final SoundEvent chargingSound;
    private final SoundEvent finishSound;
    private final PowerType<?> usesResource;
    private final PowerType<?> sunResource;
    private final int sunCost;
    private final int noonMin;
    private final int noonMax;

    private Key key = new Key();
    private int ticksSinceUse = HOLD_GRACE_TICKS + 1;
    private int charge;
    private boolean awaitingRelease;

    public NatureReclaimPower(
            PowerType<?> type,
            LivingEntity entity,
            int castTicks,
            int soundDelay,
            SoundEvent chargingSound,
            SoundEvent finishSound,
            PowerType<?> usesResource,
            PowerType<?> sunResource,
            int sunCost,
            int noonMin,
            int noonMax) {
        super(type, entity);
        this.castTicks = castTicks;
        this.soundDelay = soundDelay;
        this.chargingSound = chargingSound;
        this.finishSound = finishSound;
        this.usesResource = usesResource;
        this.sunResource = sunResource;
        this.sunCost = sunCost;
        this.noonMin = noonMin;
        this.noonMax = noonMax;
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

    private void cast(Player player, ServerLevel level) {
        if (!level.getBiome(player.blockPosition()).is(Biomes.DARK_FOREST)) {
            message(player, "message.mms_origins.nature_reclaim.not_forest");
            return;
        }
        if (!isAroundNoon(level)) {
            message(player, "message.mms_origins.nature_reclaim.not_noon");
            return;
        }
        VariableIntPower uses = resource(player, usesResource);
        if (uses != null && uses.getValue() <= 0) {
            message(player, "message.mms_origins.nature_reclaim.spent");
            return;
        }
        VariableIntPower sun = resource(player, sunResource);
        if (sun != null && sun.getValue() < sunCost) {
            message(player, "message.mms_origins.nature_reclaim.no_sun");
            return;
        }
        NatureReclaim.Result result = NatureReclaim.reclaim(player, level);
        switch (result) {
            case SUCCESS -> {
                if (uses != null) {
                    uses.setValue(uses.getValue() - 1);
                    PowerHolderComponent.syncPower(player, uses.getType());
                }
                if (sun != null && sunCost > 0) {
                    sun.setValue(sun.getValue() - sunCost);
                    PowerHolderComponent.syncPower(player, sun.getType());
                }
                if (finishSound != null) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        finishSound, SoundSource.PLAYERS, 0.8f, 1.0f);
                }
                level.sendParticles(ParticleTypes.COMPOSTER,
                    player.getX(), player.getY() + 1.0, player.getZ(), 24, 0.4, 0.6, 0.4, 0.1);
                message(player, "message.mms_origins.nature_reclaim.success");
            }
            case NOT_ENOUGH -> message(player, "message.mms_origins.nature_reclaim.not_enough");
            default -> message(player, "message.mms_origins.nature_reclaim.no_recipe");
        }
    }

    private boolean isAroundNoon(ServerLevel level) {
        long time = level.getDayTime() % 24000L;
        return time >= noonMin && time <= noonMax;
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
