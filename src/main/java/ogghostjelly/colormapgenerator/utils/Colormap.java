package ogghostjelly.colormapgenerator.utils;

import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import ogghostjelly.colormapgenerator.ColorMapGenerator;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

/**
 * A map from colors to a minecraft block.
 */
public class Colormap {
    private final Block[] map;
    private static final Logger LOGGER = ColorMapGenerator.LOGGER;

    public Colormap(Block[] map) {
        assert map.length == 64;
        this.map = map;
    }
    public Block colorToBlock(int color) {
        return this.colorToBlock(getClosestMapColorFromARGB(color));
    }

    public Block colorToBlock(MapColor color) {
        return this.map[color.id];
    }

    public void save(OutputStreamWriter writer) throws IOException {
        for (int i = 0; i < map.length; i++) {
            writer.write(i+" "+ Registries.BLOCK.getId(map[i]) + "\n");
        }
    }

    public static Colormap load(InputStreamReader reader) throws IOException {
        var map = new Block[64];
        var buf = new StringBuilder();
        char chr;

        int lim_i = 0;
        int lim = 100_000;

        while (true) {
            int value = reader.read();
            if (value == -1) {
                break;
            }
            chr = (char) value;

            if (chr == '\n') {
                String[] parts = buf.toString().split(" ");
                buf.setLength(0);
                if (parts.length != 2) {
                    LOGGER.error("invalid part count when loading colormap, skipping.");
                    continue;
                }

                int mapColorId;
                try {
                    mapColorId = Integer.parseInt(parts[0]);
                } catch (NumberFormatException exception) {
                    LOGGER.error("failed to parse map id when loading colormap: `"+exception+"`, skipping.");
                    continue;
                }

                Identifier blockId = Identifier.tryParse(parts[1]);
                if (blockId == null) {
                    LOGGER.error("failed to parse block id when loading colormap, skipping.");
                    continue;
                }

                map[mapColorId] = Registries.BLOCK.get(blockId);
            } else {
                buf.append(chr);
            }

            lim_i += 1;
            assert lim_i < lim;
        }

        return new Colormap(map);
    }

    public static MapColor getClosestMapColorFromARGB(int color) {
        Optional<MapColor> value = Arrays.stream(ColorUtil.getMapColors())
                .min((a, b) -> {
                    double aDif = ColorUtil.difference(a.color, color);
                    double bDif = ColorUtil.difference(b.color, color);
                    return Double.compare(aDif, bDif);
                });

        if (value.isEmpty()) {
            LOGGER.error("fatal error: getMapColors is empty!");
            return MapColor.CLEAR;
        }

        return value.get();
    }

    public static Colormap getDefaultColormap() {
        return new Colormap(new Block[64]);
    }

    public static Path getConfigPath() {
        return OgjUtil.getConfigDir().resolve("colormap");
    }

    public static Colormap loadFromConfig() throws IOException {
        Path path = Colormap.getConfigPath();
        try {
            return Colormap.load(new FileReader(path.toFile()));
        } catch (FileNotFoundException exception) {
            return Colormap.getDefaultColormap();
        }
    }

    public void saveToConfig() throws IOException {
        Path path = Colormap.getConfigPath();
        this.save(new FileWriter(path.toFile()));
    }

    @ApiStatus.Internal
    public void setMapping(MapColor color, Block block) {
        this.map[color.id] = block;
    }
}
