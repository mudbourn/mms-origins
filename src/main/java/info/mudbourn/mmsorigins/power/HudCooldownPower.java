package info.mudbourn.mmsorigins.power;

import io.github.apace100.apoli.power.ActiveCooldownPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.util.HudRender;
import java.util.function.Consumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * An {@code active_self} whose cooldown bar is always on screen, not just while
 * recharging. Apoli's {@link ActiveCooldownPower} hides the bar whenever the
 * power is ready, so the taming ability showed nothing until its first use and
 * again once recharged. This keeps it rendered so the bar reads full when ready,
 * empties on use, and fills back over the cooldown.
 */
public final class HudCooldownPower extends ActiveCooldownPower {

    public HudCooldownPower(
            PowerType<?> type,
            LivingEntity entity,
            int cooldownDuration,
            HudRender hudRender,
            Consumer<Entity> activeFunction) {
        super(type, entity, cooldownDuration, hudRender, activeFunction);
    }

    @Override
    public boolean shouldRender() {
        return true;
    }
}
