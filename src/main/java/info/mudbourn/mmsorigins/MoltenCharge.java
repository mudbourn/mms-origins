package info.mudbourn.mmsorigins;

/**
 * Duck-typed onto every Apoli {@code CooldownPower} by {@code CooldownAccelMixin},
 * so {@code MoltenChargeCharger} can hurry a blazeborn's ability cooldowns along
 * while it bathes in lava or fire.
 */
public interface MoltenCharge {

    /**
     * Winds the cooldown back by the given ticks, banking extra recovery while
     * the bearer burns. Does nothing once the cooldown is already spent.
     */
    void mmsOrigins$accelerate(long extraTicks);
}
