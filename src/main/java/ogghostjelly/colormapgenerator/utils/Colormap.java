package ogghostjelly.colormapgenerator.utils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import ogghostjelly.colormapgenerator.ColorMapGenerator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * A mapping from map colors to a minecraft block.
 */
public class Colormap {
    private final Block[] map;
    private static final Logger LOGGER = ColorMapGenerator.LOGGER;

    public Colormap(Block[] map) {
        if (map.length != 64) {
            LOGGER.error("`map` should be of length 64 but it is not!");
        }

        for (int i = 0; i < map.length; i++) {
            if (map[i] == null) {
                LOGGER.error("null is disallowed in colormap! replacing with " + Blocks.AIR);
                map[i] = Blocks.AIR;
            }
        }
        this.map = map;
    }

    @Deprecated
    public @NotNull Block colorToBlock(int color) {
        if (ColorUtil.IsTransparent(color)) {
            return Blocks.AIR;
        }
        return this.getNearestColorToBlock(getClosestMapColorFromARGB(color));
    }

    @Deprecated
    public @NotNull Block getNearestColorToBlock(MapColor color) {
        var bl = this.colorToBlock(color);
        if (bl != Blocks.AIR) {
            return bl;
        }

        List<MapColor> list = Arrays.stream(ColorUtil.getMapColors())
                .sorted((a, b) -> {
                    double bDif = ColorUtil.difference(b.color, color.color);
                    double aDif = ColorUtil.difference(a.color, color.color);
                    return Double.compare(aDif, bDif);
                })
                .toList();

        for (MapColor value : list) {
            if (value == color) {
                continue;
            }

            var block = this.colorToBlock(value);
            if (block != Blocks.AIR) {
                return this.colorToBlock(color);
            }
        }

        LOGGER.error("Couldn't find fall-through color mapping for color "+color.id);
        return Blocks.AIR;
    }

    public @NotNull Block colorToBlock(MapColor color) {
        return this.map[color.id];
    }

    public void save(FileWriter writer) throws IOException {
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

            if (lim_i >= lim) {
                LOGGER.warn("loading colormap is taking unusually long...");
            }
        }

