package info.mudbourn.mmsorigins.client.beastfolk;

import net.minecraft.resources.Identifier;

/** The tails a beastfolk can pick in the {@code originstweaks:beastfolk_tail} layer, each a coat for {@link TailModel} and its matching {@link EarsModel}. */
public enum BeastfolkTail {
    FOX("fox"),
    WOLF("wolf");

    private final Identifier option;
    private final Identifier texture;
    private final Identifier earTexture;

    BeastfolkTail(String name) {
        this.option = Identifier.fromNamespaceAndPath("originstweaks", "beastfolk_tail_" + name);
        this.texture = Identifier.fromNamespaceAndPath("mms_origins", "textures/entity/tail/" + name + ".png");
        this.earTexture = Identifier.fromNamespaceAndPath("mms_origins", "textures/entity/ears/" + name + ".png");
    }

    /** The tail an option origin selects, or null when it selects none. */
    public static BeastfolkTail of(Identifier option) {
        for (BeastfolkTail tail : values()) {
            if (tail.option.equals(option)) {
                return tail;
            }
        }
        return null;
    }

    public Identifier texture() {
        return this.texture;
    }

    public Identifier earTexture() {
        return this.earTexture;
    }
}
