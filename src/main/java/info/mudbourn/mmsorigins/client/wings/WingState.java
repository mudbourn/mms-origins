package info.mudbourn.mmsorigins.client.wings;

import net.minecraft.resources.Identifier;

/**
 * Carries the elytrian's chosen wing option from render-state extraction to the wing layer.
 *
 * <p>The choice lives in the {@code originstweaks:elytrian_options} layer on the player,
 * which the wing feature layer never sees; it is read off the player during extraction
 * and stamped here for the layer to resolve against {@link WingModels}. Null means the
 * player is not an elytrian, or picked no custom wing.
 */
public interface WingState {

    Identifier mmsOrigins$wingOption();

    void mmsOrigins$setWingOption(Identifier option);

    float mmsOrigins$wingFlapDegrees();

    void mmsOrigins$setWingFlapDegrees(float degrees);
}
