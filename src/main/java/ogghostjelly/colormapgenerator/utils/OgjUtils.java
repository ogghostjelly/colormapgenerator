package ogghostjelly.colormapgenerator.utils;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.FileInputStream;
import java.io.IOException;

public class OgjUtils {
    public static @Nullable NativeImage askUserForImage() throws IOException {
        String filePath = TinyFileDialogs.tinyfd_openFileDialog("Select an image.", null, null, "image files", false);
        if (filePath == null) {
            return null;
        }
        FileInputStream reader = new FileInputStream(filePath);
        return NativeImage.read(reader);
    }

    public static Vec3i abs(Vec3i pos) {
        return new Vec3i(Math.abs(pos.getX()),
                Math.abs(pos.getY()),
                Math.abs(pos.getZ()));
    }

    public static Vec3i divide(Vec3i pos, int other) {
        return new Vec3i(pos.getX() / other,
                pos.getY() / other,
                pos.getZ() / other);
    }
}
