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

	public static void handleScreenshotAWT(NativeImage img) {
		if (MinecraftClient.IS_SYSTEM_MAC) {
			return;
		}

		// Only allow ABGR
		if (img.getFormat() != NativeImage.Format.ABGR) {
			LOGGER.warn("Failed to capture screenshot: wrong format");
			return;
		}

		// IntellIJ doesn't like this
		//noinspection ConstantConditions
		long imagePointer = ((NativeImagePointerAccessor) (Object) img).getPointer();
		ByteBuffer buf = MemoryUtil.memByteBufferSafe(imagePointer, img.getWidth() * img.getHeight() * 4);
		if (buf == null) {
			throw new RuntimeException("Invalid image");
		}

		handleScreenshotAWT(buf, img.getWidth(), img.getHeight(), 4);
	}

	public static void handleScreenshotAWT(ByteBuffer byteBuffer, int width, int height, int components) {
		if (MinecraftClient.IS_SYSTEM_MAC) {
			return;
		}

		byte[] array;
		if (byteBuffer.hasArray()) {
			array = byteBuffer.array();
		} else {
			// can't use .array() as the buffer is not array-backed
			array = new byte[height * width * components];
			byteBuffer.get(array);
		}

		doCopy(array, width, height, components);
	}

	private static void doCopy(byte[] imageData, int width, int height, int components) {
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
					width * components, components,
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
