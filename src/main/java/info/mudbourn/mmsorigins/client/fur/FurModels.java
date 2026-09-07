package info.mudbourn.mmsorigins.client.fur;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import info.mudbourn.mmsorigins.MmsOrigins;
import info.mudbourn.mmsrendercommon.client.geo.BoneAnimator;
import info.mudbourn.mmsrendercommon.client.geo.GeoAnimation;
import info.mudbourn.mmsrendercommon.client.geo.GeoModelData;
import info.mudbourn.mmsrendercommon.client.geo.HumanoidGeoModel;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
     * <p>The overlay is a vanilla-skin-layout texture drawn translucently over the
     * player's own skin, adding the fur's body markings where it is painted and leaving
     * the skin showing where it is transparent; it is separate from the geo fins, with
     * {@code overlaySlim} its three-pixel-arm variant. Any of {@code model},
     * {@code texture}, {@code overlay} and {@code overlaySlim} may be null: an
     * overlay-only fur (truffle) has no geo, a geo-only fur (inchling) has no overlay,
     * and a fur with no slim variant (floran) reuses its wide overlay on slim models.
     *
     * <p>{@code offset} is a small translation in block units applied to the geo fins
     * before they are drawn, or null for none; {@code hidden} names the vanilla player
     * model parts the fur replaces, which the base body suppresses so the fur stands in
     * for them. {@code emissive} is a fullbright overlay drawn ignoring world light (the
     * glowing eyes of arachnid), and {@code elytra} retextures the worn elytra; both may
     * be null.
     */
    public record Fur(HumanoidGeoModel model,
                      Identifier texture,
                      Identifier overlay,
                      Identifier overlaySlim,
                      float[] offset,
                      Set<String> hidden,
                      Identifier emissive,
                      Identifier elytra) {}

    /**
     * Where a fur's geo and textures live before baking, plus how its bones move.
     *
     * <p>At most one of {@code animator} and {@code animation} is set: {@code animator}
     * is a procedural driver supplied in code, {@code animation} is the id of a Bedrock
     * {@code animation.json} loaded and parsed on first bake. {@code offset} may be null
     * and {@code hidden} is never null. Any other field may be null.
     */
    private record Source(Identifier geo,
                          BoneAnimator animator,
                          Identifier animation,
                          Identifier texture,
                          Identifier overlay,
                          Identifier overlaySlim,
                          float[] offset,
                          Set<String> hidden,
                          Identifier emissive,
                          Identifier elytra) {}

    private static final Map<Identifier, Source> SOURCES = new HashMap<>();
    private static final Map<Identifier, Fur> BAKED = new HashMap<>();

    static {
        register("origins", "merling",
                "mms_origins:fur/geo/merling.geo.json",
                null,
                "mms_origins:textures/fur/merling.png",
                "mms_origins:textures/fur/merling_skin.png",
                "mms_origins:textures/fur/merling_skin_thin.png");
        register("mms_origins", "floran",
                "mms_origins:fur/geo/floran.geo.json",
                null,
                "mms_origins:textures/fur/floran.png",
                "mms_origins:textures/fur/floran_skin.png",
                null);
        register("mms_origins", "inchling",
                "mms_origins:fur/geo/inchling.geo.json",
                null,
                "mms_origins:textures/fur/inchling.png",
                null,
                null);
        register("mms_origins", "piglin",
                "mms_origins:fur/geo/piglin.geo.json",
                new PiglinEarAnimator(),
                "mms_origins:textures/fur/piglin.png",
                null,
                null);
        register("mms_origins", "truffle",
                null,
                null,
                null,
                "mms_origins:textures/fur/truffle_skin.png",
                "mms_origins:textures/fur/truffle_skin_slim.png");
        register("origins", "feline",
                "mms_origins:fur/geo/feline.geo.json",
                null,
                "mms_origins:textures/fur/feline.png",
                null,
                null);
        register("origins", "enderian",
                "mms_origins:fur/geo/enderian.geo.json",
                null,
                "mms_origins:textures/fur/enderian.png",
                "mms_origins:textures/fur/enderian_skin.png",
                null);
        registerAnimated("origins", "avian",
                "mms_origins:fur/geo/avian.geo.json",
                "mms_origins:fur/animations/avian.animation.json",
                "mms_origins:textures/fur/avian.png",
                "mms_origins:textures/fur/avian_skin.png",
                null);
        registerAnimated("origins", "phantom",
                "mms_origins:fur/geo/phantom.geo.json",
                "mms_origins:fur/animations/phantom.animation.json",
                "mms_origins:textures/fur/phantom.png",
                "mms_origins:textures/fur/phantom_overlay.png",
                "mms_origins:textures/fur/phantom_overlay_slim.png");
        register("origins", "blazeborn",
                "mms_origins:fur/geo/blazeborn.geo.json",
                null,
                "mms_origins:textures/fur/blazeborn.png",
                "mms_origins:textures/fur/blazeborn_skin.png",
                null);
        register("origins", "shulk",
                "mms_origins:fur/geo/shulk.geo.json",
                null,
                "mms_origins:textures/fur/shulk.png",
                "mms_origins:textures/fur/shulk_skin.png",
                null);
        registerEmissive("origins", "arachnid",
                "mms_origins:textures/fur/arachnid.png",
                "mms_origins:textures/fur/arachnid_eyes.png");
        offset("origins", "blazeborn", 0.0F, 0.1F, 0.0F);
        hide("origins", "blazeborn", "leftArm", "rightArm", "leftSleeve", "rightSleeve");
        hide("origins", "shulk", "leftArm", "rightArm", "body");
        registerElytraOnly("origins", "elytrian",
                "mms_origins:textures/fur/elytrian_elytra.png");
        elytra("origins", "avian", "mms_origins:textures/fur/avian_elytra.png");
        elytra("origins", "phantom", "mms_origins:textures/fur/phantom_elytra.png");
        elytra("origins", "shulk", "mms_origins:textures/fur/shulk_elytra.png");
        aliasToOriginsTweaks("arachnid", "avian", "blazeborn", "elytrian",
                "enderian", "feline", "phantom", "shulk");
    }

    private FurModels() {
    }

    /**
     * Points the OriginsTweaks copies of the default origins at the same furs.
     * OriginsTweaks replaces the vanilla origins with {@code originstweaks:}
     * versions, so a player's origin id is now that namespace, not
     * {@code origins:}. Aliasing keeps the ported furs applying to both.
     */
    private static void aliasToOriginsTweaks(String... names) {
        for (String name : names) {
            Source source = SOURCES.get(Identifier.fromNamespaceAndPath("origins", name));
            if (source != null) {
                SOURCES.put(Identifier.fromNamespaceAndPath("originstweaks", name), source);
            }
        }
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
            model = load(source.geo(), animatorFor(source));
            if (model == null) {
                return null;
            }
        }
        Fur fur = new Fur(model, source.texture(), source.overlay(), source.overlaySlim(),
                source.offset(), source.hidden(), source.emissive(), source.elytra());
        BAKED.put(origin, fur);
        return fur;
    }

    /** The bone driver for a source: its procedural animator, else its parsed animation.json, else none. */
    private static BoneAnimator animatorFor(Source source) {
        if (source.animator() != null) {
            return source.animator();
        }
        if (source.animation() == null) {
            return null;
        }
        JsonObject json = readJson(source.animation());
        if (json == null) {
            MmsOrigins.LOGGER.warn("Missing fur animation {}", source.animation());
            return null;
        }
        try {
            return GeoAnimation.parse(json);
        } catch (Exception exception) {
            MmsOrigins.LOGGER.warn("Could not parse fur animation {}", source.animation(), exception);
            return null;
        }
    }

    private static HumanoidGeoModel load(Identifier geo, BoneAnimator animator) {
        JsonObject geoJson = readJson(geo);
        if (geoJson == null) {
            MmsOrigins.LOGGER.warn("Missing fur geometry {}", geo);
            return null;
        }
        try {
            return HumanoidGeoModel.bake(GeoModelData.parse(geoJson), animator);
        } catch (Exception exception) {
            MmsOrigins.LOGGER.warn("Could not bake fur geometry {}", geo, exception);
            return null;
        }
    }

    private static JsonObject readJson(Identifier id) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(id);
        if (resource.isEmpty()) {
            return null;
        }
        try (BufferedReader reader = resource.get().openAsReader()) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception exception) {
            MmsOrigins.LOGGER.warn("Could not read {}", id, exception);
            return null;
        }
    }

    private static void register(String namespace, String path, String geo, BoneAnimator animator,
                                 String texture, String overlay, String overlaySlim) {
        SOURCES.put(Identifier.fromNamespaceAndPath(namespace, path),
                new Source(id(geo), animator, null, id(texture), id(overlay), id(overlaySlim),
                        null, Set.of(), null, null));
    }

    private static void registerAnimated(String namespace, String path, String geo, String animation,
                                         String texture, String overlay, String overlaySlim) {
        SOURCES.put(Identifier.fromNamespaceAndPath(namespace, path),
                new Source(id(geo), null, id(animation), id(texture), id(overlay), id(overlaySlim),
                        null, Set.of(), null, null));
    }

    /** Registers an overlay-only fur that also paints a fullbright emissive overlay. */
    private static void registerEmissive(String namespace, String path, String overlay, String emissive) {
        SOURCES.put(Identifier.fromNamespaceAndPath(namespace, path),
                new Source(null, null, null, null, id(overlay), null,
                        null, Set.of(), id(emissive), null));
    }

    /** Registers a fur that only retextures the worn elytra, with no body geo or overlay. */
    private static void registerElytraOnly(String namespace, String path, String elytra) {
        SOURCES.put(Identifier.fromNamespaceAndPath(namespace, path),
                new Source(null, null, null, null, null, null,
                        null, Set.of(), null, id(elytra)));
    }

    /** Adds a block-unit fin offset to an already registered fur. */
    private static void offset(String namespace, String path, float x, float y, float z) {
        Identifier key = Identifier.fromNamespaceAndPath(namespace, path);
        Source s = SOURCES.get(key);
        SOURCES.put(key, new Source(s.geo(), s.animator(), s.animation(), s.texture(),
                s.overlay(), s.overlaySlim(), new float[] {x, y, z}, s.hidden(),
                s.emissive(), s.elytra()));
    }

    /** Marks the vanilla player parts an already registered fur replaces. */
    private static void hide(String namespace, String path, String... parts) {
        Identifier key = Identifier.fromNamespaceAndPath(namespace, path);
        Source s = SOURCES.get(key);
        SOURCES.put(key, new Source(s.geo(), s.animator(), s.animation(), s.texture(),
                s.overlay(), s.overlaySlim(), s.offset(), Set.of(parts),
                s.emissive(), s.elytra()));
    }

    /** Sets the retextured elytra of an already registered fur. */
    private static void elytra(String namespace, String path, String elytra) {
        Identifier key = Identifier.fromNamespaceAndPath(namespace, path);
        Source s = SOURCES.get(key);
        SOURCES.put(key, new Source(s.geo(), s.animator(), s.animation(), s.texture(),
                s.overlay(), s.overlaySlim(), s.offset(), s.hidden(),
                s.emissive(), id(elytra)));
    }

    private static Identifier id(String value) {
        return value == null ? null : Identifier.parse(value);
    }
}
