package info.mudbourn.mmsorigins;

import java.util.UUID;

/**
 * A piglin's private grudge: the kinsman it wants to hit back before it will
 * settle. Implemented on {@code AbstractPiglin} so both the neutrality test and
 * the pursuit tick can read and clear it.
 */
public interface GrudgeHolder {

    UUID mmsOrigins$getGrudge();

    void mmsOrigins$setGrudge(UUID grudge);
}
