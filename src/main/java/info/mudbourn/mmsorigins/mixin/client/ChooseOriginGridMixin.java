package info.mudbourn.mmsorigins.mixin.client;

import info.mudbourn.mmsorigins.MmsOrigins;
import io.github.apace100.origins.origin.Impact;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.screen.ChooseOriginScreen;
import io.github.apace100.origins.screen.OriginDisplayScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Replaces the origin selection screen's one-at-a-time arrow browser with a grid picker.
 *
 * <p>This is a reimplementation of UltrusBot's Alternate Origin GUI (MIT) against the
 * refactored Origins Legacy screen.  The upstream mod's own mixin cannot apply here:
 * Legacy dropped {@code OriginWindowWidget} and now draws the window directly from
 * {@code guiLeft}/{@code guiTop}, and it holds the choosable origins as an
 * {@code originSelection} list plus a {@code currentOrigin} index rather than the fields
 * the upstream mixin shadows.</p>
 *
 * <p>At the end of {@code init} the origin window is shifted right to clear room for a
 * choices box on the left, the vanilla prev/next arrows are removed, the select button is
 * moved under the shifted window, and one invisible button per grid slot is added.  The
 * box itself and its icons render last, on top of the invisible buttons, so a click on a
 * slot lands on its button while the art stays visible.</p>
 *
 * <p>Client-only.</p>
 */
@Mixin(ChooseOriginScreen.class)
public abstract class ChooseOriginGridMixin extends OriginDisplayScreen {

    @Shadow
    private int currentOrigin;
    @Shadow
    private int maxSelection;
    @Shadow
    @Final
    private List<Origin> originSelection;
    @Shadow
    private Origin randomOrigin;
    @Shadow
    @Final
    private ArrayList<OriginLayer> layerList;
    @Shadow
    private int currentLayerIndex;

    @Shadow
    protected abstract Origin getCurrentOriginInternal();

    @Unique
    private static final Identifier ORIGIN_CHOICES =
            Identifier.fromNamespaceAndPath(MmsOrigins.MOD_ID, "textures/gui/origin_choices.png");
    @Unique
    private static final int CHOICES_WIDTH = 219;
    @Unique
    private static final int CHOICES_HEIGHT = 182;
    @Unique
    private static final int ICON_SIZE = 26;
    @Unique
    private static final int COUNT_PER_PAGE = 35;
    @Unique
    private static final int COLUMNS = 7;

    @Unique
    private int calculatedLeft;
    @Unique
    private int calculatedTop;
    @Unique
    private int currentPage;
    @Unique
    private int pages;
    @Unique
    private float tickTime;

