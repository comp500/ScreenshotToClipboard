package link.infra.screenshotclipboard;

import link.infra.screenshotclipboard.mixin.NativeImagePointerAccessor;
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

public class ScreenshotToClipboard {
	public static final String MOD_ID = "screenshotclipboard";
	private static final Logger LOGGER = LogManager.getFormatterLogger("ScreenshotToClipboard");

	// TODO: fabrishot support
	// TODO: test/port to 1.17; constrain accepted versions as necessary

	public static void init() {
		if (!MinecraftClient.IS_SYSTEM_MAC) {
			// Test that the mixin was run properly
			// Ensure AWT is loaded by forcing loadLibraries() to be called, will cause a HeadlessException if someone else already loaded AWT
			try {
				Toolkit.getDefaultToolkit().getSystemClipboard();
			} catch (HeadlessException e) {
				LOGGER.warn("java.awt.headless property was not set properly!");
			}
		}
	}

	private static boolean useHackyMode = true;

	public static void handleScreenshotAWT(NativeImage img) {
		if (MinecraftClient.IS_SYSTEM_MAC) {
			return;
		}

		// Only allow ABGR
		if (img.getFormat() != NativeImage.Format.ABGR) {
			return;
		}

		// Convert NativeImage to BufferedImage
		ByteBuffer byteBuffer = null;
		if (useHackyMode) {
			try {
				byteBuffer = hackyUnsafeGetPixelsABGR(img);
			} catch (Exception e) {
				LOGGER.warn("An error has occurred trying to take a screenshot using Hacky Mode (tm), Safe Mode will be used", e);
				useHackyMode = false;
			}
			if (!useHackyMode) {
				byteBuffer = safeGetPixelsABGR(img);
			}
		} else {
			byteBuffer = safeGetPixelsABGR(img);
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

	// This method is theoretically faster than safeGetPixelsABGR but it might explode violently
	private static ByteBuffer hackyUnsafeGetPixelsABGR(NativeImage img) throws RuntimeException {
		// IntellIJ doesn't like this
		//noinspection ConstantConditions
		long imagePointer = ((NativeImagePointerAccessor) (Object) img).getPointer();
		ByteBuffer buf = MemoryUtil.memByteBufferSafe(imagePointer, img.getWidth() * img.getHeight() * 4);
		if (buf == null) {
			throw new RuntimeException("Invalid image");
		}
		return buf;
	}

	private static ByteBuffer safeGetPixelsABGR(NativeImage img) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(img.getWidth() * img.getHeight() * 4);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN); // is this system dependent? TEST!!
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				byteBuffer.putInt(img.getPixelColor(x, y));
			}
		}
		return byteBuffer;
	}

	private static void doCopy(byte[] imageData, int width, int height) {
		new Thread(() -> {
			DataBufferByte buf = new DataBufferByte(imageData, imageData.length);
			ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
			// Ignore the alpha channel, due to JDK-8204187
			int[] nBits = {8, 8, 8};
			int[] bOffs = {0, 1, 2}; // is this efficient, no transformation is being done?
			ColorModel cm = new ComponentColorModel(cs, nBits, false, false,
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
