package info.mudbourn.mmsorigins.mixin;

import info.mudbourn.mmsorigins.MmsOriginsPowers;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class AvianClimbMixin {

    private static final double CLIMB_ACCELERATION = 0.08;
    private static final double MAX_CLIMB_SPEED = 0.6;
    private static final double CLIMB_DRAG = 0.95;
    private static final int CHARGE_INTERVAL = 4;
    // 50 stamina per second, half a surge
    private static final int CHARGE = 10;
    // 75 stamina per second, half a heavy surge
    private static final int HEAVY_CHARGE = 15;
    private static final int HEAVY_ARMOR = 13;
    private static final int GROUNDING_ARMOR = 20;
    private static final String STAMINA_PATH = "gift_of_the_skies_stamina";

    @Shadow
    protected boolean jumping;

    @Inject(method = "travel", at = @At("TAIL"))
    private void mmsOrigins$avianClimb(Vec3 input, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player) || !player.isFallFlying() || !mmsOrigins$holdsJump(player)) {
            return;
        }
        if (!MmsOriginsPowers.holds(player, MmsOriginsPowers.AVIAN_PEACE)) {
            return;
        }
        int armor = player.getArmorValue();
        VariableIntPower stamina = mmsOrigins$stamina(player);
        if (armor >= GROUNDING_ARMOR || stamina == null || stamina.getValue() <= stamina.getMin()) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        double rise = Math.min(MAX_CLIMB_SPEED, Math.max(motion.y, 0.0) + CLIMB_ACCELERATION);
        player.setDeltaMovement(motion.x * CLIMB_DRAG, rise, motion.z * CLIMB_DRAG);
        if (player instanceof ServerPlayer && player.tickCount % CHARGE_INTERVAL == 0) {
            int cost = armor >= HEAVY_ARMOR ? HEAVY_CHARGE : CHARGE;
            stamina.setValue(Math.max(stamina.getMin(), stamina.getValue() - cost));
            PowerHolderComponent.syncPower(player, stamina.getType());
        }
    }

    private boolean mmsOrigins$holdsJump(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return serverPlayer.getLastClientInput().jump();
        }
        return jumping;
    }

    private static VariableIntPower mmsOrigins$stamina(Player player) {
        for (VariableIntPower power : PowerHolderComponent.getPowers(player, VariableIntPower.class)) {
            if (power.getType().getIdentifier().getPath().equals(STAMINA_PATH)) {
                return power;
            }
        }
        return null;
    }
}
