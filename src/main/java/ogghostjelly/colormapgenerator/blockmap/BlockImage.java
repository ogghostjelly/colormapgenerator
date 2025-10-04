package ogghostjelly.colormapgenerator.blockmap;

import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BlockImage {
    private final BlockmapItem[][] pixels;
    private final int width;
    private final int height;
    private boolean hasElevationFlag = false;

    public BlockImage(int width, int height) {
        this.pixels = new BlockmapItem[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.pixels[y][x] = BlockmapItem.AIR;
            }
        }

        this.width = width;
        this.height = height;
    }

    public BlockmapItem getPixel(int x, int y) {
        return this.pixels[y][x];
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setPixel(int x, int y, @NotNull Block value) {
        setPixel(x, y, value, BlockmapItem.Elevation.NORMAL);
    }

    public boolean hasElevation() {
        return hasElevationFlag;
    }

    public void setPixel(int x, int y, @NotNull Block value, @NotNull BlockmapItem.Elevation elevation) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(elevation);
        if (elevation != BlockmapItem.Elevation.NORMAL) {
            this.hasElevationFlag = true;
        }
        this.pixels[y][x] = new BlockmapItem(value, elevation);
    }
}
