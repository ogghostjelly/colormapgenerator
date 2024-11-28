package ogghostjelly.colormapgenerator.config;

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ColorMappingMenuBuilder extends AbstractFieldBuilder<Block, ColorMappingEntry, ColorMappingMenuBuilder> {
    private final Block[] valuesArray;
    private Function<Block, Text> nameProvider = null;
    private final MapColor color;

    public ColorMappingMenuBuilder(Text resetButtonKey, Text fieldNameKey, Block[] valuesArray, Block value, MapColor color) {
        super(resetButtonKey, fieldNameKey);
        Objects.requireNonNull(value);
        this.valuesArray = valuesArray;
        this.value = value;
        this.color = color;
    }

    public static ColorMappingMenuBuilder start(ConfigEntryBuilder entryBuilder, Text fieldNameKey, Block[] valuesArray, Block value, MapColor color) {
        return new ColorMappingMenuBuilder(entryBuilder.getResetButtonKey(), fieldNameKey, valuesArray, value, color);
    }

    public ColorMappingMenuBuilder setErrorSupplier(Function<Block, Optional<Text>> errorSupplier) {
        return (ColorMappingMenuBuilder)super.setErrorSupplier(errorSupplier);
    }

    public ColorMappingMenuBuilder requireRestart() {
        return (ColorMappingMenuBuilder)super.requireRestart();
    }

    public ColorMappingMenuBuilder setSaveConsumer(Consumer<Block> saveConsumer) {
        return (ColorMappingMenuBuilder)super.setSaveConsumer(saveConsumer);
    }

    public ColorMappingMenuBuilder setDefaultValue(Supplier<Block> defaultValue) {
        return (ColorMappingMenuBuilder)super.setDefaultValue(defaultValue);
    }

    public ColorMappingMenuBuilder setDefaultValue(Block defaultValue) {
        return (ColorMappingMenuBuilder)super.setDefaultValue(defaultValue);
    }

    public ColorMappingMenuBuilder setTooltipSupplier(Function<Block, Optional<Text[]>> tooltipSupplier) {
        return (ColorMappingMenuBuilder)super.setTooltipSupplier(tooltipSupplier);
    }

    public ColorMappingMenuBuilder setTooltipSupplier(Supplier<Optional<Text[]>> tooltipSupplier) {
        return (ColorMappingMenuBuilder)super.setTooltipSupplier(tooltipSupplier);
    }

    public ColorMappingMenuBuilder setTooltip(Optional<Text[]> tooltip) {
        return (ColorMappingMenuBuilder)super.setTooltip(tooltip);
    }

    public ColorMappingMenuBuilder setTooltip(Text... tooltip) {
        return (ColorMappingMenuBuilder)super.setTooltip(tooltip);
    }

    public ColorMappingMenuBuilder setNameProvider(Function<Block, Text> enumNameProvider) {
        this.nameProvider = enumNameProvider;
        return this;
    }

    public @NotNull ColorMappingEntry build() {
        ColorMappingEntry entry = new ColorMappingEntry(this.getFieldNameKey(), this.valuesArray, Arrays.stream(this.valuesArray).toList().indexOf(this.value), this.color, this.getResetButtonKey(), this.defaultValue, this.getSaveConsumer(), (Supplier)null, this.isRequireRestart());
        entry.setTooltipSupplier(() -> {
            return this.getTooltipSupplier().apply(entry.getValue());
        });
        if (this.errorSupplier != null) {
            entry.setErrorSupplier(() -> {
                return this.errorSupplier.apply(entry.getValue());
            });
        }

        return (ColorMappingEntry)this.finishBuilding(entry);
    }
}
