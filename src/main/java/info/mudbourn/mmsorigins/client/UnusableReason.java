package info.mudbourn.mmsorigins.client;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.MultiplePowerType;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.power.PreventItemUsePower;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Names the power behind Apoli's "Unusable" item tooltip. Apoli skips hidden powers there,
 * and every sub-power of an {@code origins:multiple} is hidden, so a phantom's or a diet's
 * item block showed only a count such as "(1)". This swaps the count for the names of the
 * visible powers that own the blocking sub-powers.
 */
public final class UnusableReason {

    private static final String KEY_PREFIX = "tooltip.apoli.unusable.";

    private UnusableReason() {
    }

    /** Rewrites a count-only unusable line in these tooltip lines to name its powers. */
    public static void name(ItemStack stack, Player player, List<Component> lines) {
        if (player == null) {
            return;
        }
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).getContents() instanceof TranslatableContents contents
                    && contents.getKey().startsWith(KEY_PREFIX)
                    && countOnly(contents)) {
                Component reasons = reasons(stack, player);
                if (reasons != null) {
                    lines.set(i, Component.translatable(contents.getKey(), reasons).withStyle(ChatFormatting.GRAY));
                }
                return;
            }
        }
    }

    private static boolean countOnly(TranslatableContents contents) {
        Object[] args = contents.getArgs();
        return args.length == 1 && args[0] instanceof Component arg && arg.getString().chars().allMatch(Character::isDigit);
    }

    private static Component reasons(ItemStack stack, Player player) {
        Map<Identifier, PowerType<?>> owners = new LinkedHashMap<>();
        for (PreventItemUsePower power : PowerHolderComponent.getPowers(player, PreventItemUsePower.class)) {
            if (power.doesPrevent(stack)) {
                PowerType<?> owner = owner(power.getType());
                owners.putIfAbsent(owner.getIdentifier(), owner);
            }
        }
        MutableComponent joined = null;
        for (PowerType<?> owner : owners.values()) {
            MutableComponent name = owner.getName().withStyle(ChatFormatting.RED);
            joined = joined == null ? name : joined.append(Component.literal(", ").withStyle(ChatFormatting.GRAY)).append(name);
        }
        return joined;
    }

    // The visible multiple power a hidden sub-power belongs to, or the power itself.
    private static PowerType<?> owner(PowerType<?> type) {
        if (!type.isHidden()) {
            return type;
        }
        for (PowerType<?> candidate : PowerTypeRegistry.values()) {
            if (candidate instanceof MultiplePowerType<?> multiple && multiple.getSubPowers().contains(type.getIdentifier())) {
                return multiple;
            }
        }
        return type;
    }
}
