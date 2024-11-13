package ogghostjelly.colormapgenerator.utils.image;

import net.minecraft.util.math.Vec3i;

public class ImageChunk {
    public final Vec3i from;
    public final Vec3i to;

    public ImageChunk(Vec3i from, Vec3i to) {
        this.from = from;
        this.to = to;
    }
}
