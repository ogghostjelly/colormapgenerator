package ogghostjelly.colormapgenerator.colormap;

import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import ogghostjelly.colormapgenerator.ColorMapGenerator;
import ogghostjelly.colormapgenerator.utils.ColorUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * A mapping from map colors to a list of blocks that represent that color.
 */
public class MultiColormap {
    private final ArrayList<Block>[] map;
    private static final Logger LOGGER = ColorMapGenerator.LOGGER;

    public MultiColormap() {
        this.map = generateColorToBlockMap();
    }

    private static @NotNull ArrayList<Block>[] generateColorToBlockMap() {
        ArrayList<Block>[] map = new ArrayList[64];
        for (int i = 0; i < map.length; i++) {
            map[i] = new ArrayList<>();
        }

        for (Block block : Registries.BLOCK) {
            MapColor color = block.getDefaultMapColor();
            map[color.id].add(block);
        }

        for (MapColor color : ColorUtil.getMapColors()) {
            if (map[color.id].isEmpty()) {
                LOGGER.warn("Missing mapping from color `"+color+"` to block. The Colormap will be unable to produce this color!");
            }
        }

        if (ColorUtil.getMapColors().length != map.length) {
            LOGGER.error("colormap length is not equal to map colors length.");
        }

        return map;
    }

    public @NotNull Stream<Block> colorToBlocks(MapColor color) {
        return this.map[color.id].stream();
    }
}
