package ogghostjelly.colormapgenerator.config;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import ogghostjelly.colormapgenerator.ColorMapGenerator;
import ogghostjelly.colormapgenerator.blockmap.FallthroughBlockmap;
import ogghostjelly.colormapgenerator.blockmap.IBlockmap;
import ogghostjelly.colormapgenerator.utils.OgjUtil;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Tools for loading and saving the `colormap.txt` config file.
 */
public class ColormapConfig {
    private static final Logger LOGGER = ColorMapGenerator.LOGGER;

    private final Block[] map;

    public ColormapConfig(Block[] map) {
        this.map = map;
    }

    public IBlockmap toColormap() {
        return new FallthroughBlockmap(this.map);
    }

    public Block getMapping(MapColor color) {
        return this.map[color.id];
    }

    public void setMapping(MapColor color, Block block) {
        this.map[color.id] = block;
    }

    public static ColormapConfig getDefaultColormap() {
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
        map[MapColor.WATER_BLUE.id] = null;
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

        map[62] = null;
        map[63] = null;

        return new ColormapConfig(map);
    }

    public void save(FileWriter writer) throws IOException {
        for (int i = 0; i < map.length; i++) {
            writer.write(i+" "+ (map[i] != null ? Registries.BLOCK.getId(map[i]) : null) + "\n");
        }
    }

    public static ColormapConfig load(InputStreamReader reader) throws IOException {
        ColormapConfig map = ColormapConfig.getDefaultColormap();
        var buf = new StringBuilder();
        char chr;

        int lim_i = 0;
        int lim = 1_000_000;

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

                if (Objects.equals(parts[1], "null")) {
                    map.setMapping(MapColor.get(mapColorId), null);
                    continue;
                }
                Identifier blockId = Identifier.tryParse(parts[1]);
                if (blockId == null) {
                    LOGGER.error("failed to parse block id when loading colormap, skipping.");
                    continue;
                }

                map.setMapping(MapColor.get(mapColorId), Registries.BLOCK.get(blockId));
            } else {
                buf.append(chr);
            }

            lim_i += 1;

            if (lim_i >= lim) {
                LOGGER.warn("colormap is unusually large, aborting.");
                break;
            }
        }

        return map;
    }

    public static Path getConfigPath() {
        return OgjUtil.getConfigDir().resolve("colormap.txt");
    }

    public static ColormapConfig loadFromConfig() throws IOException {
        Path path = getConfigPath();
        try {
            return load(new FileReader(path.toFile()));
        } catch (FileNotFoundException exception) {
            return getDefaultColormap();
        }
    }

    public static ColormapConfig tryLoadFromConfig() {
        try {
            return loadFromConfig();
        } catch (IOException exception) {
            LOGGER.error("failed to load colormap: " + exception);
            return getDefaultColormap();
        }
    }

    public void saveToConfig() throws IOException {
        Path path = getConfigPath();
        FileWriter writer = new FileWriter(path.toFile());
        this.save(writer);
        writer.flush();
    }
}
