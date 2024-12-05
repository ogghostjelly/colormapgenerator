package ogghostjelly.colormapgenerator.config;

import com.google.common.collect.Lists;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import me.shedaniel.clothconfig2.gui.widget.ColorDisplayWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ColorMappingEntry extends TooltipListEntry<Optional<Block>> {
    protected ColorMappingEntry.Slider sliderWidget;
    protected ButtonWidget resetButton;
    protected ColorDisplayWidget colorDisplayWidget;
    protected AtomicInteger value;
    protected final long original;
    private int minimum;
    private int maximum;
    private final Supplier<Optional<Block>> defaultValue;
    private Function<Integer, Text> textGetter;
    private final List<ClickableWidget> widgets;
    private final Optional<Block>[] blocks;

    /** @deprecated */
    @Deprecated
    @ApiStatus.Internal
    public ColorMappingEntry(Text fieldName, Block[] blocks, int value, MapColor color, Text resetButtonKey, Supplier<Optional<Block>> defaultValue, Consumer<Optional<Block>> saveConsumer) {
        this(fieldName, blocks, value, color, resetButtonKey, defaultValue, saveConsumer, (Supplier)null);
    }

    /** @deprecated */
    @Deprecated
    @ApiStatus.Internal
    public ColorMappingEntry(Text fieldName, Block[] blocks, int value, MapColor color, Text resetButtonKey, Supplier<Optional<Block>> defaultValue, Consumer<Optional<Block>> saveConsumer, Supplier<Optional<Text[]>> tooltipSupplier) {
        this(fieldName, blocks, value, color, resetButtonKey, defaultValue, saveConsumer, tooltipSupplier, false);
    }

    /** @deprecated */
    @Deprecated
    @ApiStatus.Internal
    public ColorMappingEntry(Text fieldName, Block[] blocks, int value, MapColor color, Text resetButtonKey, Supplier<Optional<Block>> defaultValue, Consumer<Optional<Block>> saveConsumer, Supplier<Optional<Text[]>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, tooltipSupplier, requiresRestart);
        // All indicies get shifted over by one because we add `Optional.empty` at the beginning of `blocks`.
        // so we need to shift `value` over too.
        value += 1;
        ArrayList<Optional<Block>> blockList = new ArrayList<>();
        blockList.add(Optional.empty());
        for (Block x : blocks) {
            blockList.add(Optional.ofNullable(x));
        }
        this.blocks = blockList.toArray(new Optional[0]);
        this.textGetter = (integer) -> {
            if (this.blocks[integer].isEmpty()) {
                return Text.translatable("gui.none");
            }
            return Text.literal(String.format("%d: ", integer)).append(this.blocks[integer].get().getName());
        };
        this.original = (long)value;
        this.defaultValue = defaultValue;
        this.value = new AtomicInteger(value);
        this.saveCallback = saveConsumer;
        this.maximum = this.blocks.length - 1 + 1;
        this.minimum = 0;
        var colorDisplayTextWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, this.getFieldName());
        this.colorDisplayWidget = new ColorDisplayWidget(colorDisplayTextWidget, 0, 0, 20, color.color | 0xff000000);
        this.sliderWidget = new ColorMappingEntry.Slider(0, 0, 152, 20, ((double)this.value.get() - (double)minimum) / (double)Math.abs(maximum - minimum));
        this.resetButton = ButtonWidget.builder(resetButtonKey, (widget) -> {
            this.setValue(this.indexOf(defaultValue.get()));
        }).dimensions(0, 0, MinecraftClient.getInstance().textRenderer.getWidth(resetButtonKey) + 6, 20).build();
        this.sliderWidget.setMessage((Text)this.textGetter.apply(this.value.get()));
        this.widgets = Lists.newArrayList(new ClickableWidget[]{this.colorDisplayWidget, this.sliderWidget, this.resetButton});
    }

    public int indexOf(Optional<Block> value) {
        return Arrays.stream(this.blocks).toList().indexOf(value);
    }

    public Function<Integer, Text> getTextGetter() {
        return this.textGetter;
    }

    public ColorMappingEntry setTextGetter(Function<Integer, Text> textGetter) {
        this.textGetter = textGetter;
        this.sliderWidget.setMessage((Text)textGetter.apply(this.value.get()));
        return this;
    }

    public Optional<Block> getValue() {
        return this.blocks[this.value.get()];
    }

    public Integer getValueIndex() {
        return this.value.get();
    }

    /** @deprecated */
    @Deprecated
    public void setValue(int value) {
        this.sliderWidget.setValue((double)(MathHelper.clamp(value, this.minimum, this.maximum) - this.minimum) / (double)Math.abs(this.maximum - this.minimum));
        this.value.set(Math.min(Math.max(value, this.minimum), this.maximum));
        this.sliderWidget.updateMessage();
    }

    public boolean isEdited() {
        return super.isEdited() || (long)this.getValueIndex() != this.original;
    }

    public Optional<Optional<Block>> getDefaultValue() {
        return this.defaultValue == null ? Optional.empty() : Optional.of(this.defaultValue.get());
    }

    public List<? extends Element> children() {
        return this.widgets;
    }

    public List<? extends Selectable> narratables() {
        return this.widgets;
    }

    public ColorMappingEntry setMaximum(int maximum) {
        this.maximum = maximum;
        return this;
    }

    public ColorMappingEntry setMinimum(int minimum) {
        this.minimum = minimum;
        return this;
    }

    public void render(DrawContext graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.render(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);
        this.resetButton.active = this.isEditable() && this.getDefaultValue().isPresent() && this.indexOf(this.defaultValue.get()) != this.value.get();
        this.resetButton.setY(y);
        this.sliderWidget.active = this.isEditable();
        this.sliderWidget.setY(y);
        if (MinecraftClient.getInstance().textRenderer.isRightToLeft()) {
            this.resetButton.setX(x);
            this.sliderWidget.setX(x + this.resetButton.getWidth() + 1);
        } else {
            this.resetButton.setX(x + entryWidth - this.resetButton.getWidth());
            this.sliderWidget.setX(x + entryWidth - 200);
        }

        this.colorDisplayWidget.setX(x);
        this.colorDisplayWidget.setY(y);
        this.colorDisplayWidget.render(graphics, mouseX, mouseY, delta);

        Optional<Block> maybeBlock = this.getValue();
        Block block = maybeBlock.orElse(Blocks.BARRIER);
        graphics.drawItem(new ItemStack(block), 20 + 2 + x, y + 2);

        this.sliderWidget.setWidth(200 - this.resetButton.getWidth() - 2);
        this.resetButton.render(graphics, mouseX, mouseY, delta);
        this.sliderWidget.render(graphics, mouseX, mouseY, delta);
    }

    private class Slider extends SliderWidget {
        protected Slider(int int_1, int int_2, int int_3, int int_4, double double_1) {
            super(int_1, int_2, int_3, int_4, Text.empty(), double_1);
        }

        public void updateMessage() {
            this.setMessage((Text)ColorMappingEntry.this.textGetter.apply(ColorMappingEntry.this.value.get()));
        }

        protected void applyValue() {
            ColorMappingEntry.this.value.set((int)((double)ColorMappingEntry.this.minimum + (double)Math.abs(ColorMappingEntry.this.maximum - ColorMappingEntry.this.minimum) * this.value));
        }

        public boolean keyPressed(int int_1, int int_2, int int_3) {
            return ColorMappingEntry.this.isEditable() && super.keyPressed(int_1, int_2, int_3);
        }

        public boolean mouseDragged(double double_1, double double_2, int int_1, double double_3, double double_4) {
            return ColorMappingEntry.this.isEditable() && super.mouseDragged(double_1, double_2, int_1, double_3, double_4);
        }

        public double getProgress() {
            return this.value;
        }

        public void setProgress(double integer) {
            this.value = integer;
        }

        public void setValue(double integer) {
            this.value = integer;
        }
    }
}