    protected ChooseOriginGridMixin(Component title, boolean showDirtBackground) {
        super(title, showDirtBackground);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void mmsOrigins$buildGrid(CallbackInfo ci) {
        this.calculatedTop = this.guiTop;
        this.calculatedLeft = (this.width - (CHOICES_WIDTH + 10 + windowWidth)) / 2;
        this.guiLeft = this.calculatedLeft + CHOICES_WIDTH + 10;
        this.pages = (int) Math.ceil((float) this.maxSelection / COUNT_PER_PAGE);

        mmsOrigins$restyleVanillaButtons();

        for (int i = 0; i < Math.min(this.maxSelection, COUNT_PER_PAGE); i++) {
            int col = i % COLUMNS;
            int row = i / COLUMNS;
            int x = this.calculatedLeft + 12 + col * (ICON_SIZE + 2);
            int y = this.calculatedTop + 10 + row * (ICON_SIZE + 4);
            int slot = i;
            addRenderableWidget(Button.builder(Component.empty(), b -> mmsOrigins$select(slot))
                    .pos(x, y)
                    .size(ICON_SIZE, ICON_SIZE)
                    .build());
        }

        if (this.maxSelection > COUNT_PER_PAGE) {
            int arrowY = this.guiTop + CHOICES_HEIGHT + 5;
            addRenderableWidget(Button.builder(Component.literal("<"), b -> mmsOrigins$turnPage(-1))
                    .pos(this.calculatedLeft, arrowY)
                    .size(20, 20)
                    .build());
            addRenderableWidget(Button.builder(Component.literal(">"), b -> mmsOrigins$turnPage(1))
                    .pos(this.calculatedLeft + CHOICES_WIDTH - 20, arrowY)
                    .size(20, 20)
                    .build());
        }

        addRenderableOnly((GuiGraphics context, int mouseX, int mouseY, float delta) -> {
            mmsOrigins$renderChoicesBox(context, mouseX, mouseY);
            this.tickTime += delta;
        });
    }

    /**
     * Removes the vanilla prev/next arrows and moves the select button under the shifted window.
     */
    @Unique
    private void mmsOrigins$restyleVanillaButtons() {
        List<GuiEventListener> arrows = new ArrayList<>();
        for (GuiEventListener child : children()) {
            if (!(child instanceof Button button)) {
                continue;
            }
            String label = button.getMessage().getString();
            if (label.equals("<") || label.equals(">")) {
                arrows.add(button);
            } else {
                button.setX(this.guiLeft + 88 - 50);
                button.setY(this.guiTop + CHOICES_HEIGHT + 5);
            }
        }
        arrows.forEach(this::removeWidget);
    }

    @Unique
    private void mmsOrigins$select(int slot) {
        int index = slot + this.currentPage * COUNT_PER_PAGE;
        if (index > this.maxSelection - 1) {
            return;
        }
        this.currentOrigin = index;
        Origin origin = this.getCurrentOriginInternal();
        this.showOrigin(origin, this.layerList.get(this.currentLayerIndex), origin == this.randomOrigin);
    }

    @Unique
    private void mmsOrigins$turnPage(int direction) {
        this.currentPage = (this.currentPage + direction + this.pages) % this.pages;
    }

    @Unique
    private void mmsOrigins$renderChoicesBox(GuiGraphics context, int mouseX, int mouseY) {
        context.blit(RenderPipelines.GUI_TEXTURED, ORIGIN_CHOICES,
                this.calculatedLeft, this.calculatedTop, 0.0F, 0.0F,
                CHOICES_WIDTH, CHOICES_HEIGHT, 256, 256);

        int from = this.currentPage * COUNT_PER_PAGE;
        int to = Math.min((this.currentPage + 1) * COUNT_PER_PAGE, this.maxSelection);
        for (int i = from; i < to; i++) {
            int local = i - from;
            int col = local % COLUMNS;
            int row = local / COLUMNS;
            int x = this.calculatedLeft + 12 + col * (ICON_SIZE + 2);
            int y = this.calculatedTop + 10 + row * (ICON_SIZE + 4);
            boolean selected = i == this.currentOrigin;
            if (i >= this.originSelection.size()) {
                mmsOrigins$renderRandomSlot(context, mouseX, mouseY, x, y, selected);
            } else {
                Origin origin = this.originSelection.get(i);
                mmsOrigins$renderOriginSlot(context, mouseX, mouseY, x, y, selected, origin);
                context.renderItem(origin.getDisplayItem(), x + 5, y + 5);
            }
        }

        Component counter = Component.literal((this.currentPage + 1) + "/" + this.pages);
        context.drawCenteredString(this.font, counter,
                this.calculatedLeft + CHOICES_WIDTH / 2,
                this.guiTop + CHOICES_HEIGHT + 5 + this.font.lineHeight / 2, 0xFFFFFF);
    }

    @Unique
    private void mmsOrigins$renderOriginSlot(GuiGraphics context, int mouseX, int mouseY,
                                             int x, int y, boolean selected, Origin origin) {
        boolean hovered = mmsOrigins$slotHovered(mouseX, mouseY, x, y);
        mmsOrigins$renderSlotFrame(context, x, y, selected, hovered);

        Impact impact = origin.getImpact();
        int impactU = 224 + impact.getImpactValue() * 8;
        int impactV = hovered ? 112 : 104;
        context.blit(RenderPipelines.GUI_TEXTURED, ORIGIN_CHOICES, x, y, impactU, impactV, 8, 8, 256, 256);

        if (hovered) {
            Component tooltip = Component.translatable(this.getCurrentLayer().getTranslationKey())
                    .append(": ")
                    .append(origin.getName());
            context.setTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
        }
    }

    @Unique
    private void mmsOrigins$renderRandomSlot(GuiGraphics context, int mouseX, int mouseY,
                                             int x, int y, boolean selected) {
        boolean hovered = mmsOrigins$slotHovered(mouseX, mouseY, x, y);
        mmsOrigins$renderSlotFrame(context, x, y, selected, hovered);
        context.blit(RenderPipelines.GUI_TEXTURED, ORIGIN_CHOICES, x + 6, y + 5, 243.0F, 120.0F, 13, 16, 256, 256);
        int phase = (int) (this.tickTime / 15.0F) % 4;
        context.blit(RenderPipelines.GUI_TEXTURED, ORIGIN_CHOICES, x, y,
                224 + phase * 8, hovered ? 112 : 104, 8, 8, 256, 256);
    }

    /**
     * Draws the 26x26 slot border, highlighted when selected or picked out by mouse or keyboard focus.
     */
    @Unique
    private void mmsOrigins$renderSlotFrame(GuiGraphics context, int x, int y, boolean selected, boolean hovered) {
        int v = selected ? 26 : 0;
        boolean focused = this.getFocused() instanceof Button button && button.getX() == x && button.getY() == y;
        if (focused || hovered) {
            v += 52;
        }
        context.blit(RenderPipelines.GUI_TEXTURED, ORIGIN_CHOICES, x, y, 230.0F, (float) v, ICON_SIZE, ICON_SIZE, 256, 256);
    }

    @Unique
    private boolean mmsOrigins$slotHovered(int mouseX, int mouseY, int x, int y) {
        return mouseX >= x && mouseY >= y && mouseX < x + ICON_SIZE && mouseY < y + ICON_SIZE;
    }
}
