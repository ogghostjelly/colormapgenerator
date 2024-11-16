package ogghostjelly.colormapgenerator.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameRules;
import ogghostjelly.colormapgenerator.command.argument.FabricClientBlockPosArgumentType;
import ogghostjelly.colormapgenerator.utils.CommandExecutor;
import ogghostjelly.colormapgenerator.utils.OgjUtils;
import ogghostjelly.colormapgenerator.utils.color.ColorMap;
import ogghostjelly.colormapgenerator.utils.color.IColorMap;
import ogghostjelly.colormapgenerator.utils.image.IBlockImage;
import ogghostjelly.colormapgenerator.utils.image.RLEBlockImage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

// TODO: put image gen on a separate thread

public class ImageFillCommand {
    private static final CommandExecutor commandExecutor
            = new CommandExecutor(50);

    public static void registerCommandsClient() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(literal("imagefill")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("pos", FabricClientBlockPosArgumentType.blockPos())
                        .executes(ImageFillCommand::execute)))));

        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> dispatcher.register(literal("cancelimagefill")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    commandExecutor.cancelAll();
                    return 1;
                }))));
    }

    private static void placeBlock(MinecraftClient client, BlockPos pos, Block block) {
        Identifier blockId = Registries.BLOCK.getId(block);

        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        if (handler == null) {
            return;
        }

        handler.sendChatCommand(String.format("tp %d %d %d", pos.getX(), pos.getY() + 1, pos.getZ()));
        if (block instanceof FallingBlock || block instanceof BrushableBlock || block instanceof SnowBlock || block instanceof CarpetBlock) {
            handler.sendChatCommand(String.format("setblock %d %d %d minecraft:dirt", pos.getX(), pos.getY() - 1, pos.getZ()));
        }
        handler.sendChatCommand(String.format("setblock %d %d %d %s", pos.getX(), pos.getY(), pos.getZ(), blockId));
    }

    private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType((maxCount, count)
            -> Text.stringifiedTranslatable("commands.fill.toobig", maxCount, count));

    private static void placeCube(MinecraftClient client, BlockPos from, BlockPos to, Block block) throws CommandSyntaxException {
        Identifier blockId = Registries.BLOCK.getId(block);

        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        if (handler == null) {
            return;
        }

        if (client.world == null) {
            return;
        }

        // TODO: Add better max size check.

        Vec3i size = OgjUtils.abs(to.subtract(from));
        int blocks = size.getX() * size.getY() * size.getZ();
        int maxBlockLimit = client.world.getGameRules().getInt(GameRules.COMMAND_MODIFICATION_BLOCK_LIMIT);

        if (blocks > maxBlockLimit) {
            throw TOO_BIG_EXCEPTION.create(maxBlockLimit, blocks);
        }

        BlockPos center = new BlockPos(OgjUtils.divide(from.add(to), 2));
        handler.sendChatCommand(String.format("tp %d %d %d", center.getX(), center.getY() + 1, center.getZ()));

        handler.sendChatCommand(String.format("fill %d %d %d %d %d %d %s",
                from.getX(),
                from.getY(),
                from.getZ(),

                to.getX(),
                to.getY(),
                to.getZ(),

                blockId));
    }

    private static int execute(CommandContext<FabricClientCommandSource> context) {
        try {
            executeImpl(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 1;
    }

    static void executeImpl(CommandContext<FabricClientCommandSource> context) throws IOException {
        NativeImage image = OgjUtils.askUserForImage();
        if (image == null) {
            return;
        }


        IColorMap colorMap = ColorMap.generateColorMap();

        IBlockImage blockImage = new RLEBlockImage(image, colorMap);

        /*Vec3d originDouble = context.getSource().getPosition();
        Vec3i origin = new Vec3i((int) originDouble.x, (int) originDouble.y, (int) originDouble.z);*/
        BlockPos origin = FabricClientBlockPosArgumentType.getBlockPos(context, "pos");

        // TODO: Add better print messages

        executePrintBlockImage(origin, blockImage);

        image.close();
    }

    private static void executePrintBlockImage(Vec3i origin, @NotNull IBlockImage image) {
        image.getChunks().forEach(chunk -> {
            if (chunk.from == chunk.to) {
                var pos = new BlockPos(origin.add(new Vec3i(chunk.from.x, 0, chunk.from.y)));
                commandExecutor.add(client -> placeBlock(client, pos, chunk.block));
            } else {
                var from = new BlockPos(origin.add(new Vec3i(chunk.from.x, 0, chunk.from.y)));
                var to = new BlockPos(origin.add(new Vec3i(chunk.to.x, 0, chunk.to.y)));

                commandExecutor.add(client -> {
                    try {
                        placeCube(client, from, to, chunk.block);
                    } catch (CommandSyntaxException e) {
                        Text message = Text.literal(e.getMessage()).withColor(Colors.RED);
                        client.inGameHud.getChatHud().addMessage(message);
                        client.getNarratorManager().narrate(message);
                    }
                });
            }
        });
    }
}
