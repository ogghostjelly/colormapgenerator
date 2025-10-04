package ogghostjelly.colormapgenerator.blockmap;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import ogghostjelly.colormapgenerator.ColorMapGenerator;
import ogghostjelly.colormapgenerator.utils.ColorUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A mapping from map colors to a minecraft block. Can have null mappings, if a mapping is null it will "fallthrough" and find the next closest color.
 */
@Deprecated
public class FallthroughBlockmap implements IBlockmap {
    private final Block[] map;
    private static final Logger LOGGER = ColorMapGenerator.LOGGER;

    public FallthroughBlockmap(Block[] map) {
        if (map.length != 64) {
            LOGGER.error("`map` should be of length 64 but it is not!");
        }

        if (Arrays.stream(map).allMatch(Objects::isNull)) {
            LOGGER.warn("Empty colormap is disallowed! There should always be at least one mapping.");
        }

        for (int i = 0; i < map.length; i++) {
            if (map[i] == null) {
                map[i] = fallThroughColorToBlock(map, MapColor.get(i));
            }
        }

        this.map = map;
    }

    /**
     * When a mapping is set to None, this will be used to find the next closest match.
     */
    private @NotNull Block fallThroughColorToBlock(Block[] map, MapColor color) {
        for (MapColor x : getSimilarColors(color).toList()) {
            if (map[x.id] != null) {
                return map[x.id];
            }
        }

        LOGGER.error("fatal error: couldn't find block for color `"+color.id+"`, there should always be at least one available map color since MapColor.CLEAR is always set to Blocks.AIR.");
        return Blocks.AIR;
    }

    public @NotNull Block map(MapColor color, MapColor.Brightness brightness) {
        return this.map[color.id];
    }

    public static Stream<MapColor> getSimilarColors(MapColor color) {
        return Arrays.stream(ColorUtil.getMapColors())
                .filter(x -> x.id != color.id)
                .sorted((a, b) -> ColorUtil.compare(color.color, a.color, b.color));
    }
}
