package ogghostjelly.colormapgenerator.command.argument;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

/**
 * Modified version of PosArgument to support FabricClientCommandSource
 */
public interface FabricClientPosArgument {
    Vec3d toAbsolutePos(FabricClientCommandSource source);

    Vec2f toAbsoluteRotation(FabricClientCommandSource source);

    default BlockPos toAbsoluteBlockPos(FabricClientCommandSource source) {
        return BlockPos.ofFloored(this.toAbsolutePos(source));
    }

    boolean isXRelative();

    boolean isYRelative();

    boolean isZRelative();
}
