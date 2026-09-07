package info.mudbourn.mmsorigins.client.fur;

import net.minecraft.resources.Identifier;

/**
 * Carries a player's origin id from render-state extraction to the fur feature layer.
 *
 * <p>Render states hold no back-reference to the entity, so the origin has to be read
 * off the player while it is still in hand and stamped here. The feature layer then
 * resolves it against {@link FurModels} at draw time. Null means no fur.
 */
public interface FurState {

    Identifier mmsOrigins$furOrigin();

    void mmsOrigins$setFurOrigin(Identifier origin);

    float mmsOrigins$furVerticalSpeed();

    void mmsOrigins$setFurVerticalSpeed(float verticalSpeed);
}
