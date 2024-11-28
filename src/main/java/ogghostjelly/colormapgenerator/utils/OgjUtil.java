package ogghostjelly.colormapgenerator.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.math.Vec3i;
import ogghostjelly.colormapgenerator.ColorMapGenerator;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

public class OgjUtil {
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

    /**
     * Returns a new image which is the original image scaled by a factor of 2.
     */
    public static NativeImage getDoubleScaledImage(NativeImage image) {
        NativeImage scaledImage = new NativeImage(image.getFormat(), image.getWidth()*2, image.getHeight()*2, false);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int scaledX = x*2;
                int scaledY = y*2;
                int color = image.getColor(x, y);
                scaledImage.setColor(scaledX, scaledY, color);
                scaledImage.setColor(scaledX + 1, scaledY, color);
                scaledImage.setColor(scaledX, scaledY + 1, color);
                scaledImage.setColor(scaledX + 1, scaledY + 1, color);
            }
        }

        return scaledImage;
    }

    public static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir().resolve(ColorMapGenerator.MOD_ID);
    }
}
