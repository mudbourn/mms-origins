package info.mudbourn.mmsorigins.fur;

import info.mudbourn.mmsorigins.MmsOrigins;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

// A synced attachment carrying the fur a logout-body mannequin should wear, resolved server-side and read by the client renderer.
public final class BodyFurAttachment {

    public static final AttachmentType<Identifier> TYPE = AttachmentRegistry.<Identifier>builder()
        .syncWith(Identifier.STREAM_CODEC, AttachmentSyncPredicate.all())
        .buildAndRegister(Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "body_fur"));

    private BodyFurAttachment() {
    }

    // Forces the attachment type to register during mod init.
    public static void touch() {
    }
}
