package info.mudbourn.mmsorigins.mixin.client;

import io.github.apace100.apoli.screen.PowerHudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Puts the Apoli resource bars above every row of mount hearts. Apoli lifts
 * them 8 pixels per 20 max health, while vanilla stacks mount hearts in rows
 * of ten, 10 pixels apart, capped at three rows.
 */
@Mixin(PowerHudRenderer.class)
public abstract class PowerHudMountOffsetMixin {

    @ModifyVariable(
        method = "render",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"),
        ordinal = 1
    )
    private int mmsOrigins$mountRows(int y) {
        if (!(Minecraft.getInstance().player.getVehicle() instanceof LivingEntity vehicle)) {
            return y;
        }
        int apoliOffset = 8 * (int) (vehicle.getMaxHealth() / 20.0F);
        int hearts = vehicle.showVehicleHealth() ? Math.min((int) (vehicle.getMaxHealth() + 0.5F) / 2, 30) : 0;
        int rows = (hearts + 9) / 10;
        return y + apoliOffset - 10 * Math.max(rows - 1, 0);
    }
}
