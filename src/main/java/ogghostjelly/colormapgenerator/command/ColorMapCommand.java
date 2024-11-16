package ogghostjelly.colormapgenerator.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import ogghostjelly.colormapgenerator.ColorMapGenerator;
import ogghostjelly.colormapgenerator.utils.OgjUtils;
import ogghostjelly.colormapgenerator.utils.color.ColorMap;
import ogghostjelly.colormapgenerator.utils.color.ColorUtil;
import ogghostjelly.colormapgenerator.utils.color.IColorMap;
import ogghostjelly.colormapgenerator.utils.image.ArrayBlockImage;
import ogghostjelly.colormapgenerator.utils.image.BSPBlockImage;
import ogghostjelly.colormapgenerator.utils.image.ImageChunk;
import ogghostjelly.colormapgenerator.utils.image.RLEBlockImage;
import org.joml.Vector2i;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ColorMapCommand {
    public static void registerCommandsClient() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registry) -> dispatcher.register(literal("colormap")
                .then(literal("generate")
                        .executes(ColorMapCommand::generate))
                .then(literal("whereis")
                        .executes(ColorMapCommand::whereIs))
                .then(literal("colorof").then(argument("block", BlockStateArgumentType.blockState(registry))
                        .executes(ColorMapCommand::colorof)))
                .then(literal("blockof").then(argument("color", IntegerArgumentType.integer())
                        .executes(ColorMapCommand::blockof)))
                .then(literal("profile")
                        .executes(ColorMapCommand::profile))
                .then(literal("visualise")
                        .executes(ColorMapCommand::visualise))
                .executes(ColorMapCommand::help))));
    }

    private static Path getPathToVisualization() throws IOException {
        Path configDir = getConfigDir();
        Files.createDirectories(configDir);
        return configDir.resolve("visualization.png");
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
        NativeImage inputImage = OgjUtils.askUserForImage();
        if (inputImage == null) {
            return;
        }

        NativeImage outputImage = new NativeImage(inputImage.getWidth()*16, inputImage.getHeight()*16, false);

        ColorMap colorMap = ColorMap.generateColorMap();
        BakedModelManager bakedModelManager = context.getSource().getClient().getBakedModelManager();

        for (int y = 0; y < inputImage.getHeight(); y++) {
            for (int x = 0; x < inputImage.getWidth(); x++) {
                int color = ColorUtil.SwapFormat(inputImage.getColor(x, y));
                Block block = colorMap.colorToBlock(color);
                if (block == Blocks.AIR) {
                    continue;
                }
                Identifier blockId = Registries.BLOCK.getId(block);

                BakedModel model = bakedModelManager.getModel(BlockModels.getModelId(block.getDefaultState()));
                List<BakedQuad> quads = model.getQuads(block.getDefaultState(), Direction.UP, Random.create());
                if (quads == null || quads.isEmpty()) {
                    model = bakedModelManager.getMissingModel();
                    quads = model.getQuads(block.getDefaultState(), Direction.UP, Random.create());
                    context.getSource().sendFeedback(Text.translatable("commands.visualise.invalid-texture", blockId)
                            .withColor(Colors.RED));
                    ColorMapGenerator.LOGGER.error("invalid texture: empty quads");
                }

                SpriteContents sprite = quads.getFirst().getSprite().getContents();
                NativeImage image;

                try {
                    Field field = SpriteContents.class.getDeclaredField("image");
                    field.setAccessible(true);
                    try {
                        image = (NativeImage) field.get(sprite);
                    } catch (IllegalAccessException exception) {
                        context.getSource().sendFeedback(Text.translatable("commands.visualise.invalid-texture", blockId)
                                .withColor(Colors.RED));
                        ColorMapGenerator.LOGGER.error("invalid tex: illegal access");
                        continue;
                    }
                } catch (NoSuchFieldException exception) {
                    context.getSource().sendFeedback(Text.translatable("commands.visualise.invalid-texture", blockId)
                            .withColor(Colors.RED));
                    ColorMapGenerator.LOGGER.error("invalid tex: no such field `image`");
                    continue;
                }

                image.copyRect(outputImage, 0, 0, x*16, y*16, 16, 16, false, false);
            }
        }

        Path path = getPathToVisualization();
        context.getSource().sendFeedback(Text.translatable("commands.visualise.write", path));
        outputImage.writeTo(path);

        outputImage.close();
        inputImage.close();
    }

    private static int profile(CommandContext<FabricClientCommandSource> context) {
        try {
            profileImpl(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 1;
    }

    private static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir().resolve(ColorMapGenerator.MOD_ID);
    }

    private static Path getPathToChunkVisual(String name) throws IOException {
        Path configDir = getConfigDir().resolve("chunk_vis");
        Files.createDirectories(configDir);
        return configDir.resolve(name + ".png");
    }

    private static void profileChunks(
            CommandContext<FabricClientCommandSource> context,
            NativeImage image,
            String name,
            Function<NativeImage, Stream<ImageChunk>> fn
    ) {
        long beforeTime = System.currentTimeMillis();
        List<ImageChunk> chunks = fn.apply(image).toList();
        long afterTime = System.currentTimeMillis();

        Text text = Text.translatable("commands.profile.result", name, afterTime - beforeTime, chunks.size());
        context.getSource().sendFeedback(text);

        NativeImage scaledImage = OgjUtils.getDoubleScaledImage(image);

        chunks.forEach(chunk -> {
            Vector2i scaledFrom = new Vector2i(chunk.from.x, chunk.from.y).mul(2);
            Vector2i scaledTo = new Vector2i(chunk.to.x, chunk.to.y).mul(2);
            int fillColor = 0xff0000ff;

            if (scaledFrom == scaledTo) {
                scaledImage.setColor(scaledFrom.x, scaledFrom.y, fillColor);
                return;
            }

            for (int x = scaledFrom.x; x <= scaledTo.x; x++) {
                scaledImage.setColor(x, scaledFrom.y, fillColor);
                scaledImage.setColor(x, scaledTo.y, fillColor);
            }

            for (int y = scaledFrom.y; y <= scaledTo.y; y++) {
                scaledImage.setColor(scaledFrom.x, y, fillColor);
                scaledImage.setColor(scaledTo.x, y, fillColor);
            }
        });

        try {
            Path path = getPathToChunkVisual(name);
            context.getSource().sendFeedback(Text.translatable("commands.profile.write-vis", name, path)
                    .withColor(Colors.GRAY));
            scaledImage.writeTo(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void profileImpl(CommandContext<FabricClientCommandSource> context) throws IOException {
        NativeImage image = OgjUtils.askUserForImage();
        if (image == null) {
            return;
        }

        long beforeTime = System.currentTimeMillis();
        ColorMap colorMap = ColorMap.generateColorMap();
        long afterTime = System.currentTimeMillis();
        context.getSource().sendFeedback(Text.translatable("commands.profile.colormap-result", afterTime - beforeTime));

        profileChunks(context, image, "array", (image1) -> new ArrayBlockImage(image1, colorMap).getChunks());
        profileChunks(context, image, "rle", (image1) -> new RLEBlockImage(image1, colorMap).getChunks());
        profileChunks(context, image, "qt", (image1) -> new BSPBlockImage(image1, colorMap).getChunks());

        image.close();
    }

    private static int colorof(CommandContext<FabricClientCommandSource> context) {
        BlockStateArgument blockArg = context.getArgument("block", BlockStateArgument.class);

        int color = blockArg.getBlockState().getBlock().getDefaultMapColor().color;
        context.getSource().sendFeedback(Text.literal(Integer.toString(color)));

        return 1;
    }

    private static int blockof(CommandContext<FabricClientCommandSource> context) {
        // or 0xff000000 to make it not transparent
        int color = IntegerArgumentType.getInteger(context, "color") | 0xff000000;

        IColorMap colorMap = ColorMap.generateColorMap();
        Identifier blockId = Registries.BLOCK.getId(colorMap.colorToBlock(color));
        context.getSource().sendFeedback(Text.literal(blockId.toString()));

        return 1;
    }

    private static int help(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.translatable("commands.colormap.help"));

        return 1;
    }

    private static Path getPathToColorMap() {
        return getConfigDir().resolve("colormap.txt");
    }

    private static int whereIs(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.translatable("commands.colormap.whereis", getPathToColorMap().toString()));

        return 1;
    }

    private static int generate(CommandContext<FabricClientCommandSource> context) {
        try {
            generateImpl(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 1;
    }

    private static void generateImpl(CommandContext<FabricClientCommandSource> context) throws IOException {
        Path path = getPathToColorMap();
        BufferedWriter bw = new BufferedWriter(new FileWriter(path.toFile()));

        for (Block value : Registries.BLOCK) {
            MapColor color = value.getDefaultMapColor();
            bw.write(Registries.BLOCK.getId(value) + " " + color.color + "\n");
        }

        bw.close();

        context.getSource().sendFeedback(Text.translatable("commands.colormap.generate.success", path));
    }
}
