package link.infra.screenshotclipboard;

import link.infra.screenshotclipboard.mixin.NativeImageMixin;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ScreenshotToClipboard implements ModInitializer {
	private static final Logger LOGGER = LogManager.getFormatterLogger("ScreenshotToClipboard");

	@Override
	public void onInitialize() {
		// A bit dangerous, but shouldn't technically cause any issues on most platforms - headless mode just disables the awt API
		// Minecraft usually has this enabled because it's using GLFW rather than AWT/Swing
		// Also causes problems on macOS, see: https://github.com/MinecraftForge/MinecraftForge/pull/5591#issuecomment-470805491
		if (!MinecraftClient.IS_SYSTEM_MAC) {
			System.setProperty("java.awt.headless", "false");
		}
	}

	private static boolean useHackyMode = true;

	public static void handleScreenshot(NativeImage img) {
		if (MinecraftClient.IS_SYSTEM_MAC) {
			return;
		}

		// Only allow RGBA
		if (img.getFormat() != NativeImage.Format.RGBA) {
			return;
		}

		// Convert NativeImage to BufferedImage
		ByteBuffer byteBuffer = null;
		if (useHackyMode) {
			try {
				byteBuffer = hackyUnsafeGetPixelsRGBA(img);
			} catch (Exception e) {
				LOGGER.warn("An error has occurred trying to take a screenshot using Hacky Mode (tm), Safe Mode will be used", e);
				useHackyMode = false;
			}
			if (!useHackyMode) {
				byteBuffer = safeGetPixelsRGBA(img);
			}
		} else {
			byteBuffer = safeGetPixelsRGBA(img);
		}

		byte[] array;
		if (byteBuffer.hasArray()) {
			array = byteBuffer.array();
		} else {
			// can't use .array() because unsafe retrieval references the volatile bytes directly!!
			array = new byte[img.getHeight() * img.getWidth() * 4];
			byteBuffer.get(array);
		}

		doCopy(array, img.getWidth(), img.getHeight());
	}

	// This method is theoretically faster than safeGetPixelsRGBA but it might explode violently
	private static ByteBuffer hackyUnsafeGetPixelsRGBA(NativeImage img) throws RuntimeException {
		long imagePointer = ((NativeImageMixin) (Object) img).getPointer();
		ByteBuffer buf = MemoryUtil.memByteBufferSafe(imagePointer, img.getWidth() * img.getHeight() * 4);
		if (buf == null) {
			throw new RuntimeException("Invalid image");
		}
		return buf;
	}

	private static ByteBuffer safeGetPixelsRGBA(NativeImage img) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(img.getWidth() * img.getHeight() * 4);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN); // is this system dependent? TEST!!
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				byteBuffer.putInt(img.getPixelRGBA(x, y));
			}
		}
		return byteBuffer;
	}

	private static void doCopy(byte[] imageData, int width, int height) {
		new Thread(() -> {
			DataBufferByte buf = new DataBufferByte(imageData, imageData.length);
			// This is RGBA but it doesn't work with ColorModel.getRGBdefault for some reason!
			ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
			int[] nBits = {8, 8, 8, 8};
			int[] bOffs = {0, 1, 2, 3}; // is this efficient, no transformation is being done?
			ColorModel cm = new ComponentColorModel(cs, nBits, true, false,
					Transparency.TRANSLUCENT,
					DataBuffer.TYPE_BYTE);
			BufferedImage bufImg = new BufferedImage(cm, Raster.createInterleavedRaster(buf,
					width, height,
					width * 4, 4,
					bOffs, null), false, null);

			Transferable trans = getTransferableImage(bufImg);
			Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
			c.setContents(trans, null);
		}, "Screenshot to Clipboard Copy").start();
	}

	private static Transferable getTransferableImage(final BufferedImage bufferedImage) {
		return new Transferable() {
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[]{DataFlavor.imageFlavor};
			}

			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return DataFlavor.imageFlavor.equals(flavor);
			}

			@Override
			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
				if (DataFlavor.imageFlavor.equals(flavor)) {
					return bufferedImage;
				}
				throw new UnsupportedFlavorException(flavor);
			}
		};
	}
}
