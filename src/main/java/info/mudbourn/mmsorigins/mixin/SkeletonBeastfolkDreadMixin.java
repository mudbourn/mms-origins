package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vicious Appearance: skeletons shy from a beastfolk as they do from a wolf.
 *
 * <p>This adds a second avoid goal beside the vanilla wolf one, with the same
 * reach and speeds, that flees players holding an active Vicious Appearance.
 */
@Mixin(AbstractSkeleton.class)
public abstract class SkeletonBeastfolkDreadMixin extends Monster {

    protected SkeletonBeastfolkDreadMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Inject(method = "registerGoals", at = @At("RETURN"))
    private void mmsOrigins$fearBeastfolk(CallbackInfo ci) {
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(
            this,
            Player.class,
            MmsOriginsPowers.VICIOUS_APPEARANCE::isActive,
            6.0F,
            1.0,
            1.2,
            EntitySelector.NO_CREATIVE_OR_SPECTATOR::test
        ));
    }
}
