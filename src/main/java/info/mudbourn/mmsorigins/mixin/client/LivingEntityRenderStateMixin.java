package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.client.fur.FurState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Storage for {@link FurState} on every living render state.
 *
 * <p>Stamped on {@code PlayerRenderer} extraction and cleared on every other entity's,
 * since render states are pooled and reused.
 */
@Mixin(LivingEntityRenderState.class)
public abstract class LivingEntityRenderStateMixin implements FurState {

    @Unique
    private Identifier mmsOrigins$furOrigin;

    @Override
    public Identifier mmsOrigins$furOrigin() {
        return this.mmsOrigins$furOrigin;
    }

    @Override
    public void mmsOrigins$setFurOrigin(Identifier origin) {
        this.mmsOrigins$furOrigin = origin;
    }
}
