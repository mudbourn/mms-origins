package info.mudbourn.mmsorigins.mixin.uselessreptile;

import info.mudbourn.mmsorigins.Beastmasters;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Wild Useless Reptile dragons hunt players, which they otherwise leave alone until struck. Beastmasters by class are
// spared: a dragon stays patient with them, though it still answers a blow. Tamed dragons never pick up this target.
@Pseudo
@Mixin(targets = "nordmods.uselessreptile.common.entity.base.URDragonEntity", remap = false)
public abstract class DragonHostilityMixin extends TamableAnimal {

    protected DragonHostilityMixin(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void mmsOrigins$huntPlayers(CallbackInfo ci) {
        if (this.level() == null || this.level().isClientSide()) {
            return;
        }
        this.targetSelector.addGoal(4, new NonTameRandomTargetGoal<>(this, Player.class, false,
            (target, level) -> target instanceof Player player && !player.isCreative() && !player.isSpectator()
                && !Beastmasters.hasClass(player)));
    }
}
