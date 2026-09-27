package info.mudbourn.mmsorigins.client.fur;

import net.minecraft.resources.Identifier;

/**
 * Carries a player's origin id, beastfolk tail option, and claw mode from
 * render-state extraction to the fur and beastfolk feature layers.
 *
 * <p>Render states hold no back-reference to the entity, so these have to be read off
 * the player while it is still in hand and stamped here. The fur layer resolves the
 * origin against {@link FurModels} at draw time; null means no fur, and a null tail
 * option means no tail.
 */
public interface FurState {

    Identifier mmsOrigins$furOrigin();

    void mmsOrigins$setFurOrigin(Identifier origin);

    Identifier mmsOrigins$tailOption();

    void mmsOrigins$setTailOption(Identifier option);

    boolean mmsOrigins$clawsOut();

    void mmsOrigins$setClawsOut(boolean out);
}
