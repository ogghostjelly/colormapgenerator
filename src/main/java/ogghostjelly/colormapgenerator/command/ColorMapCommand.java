package ogghostjelly.colormapgenerator.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ogghostjelly.colormapgenerator.utils.OgjUtils;
import ogghostjelly.colormapgenerator.utils.color.ColorMap;
import ogghostjelly.colormapgenerator.utils.color.IColorMap;
import ogghostjelly.colormapgenerator.utils.image.ArrayBlockImage;
import ogghostjelly.colormapgenerator.utils.image.ImageChunk;
import ogghostjelly.colormapgenerator.utils.image.QTBlockImage;
import ogghostjelly.colormapgenerator.utils.image.RLEBlockImage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;
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
                .executes(ColorMapCommand::help))));
    }

    private static int profile(CommandContext<FabricClientCommandSource> context) {
        try {
            profileImpl(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 1;
    }

    private static void profileChunks(CommandContext<FabricClientCommandSource> context, String name, Supplier<Stream<ImageChunk>> fn) {
        long beforeTime = System.currentTimeMillis();
        Stream<ImageChunk> chunks = fn.get();
        long afterTime = System.currentTimeMillis();
        Text text = Text.translatable("commands.profile.result", name, afterTime - beforeTime, chunks.count());
        context.getSource().sendFeedback(text);
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

        profileChunks(context, "array", () -> new ArrayBlockImage(image, colorMap).getChunks());
        profileChunks(context, "rle", () -> new RLEBlockImage(image, colorMap).getChunks());
        profileChunks(context, "qt", () -> new QTBlockImage(image, colorMap).getChunks());

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

    private static Path getPathToColorMapFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("colormap.txt");
    }

    private static int whereIs(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.translatable("commands.colormap.whereis", getPathToColorMapFile().toString()));

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
        Path path = getPathToColorMapFile();
        BufferedWriter bw = new BufferedWriter(new FileWriter(path.toFile()));

        for (Block value : Registries.BLOCK) {
            MapColor color = value.getDefaultMapColor();
            bw.write(Registries.BLOCK.getId(value) + " " + color.color + "\n");
        }

        bw.close();

        context.getSource().sendFeedback(Text.translatable("commands.colormap.generate.success", path));
    }
}