        for (int i = 0; i < map.length; i++) {
            if (map[i] == null) {
                LOGGER.warn("missing mapping for color " + i);
                map[i] = getDefaultColormap().colorToBlock(MapColor.get(i));
            }
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
        var map = new Block[64];

        map[MapColor.CLEAR.id] = Blocks.AIR;
        map[MapColor.PALE_GREEN.id] = Blocks.GRASS_BLOCK;
        map[MapColor.PALE_YELLOW.id] = Blocks.BIRCH_PLANKS;
        map[MapColor.WHITE_GRAY.id] = Blocks.MUSHROOM_STEM;
        map[MapColor.BRIGHT_RED.id] = Blocks.REDSTONE_BLOCK;
        map[MapColor.PALE_PURPLE.id] = Blocks.ICE;
        map[MapColor.IRON_GRAY.id] = Blocks.IRON_BLOCK;
        map[MapColor.DARK_GREEN.id] = Blocks.OAK_LEAVES;
        map[MapColor.WHITE.id] = Blocks.SNOW_BLOCK;
        map[MapColor.LIGHT_BLUE_GRAY.id] = Blocks.CLAY;
        map[MapColor.DIRT_BROWN.id] = Blocks.PACKED_MUD;
        map[MapColor.STONE_GRAY.id] = Blocks.COBBLESTONE;
        map[MapColor.WATER_BLUE.id] = Blocks.AIR;
        map[MapColor.OAK_TAN.id] = Blocks.OAK_PLANKS;
        map[MapColor.OFF_WHITE.id] = Blocks.QUARTZ_BLOCK;
        map[MapColor.ORANGE.id] = Blocks.ORANGE_CONCRETE;
        map[MapColor.MAGENTA.id] = Blocks.MAGENTA_CONCRETE;
        map[MapColor.LIGHT_BLUE.id] = Blocks.LIGHT_BLUE_CONCRETE;
        map[MapColor.YELLOW.id] = Blocks.YELLOW_CONCRETE;
        map[MapColor.LIME.id] = Blocks.LIME_CONCRETE;
        map[MapColor.PINK.id] = Blocks.PINK_CONCRETE;
        map[MapColor.GRAY.id] = Blocks.GRAY_CONCRETE;
        map[MapColor.LIGHT_GRAY.id] = Blocks.LIGHT_GRAY_CONCRETE;
        map[MapColor.CYAN.id] = Blocks.CYAN_CONCRETE;
        map[MapColor.PURPLE.id] = Blocks.PURPLE_CONCRETE;
        map[MapColor.BLUE.id] = Blocks.BLUE_CONCRETE;
        map[MapColor.BROWN.id] = Blocks.BROWN_CONCRETE;
        map[MapColor.GREEN.id] = Blocks.GREEN_CONCRETE;
        map[MapColor.RED.id] = Blocks.RED_CONCRETE;
        map[MapColor.BLACK.id] = Blocks.BLACK_CONCRETE;
        map[MapColor.GOLD.id] = Blocks.GOLD_BLOCK;
        map[MapColor.DIAMOND_BLUE.id] = Blocks.PRISMARINE_BRICKS;
        map[MapColor.LAPIS_BLUE.id] = Blocks.LAPIS_BLOCK;
        map[MapColor.EMERALD_GREEN.id] = Blocks.EMERALD_BLOCK;
        map[MapColor.SPRUCE_BROWN.id] = Blocks.SPRUCE_PLANKS;
        map[MapColor.DARK_RED.id] = Blocks.NETHERRACK;
        map[MapColor.TERRACOTTA_WHITE.id] = Blocks.WHITE_TERRACOTTA;
        map[MapColor.TERRACOTTA_ORANGE.id] = Blocks.ORANGE_TERRACOTTA;
        map[MapColor.TERRACOTTA_MAGENTA.id] = Blocks.MAGENTA_TERRACOTTA;
        map[MapColor.TERRACOTTA_LIGHT_BLUE.id] = Blocks.LIGHT_BLUE_TERRACOTTA;
        map[MapColor.TERRACOTTA_YELLOW.id] = Blocks.YELLOW_TERRACOTTA;
        map[MapColor.TERRACOTTA_LIME.id] = Blocks.LIME_TERRACOTTA;
        map[MapColor.TERRACOTTA_PINK.id] = Blocks.PINK_TERRACOTTA;
        map[MapColor.TERRACOTTA_GRAY.id] = Blocks.GRAY_TERRACOTTA;
        map[MapColor.TERRACOTTA_LIGHT_GRAY.id] = Blocks.LIGHT_GRAY_TERRACOTTA;
        map[MapColor.TERRACOTTA_CYAN.id] = Blocks.CYAN_TERRACOTTA;
        map[MapColor.TERRACOTTA_PURPLE.id] = Blocks.PURPLE_TERRACOTTA;
        map[MapColor.TERRACOTTA_BLUE.id] = Blocks.BLUE_TERRACOTTA;
        map[MapColor.TERRACOTTA_BROWN.id] = Blocks.BROWN_TERRACOTTA;
        map[MapColor.TERRACOTTA_GREEN.id] = Blocks.GREEN_TERRACOTTA;
        map[MapColor.TERRACOTTA_RED.id] = Blocks.RED_TERRACOTTA;
        map[MapColor.TERRACOTTA_BLACK.id] = Blocks.BLACK_TERRACOTTA;
        map[MapColor.DULL_RED.id] = Blocks.CRIMSON_NYLIUM;
        map[MapColor.DULL_PINK.id] = Blocks.CRIMSON_PLANKS;
        map[MapColor.DARK_CRIMSON.id] = Blocks.CRIMSON_HYPHAE;
        map[MapColor.TEAL.id] = Blocks.OXIDIZED_COPPER;
        map[MapColor.DARK_AQUA.id] = Blocks.WARPED_PLANKS;
        map[MapColor.DARK_DULL_PINK.id] = Blocks.WARPED_HYPHAE;
        map[MapColor.BRIGHT_TEAL.id] = Blocks.WARPED_WART_BLOCK;
        map[MapColor.DEEPSLATE_GRAY.id] = Blocks.COBBLED_DEEPSLATE;
        map[MapColor.RAW_IRON_PINK.id] = Blocks.RAW_IRON_BLOCK;
        map[MapColor.LICHEN_GREEN.id] = Blocks.VERDANT_FROGLIGHT;

        map[62] = Blocks.AIR;
        map[63] = Blocks.AIR;

        return new Colormap(map);
    }

    public static Path getConfigPath() {
        return OgjUtil.getConfigDir().resolve("colormap.txt");
    }

    public static Colormap loadFromConfig() throws IOException {
        Path path = Colormap.getConfigPath();
        try {
            return Colormap.load(new FileReader(path.toFile()));
        } catch (FileNotFoundException exception) {
            return Colormap.getDefaultColormap();
        }
    }

    public static Colormap tryLoadFromConfig() {
        try {
            return Colormap.loadFromConfig();
        } catch (IOException exception) {
            LOGGER.error("failed to load colormap: " + exception);
            return Colormap.getDefaultColormap();
        }
    }

    public void saveToConfig() throws IOException {
        Path path = Colormap.getConfigPath();
        FileWriter writer = new FileWriter(path.toFile());
        this.save(writer);
        writer.flush();
    }

    @ApiStatus.Internal
    public void setMapping(MapColor color, Block block) {
        this.map[color.id] = block;
    }
}
