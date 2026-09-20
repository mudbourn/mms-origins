package info.mudbourn.mmsorigins.fur;

import info.mudbourn.mmscombat.combatlog.LogoutBodyEvents;
import net.minecraft.resources.Identifier;

// Dresses mms-combat's logout-body mannequin in the disconnecting player's fur and wings, resolved server-side and synced to viewers.
public final class LogoutBodyFurBridge {

    private LogoutBodyFurBridge() {
    }

    public static void register() {
        LogoutBodyEvents.CREATED.register((player, body) -> {
            Identifier fur = FurResolver.resolve(player);
            if (fur != null) {
                body.setAttached(BodyFurAttachment.TYPE, fur);
            }
            Identifier wing = FurResolver.wingOption(player);
            if (wing != null) {
                body.setAttached(BodyWingAttachment.TYPE, wing);
            }
        });
    }
}
