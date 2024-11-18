package ogghostjelly.colormapgenerator.utils.color;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class StaircasingColorMap {
    public static class ColorMapItem implements Comparable<ColorMapItem> {
        public final Block block;
        public final MapColor mapColor;
        public final MapColor.Brightness brightness;
        public final int color;

        public ColorMapItem(Block block, MapColor.Brightness brightness) {
            this.block = block;
            this.brightness = brightness;
            this.mapColor = block.getDefaultMapColor();
            // getRenderColor converts the ARGB mapColor to ABGR
            // we don't want that, so we swap the format back to ARGB
            this.color = ColorUtil.SwapFormat(this.mapColor.getRenderColor(brightness));
        }

        public double difference(@NotNull ColorMapItem o) {
            return ColorUtil.differenceColor(this.color, o.color);
        }

        @Override
        public int compareTo(@NotNull ColorMapItem o) {
            return (int) Math.signum(this.difference(o));
        }
    }

    final List<ColorMapItem> colorMap;

    private StaircasingColorMap(List<ColorMapItem> colorMap) {
        this.colorMap = colorMap;
    }

    public static StaircasingColorMap generateColorMap() {
        return generateColorMap(item -> {
            // Cactus breaks when placed near other blocks which is not helpful to us,
            // so we remove it from the map.
            return !(item.block instanceof CactusBlock) &&
                    isNotFluid(item.block.getDefaultState()) &&
                    isSolid(item.block.getDefaultState());
        });
    }

    public static StaircasingColorMap generateColorMap(Predicate<? super ColorMapItem> filter) {
        List<ColorMapItem> colorMap = Registries.BLOCK.stream()
                .flatMap(block -> Stream.of(
                        new ColorMapItem(block, MapColor.Brightness.LOW),
                        new ColorMapItem(block, MapColor.Brightness.NORMAL),
                        new ColorMapItem(block, MapColor.Brightness.HIGH)
                ))
                .filter(item -> item.mapColor != MapColor.CLEAR)
                .filter(filter)
                .distinct()
                .toList();

        return new StaircasingColorMap(colorMap);
    }

    public Optional<ColorMapItem> colorToBlock(int color) {
        if (ColorUtil.IsTransparent(color)) {
            return Optional.empty();
        }

        return this.colorMap.stream().min((a, b) -> {
                    double aDif = ColorUtil.differenceColor(color, a.color);
                    double bDif = ColorUtil.differenceColor(color, b.color);
                    return Double.compare(aDif, bDif);
                });
    }

    private static boolean isNotFluid(@NotNull BlockState state) {
        return state.getFluidState().isEmpty();
    }
    private static boolean isSolid(@NotNull BlockState state) {
        return state.isOpaque();
    }
}
