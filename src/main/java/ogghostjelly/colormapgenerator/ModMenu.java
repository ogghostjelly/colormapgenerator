package ogghostjelly.colormapgenerator;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import ogghostjelly.colormapgenerator.config.ColorMappingMenuBuilder;
import ogghostjelly.colormapgenerator.utils.ColorUtil;
import ogghostjelly.colormapgenerator.utils.Colormap;
import ogghostjelly.colormapgenerator.utils.MultiColormap;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;

public class ModMenu implements ModMenuApi {
    private static final Logger LOGGER = ColorMapGenerator.LOGGER;

    public static ConfigBuilder makeConfigScreen(Screen screen) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(screen)
                .setTitle(Text.translatable("title.colormap-generator.config"))
                .setSavingRunnable(() -> {

                });

        ConfigCategory mapping = builder.getOrCreateCategory(Text.translatable("category.colormap-generator.mapping"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        var multicolormap = new MultiColormap();
        var colormap = Colormap.tryLoadFromConfig();
        var defaultColormap = Colormap.getDefaultColormap();

        for (MapColor color : ColorUtil.getMapColors()) {
            if (color == MapColor.CLEAR) {
                continue;
            }

            ArrayList<Block> blocks = new ArrayList<>(multicolormap.colorToBlocks(color).toList());
            if (blocks.isEmpty()) {
                LOGGER.error("No blocks found that match color `"+color+"`");
                continue;
            }
            blocks.add(0, Blocks.AIR);

            mapping.addEntry(
                    ColorMappingMenuBuilder.start(entryBuilder, Text.translatable("color.colormap-generator." + color.id),
                            blocks.toArray(new Block[0]),
                            colormap.colorToBlock(color),
                            color)
                    .setNameProvider(block -> block != Blocks.AIR ? block.getName() : Text.translatable("gui.none"))
                    .setDefaultValue(defaultColormap.colorToBlock(color))
                    .setSaveConsumer(block -> {
                        colormap.setMapping(color, block);
                    })
                    .build());
        }

        builder.setSavingRunnable(() -> {
            try {
                colormap.saveToConfig();
            } catch (IOException exception) {
                LOGGER.error("failed to save colormap: " + exception);
            }
        });

        return builder;
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> makeConfigScreen(parent).build();
    }
}
