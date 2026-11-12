package info.mudbourn.mmsorigins.power;

import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.util.HudRender;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * An active key that tells a tap from a hold. Apoli reports a continuous key once per tick
 * while it is down and never reports its release, so the server counts those ticks itself.
 * A key let go before {@code holdTicks} runs the press action at release; a key still down
 * at {@code holdTicks} runs the hold action at once. Each action has its own entity condition,
 * checked at the moment it would run, and either one spends the shared cooldown.
 */
public final class KeyGesturePower extends CooldownPower implements Active {

    private static final int RELEASE_GRACE_TICKS = 5;

    private final int holdTicks;
    private final Consumer<Entity> pressAction;
    private final Predicate<Entity> pressCondition;
    private final Consumer<Entity> holdAction;
    private final Predicate<Entity> holdCondition;

    private Key key = new Key();
    private int ticksSinceUse = RELEASE_GRACE_TICKS + 1;
    private int heldTicks;
    private long lastUseTick = -1L;

    public KeyGesturePower(
            PowerType<?> type,
            LivingEntity entity,
            int cooldownDuration,
            HudRender hudRender,
            int holdTicks,
            Consumer<Entity> pressAction,
            Predicate<Entity> pressCondition,
            Consumer<Entity> holdAction,
            Predicate<Entity> holdCondition) {
        super(type, entity, cooldownDuration, hudRender);
        this.holdTicks = holdTicks;
        this.pressAction = pressAction;
        this.pressCondition = pressCondition;
        this.holdAction = holdAction;
        this.holdCondition = holdCondition;
        this.setTicking(true);
    }

    @Override
    public void onUse() {
        if (!(entity.level() instanceof ServerLevel)) {
            return;
        }
        long now = entity.level().getGameTime();
        if (now == lastUseTick) {
            return;
        }
        lastUseTick = now;
        if (ticksSinceUse > RELEASE_GRACE_TICKS) {
            heldTicks = 0;
        }
        ticksSinceUse = 0;
        heldTicks++;
        if (heldTicks == holdTicks) {
            fire(holdAction, holdCondition);
        }
    }

    @Override
    public void tick() {
        if (!(entity.level() instanceof ServerLevel)) {
            return;
        }
        ticksSinceUse++;
        if (heldTicks > 0 && ticksSinceUse > RELEASE_GRACE_TICKS) {
            if (heldTicks < holdTicks) {
                fire(pressAction, pressCondition);
            }
            heldTicks = 0;
        }
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public void setKey(Key key) {
        this.key = key;
    }

    private void fire(Consumer<Entity> action, Predicate<Entity> condition) {
        if (action == null || !isActive() || !canUse()) {
            return;
        }
        if (condition != null && !condition.test(entity)) {
            return;
        }
        action.accept(entity);
        use();
    }
}
