package ogghostjelly.colormapgenerator.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import ogghostjelly.colormapgenerator.utils.ColorMap;
import org.lwjgl.PointerBuffer;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class ColorMapCommand {
    public static void registerCommandsClient() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registry) -> dispatcher.register(literal("colormap")
                .then(literal("generate")
                        .executes(ColorMapCommand::generate))
                .then(literal("whereis")
                        .executes(ColorMapCommand::whereIs))
                .then(literal("draw").then(argument("amount", IntegerArgumentType.integer())
                        .executes(ColorMapCommand::draw)))
                .then(literal("colorof").then(argument("block", BlockStateArgumentType.blockState(registry))
                        .executes(ColorMapCommand::colorof)))
                .then(literal("blockof").then(argument("color", IntegerArgumentType.integer())
                        .executes(ColorMapCommand::blockof)))
                .executes(ColorMapCommand::help))));
    }

    private static int colorof(CommandContext<FabricClientCommandSource> context) {
        BlockStateArgument blockArg = context.getArgument("block", BlockStateArgument.class);

        int color = blockArg.getBlockState().getBlock().getDefaultMapColor().color;
        context.getSource().sendFeedback(Text.translatable("commands.colorof.result", color));

        return 1;
    }

    private static int blockof(CommandContext<FabricClientCommandSource> context) {
        int color = IntegerArgumentType.getInteger(context, "color");

        ColorMap colorMap = ColorMap.generateColorMap();
        Identifier blockId = Registries.BLOCK.getId(colorMap.colorToBlock(color));
        context.getSource().sendFeedback(Text.translatable("commands.blockof.result", blockId));

        return 1;
    }

    private static int help(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Text.translatable("commands.colormap.help"));

        return 1;
    }

    private static int draw(CommandContext<FabricClientCommandSource> context) {
        ClientPlayNetworkHandler handler = MinecraftClient.getInstance().getNetworkHandler();
        if (handler == null) {
            return 1;
        }

        ColorMap colorMap = ColorMap.generateColorMap();

        int x = 0;
        int amount = IntegerArgumentType.getInteger(context, "amount");
        for (ColorMap.ColorMapItem item : colorMap.stream().toList()) {
            Identifier blockId = Registries.BLOCK.getId(item.block);

            if (item.block instanceof FallingBlock || item.block instanceof BrushableBlock || item.block instanceof SnowBlock || item.block instanceof CarpetBlock) {
                handler.sendChatCommand(String.format("setblock ~%d ~-1 ~ minecraft:dirt", x));
            }
            handler.sendChatCommand(String.format("setblock ~%d ~ ~ %s", x, blockId));

            handler.sendChatCommand(String.format(
                    "setblock ~%d ~2 ~ minecraft:oak_sign{front_text:{has_glowing_text:1b,messages:['\"\"','\"%s\"','\"%s\"','\"\"']}}",
                    x, item.color.color, blockId
            ));
            x += 1;

            if (x >= amount) {
                break;
            }
        }

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
            return generateImpl(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int generateImpl(CommandContext<FabricClientCommandSource> context) throws IOException {
        Path path = getPathToColorMapFile();
        BufferedWriter bw = new BufferedWriter(new FileWriter(path.toFile()));

        for (Block value : Registries.BLOCK) {
            MapColor color = value.getDefaultMapColor();
            bw.write(Registries.BLOCK.getId(value) + " " + color.color + "\n");
        }

        bw.close();

        context.getSource().sendFeedback(Text.translatable("commands.colormap.generate.success", path));

        return 1;
    }
}
