package ogghostjelly.colormapgenerator.utils;

import net.minecraft.block.*;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

// TODO: You might be able to use fluids as long as you place blocks around them.

public class ColorMap {
    public static class ColorMapItem implements Comparable<ColorMapItem> {
        public Block block;
        public MapColor color;

        public ColorMapItem(Block block, MapColor color) {
            this.block = block;
            this.color = color;
        }

        public double difference(@NotNull ColorMapItem o) {
            return ColorUtil.differenceARGB(this.color.color, o.color.color);
        }

        @Override
        public int compareTo(@NotNull ColorMap.ColorMapItem o) {
            return (int) Math.signum(this.difference(o));
        }
    }

    List<ColorMapItem> colorMap;

    private ColorMap(List<ColorMapItem> colorMap) {
        this.colorMap = colorMap;
    }

    public static ColorMap generateColorMap() {
        List<ColorMapItem> colorMap = Registries.BLOCK.stream()
                .map(block -> new ColorMapItem(block, block.getDefaultMapColor()))
                .filter(item -> item.color != MapColor.CLEAR)
                // Cactus breaks when placed near other blocks which is not helpful to us,
                // so we remove it from the map.
                .filter(item -> !(item.block instanceof CactusBlock))
                .filter(item -> isNotFluid(item.block.getDefaultState()))
                .filter(item -> isSolid(item.block.getDefaultState()))
                .distinct()
                .toList();

        return new ColorMap(colorMap);
    }

    public @Nullable Block colorToBlock(int color) {
        if (color == MapColor.CLEAR.color) {
            return null;
        }

        return this.colorMap.stream().min((a, b) -> {
                    double aDif = ColorUtil.differenceARGB(color, a.color.color);
                    double bDif = ColorUtil.differenceARGB(color, b.color.color);
                    return Double.compare(aDif, bDif);
                })
                .map(x -> x.block)
                .orElse(null);
    }

    private static boolean isNotFluid(@NotNull BlockState state) {
        return state.getFluidState().isEmpty();
    }
    private static boolean isSolid(@NotNull BlockState state) {
        return state.isOpaque();
    }

    public Stream<ColorMapItem> stream() {
        return colorMap.stream();
    }
}
