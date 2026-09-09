package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.GrudgeHolder;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

/**
 * Stores a piglin's grudge, the kinsman it means to strike back before it calms.
 */
@Mixin(AbstractPiglin.class)
public abstract class AbstractPiglinGrudgeMixin implements GrudgeHolder {

    @Unique
    private UUID mmsOrigins$grudge;

    @Override
    public UUID mmsOrigins$getGrudge() {
        return this.mmsOrigins$grudge;
    }

    @Override
    public void mmsOrigins$setGrudge(UUID grudge) {
        this.mmsOrigins$grudge = grudge;
    }
}
