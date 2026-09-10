package info.mudbourn.mmsorigins.client.entity;

import info.mudbourn.mmsorigins.MmsOrigins;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

/**
 * Draws the floran henchman as a zombie wearing its own skin. The behaviour and
 * proportions are the vanilla zombie's, so only the texture changes.
 */
public class FloranHenchmanRenderer extends ZombieRenderer {

    private static final Identifier TEXTURE =
        Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "textures/entity/floran_henchman.png");

    public FloranHenchmanRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState state) {
        return TEXTURE;
    }
}
