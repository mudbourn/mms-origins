package info.mudbourn.mmsorigins.client.fur;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import info.mudbourn.mmsorigins.MmsOrigins;
import info.mudbourn.mmsrendercommon.client.geo.GeoModelData;
import info.mudbourn.mmsrendercommon.client.geo.HumanoidGeoModel;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The table of ported furs, keyed by the origin they belong to.
 *
 * <p>One row per origin. Each row names the Bedrock geo and the texture; both are
 * loaded and baked lazily on first draw and cached, since baking touches the resource
 * manager and so cannot run before the client is up. A resource-reload does not yet
 * invalidate the cache, which is fine while the set is fixed and shipped in the jar.
 */
public final class FurModels {

    /**
     * A resolved fur: its baked geometry, the geo texture, and the body overlay.
     *
     * <p>The overlay is a vanilla-skin-layout texture that replaces the player's body
     * skin on the render state, turning the vanilla model into the fur body; it is
     * separate from the geo fins, with {@code overlaySlim} its three-pixel-arm variant.
     * Any of {@code model},
     * {@code texture}, {@code overlay} and {@code overlaySlim} may be null: an
     * overlay-only fur (truffle) has no geo, a geo-only fur (inchling) has no overlay,
     * and a fur with no slim variant (floran) reuses its wide overlay on slim models.
     */
    public record Fur(HumanoidGeoModel model,
                      Identifier texture,
                      Identifier overlay,
                      Identifier overlaySlim) {}

    /** Where a fur's geo and textures live, before baking. Any field may be null. */
    private record Source(Identifier geo,
                          Identifier texture,
                          Identifier overlay,
                          Identifier overlaySlim) {}

    private static final Map<Identifier, Source> SOURCES = new HashMap<>();
    private static final Map<Identifier, Fur> BAKED = new HashMap<>();

    static {
        register("origins", "merling",
                "mms_origins:fur/geo/merling.geo.json",
                "mms_origins:textures/fur/merling.png",
                "mms_origins:textures/fur/merling_skin.png",
                "mms_origins:textures/fur/merling_skin_thin.png");
        register("mms_origins", "floran",
                "mms_origins:fur/geo/floran.geo.json",
                "mms_origins:textures/fur/floran.png",
                "mms_origins:textures/fur/floran_skin.png",
                null);
        register("mms_origins", "inchling",
                "mms_origins:fur/geo/inchling.geo.json",
                "mms_origins:textures/fur/inchling.png",
                null,
                null);
        // Baked static; the source animation file is not applied.
        register("mms_origins", "piglin",
                "mms_origins:fur/geo/piglin.geo.json",
                "mms_origins:textures/fur/piglin.png",
                null,
                null);
        register("mms_origins", "truffle",
                null,
                null,
                "mms_origins:textures/fur/truffle_skin.png",
                "mms_origins:textures/fur/truffle_skin_slim.png");
    }

    private FurModels() {
    }

    /** Touches the class so its table is populated; the models bake on first draw. */
    public static void init() {
    }

    /** The fur for an origin, baking it on first request, or null if none is registered. */
    public static Fur resolve(Identifier origin) {
        if (origin == null || !SOURCES.containsKey(origin)) {
            return null;
        }
        Fur baked = BAKED.get(origin);
        if (baked != null) {
            return baked;
        }
        Source source = SOURCES.get(origin);
        HumanoidGeoModel model = null;
        if (source.geo() != null) {
            model = load(source.geo());
            if (model == null) {
                return null;
            }
        }
        Fur fur = new Fur(model, source.texture(), source.overlay(), source.overlaySlim());
        BAKED.put(origin, fur);
        return fur;
    }

    private static HumanoidGeoModel load(Identifier geo) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(geo);
        if (resource.isEmpty()) {
            MmsOrigins.LOGGER.warn("Missing fur geometry {}", geo);
            return null;
        }
        try (BufferedReader reader = resource.get().openAsReader()) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            return HumanoidGeoModel.bake(GeoModelData.parse(json));
        } catch (Exception exception) {
            MmsOrigins.LOGGER.warn("Could not read fur geometry {}", geo, exception);
            return null;
        }
    }

    private static void register(String namespace, String path,
                                 String geo, String texture, String overlay, String overlaySlim) {
        SOURCES.put(Identifier.fromNamespaceAndPath(namespace, path),
                new Source(id(geo), id(texture), id(overlay), id(overlaySlim)));
    }

    private static Identifier id(String value) {
        return value == null ? null : Identifier.parse(value);
    }
}
