package ogghostjelly.colormapgenerator;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CactusBlock;
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
import java.util.List;

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

        var colormap = new MultiColormap();

        mapping.addEntry(entryBuilder.startIntList(Text.literal("IntList"), List.of(new Integer[]{1, 2, 3, 4, 5, 6, 7})).build());
        mapping.addEntry(entryBuilder.startIntField(Text.literal("IntField"), 5).build());
        mapping.addEntry(entryBuilder.startIntSlider(Text.literal("IntSlider"), 0, 10, 1).build());
        mapping.addEntry(entryBuilder.startTextDescription(Text.literal("TextDescription")).build());

        for (MapColor color : ColorUtil.getMapColors()) {
            if (color == MapColor.CLEAR) {
                continue;
            }

            ArrayList<Block> blocks = new ArrayList<>(colormap.colorToBlocks(color).toList());

            if (blocks.isEmpty()) {
                LOGGER.error("No blocks found that match color `"+color+"`");
                continue;
            }

            Block suggestedBlock = blocks.stream()
                    .filter(block -> block.getDefaultState().getFluidState().isEmpty() &&
                            block.getDefaultState().isOpaque() &&
                            !(block instanceof CactusBlock))
                    .findAny()
                    .orElse(blocks.getFirst());

            blocks.add(0, Blocks.AIR);

            mapping.addEntry(
                    ColorMappingMenuBuilder.start(entryBuilder, Text.translatable("color.colormap-generator." + color.id),
                            blocks.toArray(new Block[0]),
                            suggestedBlock,
                            color)
                    .setNameProvider(block -> block != Blocks.AIR ? block.getName() : Text.translatable("gui.none"))
                    .setDefaultValue(suggestedBlock)
                    .setSaveConsumer(block -> {
                        LOGGER.warn("Saving " + block);
                        try {
                            Colormap cm = Colormap.loadFromConfig();
                            cm.setMapping(block.getDefaultMapColor(), block);
                            cm.saveToConfig();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .build());
        }

        return builder;
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> makeConfigScreen(parent).build();
    }
}
