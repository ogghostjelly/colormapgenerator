package ogghostjelly.colormapgenerator.command;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import ogghostjelly.colormapgenerator.ColorMapGenerator;
import ogghostjelly.colormapgenerator.ModMenu;
import ogghostjelly.colormapgenerator.colormap.IColormap;
import ogghostjelly.colormapgenerator.config.ColormapConfig;
import ogghostjelly.colormapgenerator.utils.OgjUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ColormapCommand {
    private static final Logger LOGGER = ColorMapGenerator.LOGGER;

    public static void registerCommandsClient() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registry) -> dispatcher.register(literal("colormap")
                .then(literal("settings")
                        .executes(ColormapCommand::settings))
                .then(literal("openconfigfolder")
                        .executes(ColormapCommand::openconfigfolder))
                .then(literal("colorof").then(argument("block", BlockStateArgumentType.blockState(registry))
                        .executes(ColormapCommand::colorof)))
                .then(literal("visualise")
                        .executes(ColormapCommand::visualise))
                .executes(ColormapCommand::help))));
    }

    /* === VISUALISATION === */

    private static Path getPathToVisDir() {
        return OgjUtil.getConfigDir().resolve("vis");
    }

    private static Path getPathToInVis() throws IOException {
        Path configDir = getPathToVisDir();
        Files.createDirectories(configDir);
        return configDir.resolve("in.png");
    }

    private static Path getPathToBigVis() throws IOException {
        Path configDir = getPathToVisDir();
        Files.createDirectories(configDir);
        return configDir.resolve("big-out.png");
    }

    private static Path getPathToTinyVis() throws IOException {
        Path configDir = getPathToVisDir();
        Files.createDirectories(configDir);
        return configDir.resolve("out.png");
    }

    private static int visualise(CommandContext<FabricClientCommandSource> context) {
        try {
            visualiseImpl(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 1;
    }

    private static void visualiseImpl(CommandContext<FabricClientCommandSource> context) throws IOException {
        NativeImage inputNativeImage = OgjUtil.askUserForImage();
        if (inputNativeImage == null) {
            return;
        }
        /*TODO
        Image inputImage = new ThresholdImage(inputNativeImage);

        NativeImage bigOutputImage = new NativeImage(inputImage.getWidth()*16, inputImage.getHeight()*16, false);
        NativeImage tinyOutputImage = new NativeImage(inputImage.getWidth(), inputImage.getHeight(), false);

        IColormap colorMap = getColormap();
        BakedModelManager bakedModelManager = context.getSource().getClient().getBakedModelManager();

        for (int y = 0; y < inputImage.getHeight(); y++) {
            for (int x = 0; x < inputImage.getWidth(); x++) {
                MapColor color = inputImage.getPixel(x, y);
                Block block = colorMap.colorToBlock(color);

                if (block != Blocks.AIR) {
                    tinyOutputImage.setColor(x, y, ColorUtil.SwapFormat(ColorUtil.mapColorToARGB(block.getDefaultMapColor())));
                } else {
                    tinyOutputImage.setColor(x, y, 0);
                }

                if (block == Blocks.AIR) {
                    continue;
                }
                Identifier blockId = Registries.BLOCK.getId(block);

                BakedModel model = bakedModelManager.getModel(BlockModels.getModelId(block.getDefaultState()));
                List<BakedQuad> quads = model.getQuads(block.getDefaultState(), Direction.UP, Random.create());
                if (quads == null || quads.isEmpty()) {
                    model = bakedModelManager.getMissingModel();
                    quads = model.getQuads(block.getDefaultState(), Direction.UP, Random.create());
                    context.getSource().sendFeedback(Text.translatable("commands.colormap-generator.visualise.invalid-texture", blockId)
                            .withColor(Colors.RED));
                    LOGGER.error("invalid texture: empty quads for `"+blockId+"`");
                }

                SpriteContents sprite = quads.getFirst().getSprite().getContents();
                NativeImage image;

                try {
                    Field field = SpriteContents.class.getDeclaredField("image");
                    field.setAccessible(true);
                    try {
                        image = (NativeImage) field.get(sprite);
                    } catch (IllegalAccessException exception) {
                        context.getSource().sendFeedback(Text.translatable("commands.colormap-generator.visualise.invalid-texture", blockId)
                                .withColor(Colors.RED));
                        LOGGER.error("invalid texture: illegal access for `"+blockId+"`");
                        continue;
                    }
                } catch (NoSuchFieldException exception) {
                    context.getSource().sendFeedback(Text.translatable("commands.colormap-generator.visualise.invalid-texture", blockId)
                            .withColor(Colors.RED));
                    LOGGER.error("invalid texture: no such field `image` for `"+blockId+"`");
                    continue;
                }

                image.copyRect(bigOutputImage, 0, 0, x*16, y*16, 16, 16, false, false);
            }
        }

        context.getSource().sendFeedback(Text.translatable("commands.colormap-generator.visualise.write", Text.of(getPathToVisDir().toUri())));
        inputNativeImage.writeTo(getPathToInVis());
        bigOutputImage.writeTo(getPathToBigVis());
        tinyOutputImage.writeTo(getPathToTinyVis());

        bigOutputImage.close();
        tinyOutputImage.close();
        inputNativeImage.close();*/
    }

    /* === COLOR OF/BLOCK OF === */

    private static int colorof(CommandContext<FabricClientCommandSource> context) {
        BlockStateArgument blockArg = context.getArgument("block", BlockStateArgument.class);

        int color = blockArg.getBlockState().getBlock().getDefaultMapColor().color;
        context.getSource().sendFeedback(Text.literal(Integer.toString(color)));

        return 1;
    }

    /* === HELP === */

    private static int help(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.translatable("commands.colormap-generator.help"));

        return 1;
    }

    /* === COLORMAP SETTINGS === */

    private static @NotNull IColormap getColormap() {
        return ColormapConfig.tryLoadFromConfig().toColormap();
    }

    private static int openconfigfolder(CommandContext<FabricClientCommandSource> context) {
        Util.getOperatingSystem().open(OgjUtil.getConfigDir());

        return 1;
    }

    private static int settings(CommandContext<FabricClientCommandSource> context) {
        try {
            settingsImpl(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 1;
    }

    private static void settingsImpl(CommandContext<FabricClientCommandSource> context) throws IOException {
        MinecraftClient client = context.getSource().getClient();
        Screen screen = ModMenu.makeConfigScreen(client.currentScreen).build();
        // for some reason setScreen is not setting the screen ???
        client.setScreen(screen);

        // TODO
        context.getSource().sendFeedback(Text.literal("This feature is still under construction, in the meantime consider installing the modmenu mod."));
    }
}
