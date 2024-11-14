package ogghostjelly.colormapgenerator.utils.image;

import net.minecraft.block.Block;
import net.minecraft.client.texture.NativeImage;
import ogghostjelly.colormapgenerator.utils.ColorMap;
import ogghostjelly.colormapgenerator.utils.ColorUtil;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class ArrayBlockImage implements IBlockImage {
    private final Block[] pixels;
    private final int width;
    private final int height;

    public ArrayBlockImage(NativeImage image, ColorMap colorMap) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.pixels = new Block[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = ColorUtil.SwapFormat(image.getColor(x, y));
                Block block = colorMap.colorToBlock(color);
                pixels[x + y * width] = block;
            }
        }
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public @NotNull Stream<ImageChunk> getChunks() {
        var chunks = new ImageChunk[this.pixels.length];

        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                int index = x + y * this.width;
                if (this.pixels[index] == null) {
                    continue;
                }
                chunks[index] = new ImageChunk(this.pixels[index], new Vector2i(x, y));
            }
        }

        return Arrays.stream(chunks).filter(Objects::nonNull);
    }
}
