package ogghostjelly.colormapgenerator.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import ogghostjelly.colormapgenerator.utils.ColorMap;
import ogghostjelly.colormapgenerator.utils.ColorUtil;
import ogghostjelly.colormapgenerator.utils.CommandExecutor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

enum ImageFillOrientation implements StringIdentifiable {
    TopLeft,
    TopRight,
    BottomLeft,
    BottomRight,
    ;

    public static final Codec<ImageFillOrientation> CODEC = StringIdentifiable.createCodec(ImageFillOrientation::values);

    @Override
    public String asString() {
        return switch (this) {
            case TopLeft -> "top_left";
            case TopRight -> "top_right";
            case BottomLeft -> "bottom_left";
            case BottomRight -> "bottom_right";
        };
    }
}

class ImageFillOrientationArgumentType extends EnumArgumentType<ImageFillOrientation> {
    private ImageFillOrientationArgumentType() {
        super(ImageFillOrientation.CODEC, ImageFillOrientation::values);
    }

    public static ImageFillOrientationArgumentType orientation() {
        return new ImageFillOrientationArgumentType();
    }
}

public class ImageFillCommand {
    private record PlaceBlockCommandArgs(Block block, BlockPos pos) { }
    private static final CommandExecutor<PlaceBlockCommandArgs> commandExecutor
            = new CommandExecutor<>(50, ImageFillCommand::placeBlock);

    public static void registerCommandsClient() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(literal("imagefill")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("pos", BlockPosArgumentType.blockPos())
                        .then(argument("orientation", ImageFillOrientationArgumentType.orientation())
                                .executes(ImageFillCommand::execute))))));

        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(literal("cancelimagefill")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    commandExecutor.cancelAll();
                    return 1;
                }))));
    }

    private static void placeBlock(MinecraftClient client, PlaceBlockCommandArgs args) {
        BlockPos pos = args.pos;
        Identifier blockId = Registries.BLOCK.getId(args.block);

        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        if (handler == null) {
            return;
        }

        handler.sendChatCommand(String.format("tp %d %d %d", pos.getX(), pos.getY() + 1, pos.getZ()));
        if (args.block instanceof FallingBlock || args.block instanceof BrushableBlock || args.block instanceof SnowBlock || args.block instanceof CarpetBlock) {
            handler.sendChatCommand(String.format("setblock %d %d %d minecraft:dirt", pos.getX(), pos.getY() - 1, pos.getZ()));
        }
        handler.sendChatCommand(String.format("setblock %d %d %d %s", pos.getX(), pos.getY(), pos.getZ(), blockId));
    }

    private static int execute(CommandContext<FabricClientCommandSource> context) {
        try {
            return executeImpl(context);
        } catch (IOException | CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    static int executeImpl(CommandContext<FabricClientCommandSource> context) throws IOException, CommandSyntaxException {
        NativeImage image = askUserForImage();
        if (image == null) {
            return 1;
        }

        context.getSource().sendFeedback(Text.translatable("commands.imagefill.build-colormap.processing")
                .withColor(Colors.YELLOW));
        ColorMap colorMap = ColorMap.generateColorMap();
        context.getSource().sendFeedback(Text.translatable("commands.imagefill.build-colormap.success")
                .withColor(Colors.GREEN));

        for (int y = 0; y < image.getHeight(); y++) {
            MutableText text = Text.literal("").copy();
            for (int x = 0; x < image.getWidth(); x++) {
                int color = ColorUtil.SwapFormat(image.getColor(x, y));
                if (ColorUtil.IsTransparent(color)) {
                    text.append(Text.literal("█").withColor(Colors.GRAY));
                } else {
                    text.append(Text.literal("█").withColor(color));
                }
            }
            context.getSource().sendFeedback(text);
        }

        Vec3d originDouble = context.getSource().getPosition();
        Vec3i origin = new Vec3i((int) originDouble.x, (int) originDouble.y, (int) originDouble.z);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int color = ColorUtil.SwapFormat(image.getColor(x, y));
                if (ColorUtil.IsTransparent(color)) {
                    context.getSource().sendFeedback(Text.literal("█").withColor(Colors.GRAY));
                    continue;
                } else {
                    context.getSource().sendFeedback(Text.literal("█").withColor(color));
                }

                Block block = colorMap.colorToBlock(color);
                if (block == null) {
                    continue;
                }
                BlockPos pos = new BlockPos(origin.add(new Vec3i(x, 0, y)));

                commandExecutor.add(new PlaceBlockCommandArgs(block, pos));
            }
        }

        return 1;
    }

    static @Nullable NativeImage askUserForImage() throws IOException {
        String filePath = TinyFileDialogs.tinyfd_openFileDialog("", null, null, "image files", false);
        if (filePath == null) {
            return null;
        }
        FileInputStream reader = new FileInputStream(filePath);
        return NativeImage.read(reader);
    }
}
