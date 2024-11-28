package ogghostjelly.colormapgenerator.utils;

import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import ogghostjelly.colormapgenerator.ColorMapGenerator;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * A map from colors to a list of blocks that represent that color.
 */
public class MultiColormap {
    private final ArrayList<Block>[] map;
    private static final Logger LOGGER = ColorMapGenerator.LOGGER;

    public MultiColormap() {
        this.map = generateColorToBlockMap();
    }

    private static ArrayList<Block>[] generateColorToBlockMap() {
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

        assert ColorUtil.getMapColors().length == map.length;

        return map;
    }

    public Stream<Block> colorToBlocks(MapColor color) {
        return this.map[color.id].stream();
    }
}
