package ogghostjelly.colormapgenerator.utils.color;

import net.minecraft.block.*;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

// TODO: You might be able to use fluids as long as you place blocks around them.

public class ColorMap implements IColorMap {
    public record ColorMapItem(Block block, MapColor color) implements Comparable<ColorMapItem> {
        public double difference(@NotNull ColorMapItem o) {
                return ColorUtil.differenceColor(this.color.color, o.color.color);
            }

            @Override
            public int compareTo(@NotNull ColorMap.ColorMapItem o) {
                return (int) Math.signum(this.difference(o));
            }
        }

    final List<ColorMapItem> colorMap;

    private ColorMap(List<ColorMapItem> colorMap) {
        this.colorMap = colorMap;
    }

    public static ColorMap generateColorMap() {
        return generateColorMap(item -> {
            // Cactus breaks when placed near other blocks which is not helpful to us,
            // so we remove it from the map.
            return !(item.block instanceof CactusBlock) &&
                    isNotFluid(item.block.getDefaultState()) &&
                    isSolid(item.block.getDefaultState());
        });
    }

    public static ColorMap generateColorMap(Predicate<? super ColorMapItem> filter) {
        List<ColorMapItem> colorMap = Registries.BLOCK.stream()
                .map(block -> new ColorMapItem(block, block.getDefaultMapColor()))
                .filter(item -> item.color != MapColor.CLEAR)
                .filter(filter)
                .distinct()
                .toList();

        return new ColorMap(colorMap);
    }

    public @NotNull Block colorToBlock(int color) {
        if (ColorUtil.IsTransparent(color)) {
            return Blocks.AIR;
        }

        return this.colorMap.stream().min((a, b) -> {
                    double aDif = ColorUtil.differenceColor(color, a.color.color);
                    double bDif = ColorUtil.differenceColor(color, b.color.color);
                    return Double.compare(aDif, bDif);
                })
                .map(x -> x.block)
                .orElse(Blocks.AIR);
    }

    private static boolean isNotFluid(@NotNull BlockState state) {
        return state.getFluidState().isEmpty();
    }
    private static boolean isSolid(@NotNull BlockState state) {
        return state.isOpaque();
    }
}
