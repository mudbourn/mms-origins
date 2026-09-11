package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MoltenCharge;
import io.github.apace100.apoli.power.CooldownPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Exposes a way to hurry any {@link CooldownPower} along, used by
 * {@code MoltenChargeCharger} to charge a blazeborn's abilities faster while it
 * burns. Winding {@code lastUseTime} back is what both {@code getRemainingTicks}
 * and {@code canUse} read from, so the HUD bar and the actual readiness advance
 * together.
 */
@Mixin(CooldownPower.class)
public abstract class CooldownAccelMixin implements MoltenCharge {

    @Shadow
    protected long lastUseTime;

    @Override
    public void mmsOrigins$accelerate(long extraTicks) {
        CooldownPower self = (CooldownPower) (Object) this;
        if (self.getRemainingTicks() > 0) {
            this.lastUseTime -= extraTicks;
        }
    }
}
