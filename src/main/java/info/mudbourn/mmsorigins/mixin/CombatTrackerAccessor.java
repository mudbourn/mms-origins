package info.mudbourn.mmsorigins.mixin;

import java.util.List;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.CombatTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the combat tracker's per-blow log so {@code BeastfolkDeathCryMixin}
 * can weigh who dealt the killing damage.
 */
@Mixin(CombatTracker.class)
public interface CombatTrackerAccessor {

    @Accessor("entries")
    List<CombatEntry> mmsOrigins$getEntries();
}
