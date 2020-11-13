package link.infra.screenshotclipboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
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
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Mod("screenshotclipboard")
public class ScreenshotToClipboard {
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static byte[] savedArray;
	private static int savedWidth;
	private static int savedHeight;
	private static ScreenshotEvent savedEvent;
	
	@SubscribeEvent
	public void handleChat(ClientChatEvent event)
	{
		String message = event.getMessage();
		if (message.equals("/copyscreenshot"))
		{
			if (Minecraft.IS_RUNNING_ON_MAC)
			{
				MacOSCompat.handleScreenshot(savedEvent);
			}
			else
			{
				doCopy(savedArray, savedWidth, savedHeight);
			}
			
			StringTextComponent base = new StringTextComponent("Screenshot ");
			
			Style copiedStyle = new Style();
			copiedStyle.setColor(TextFormatting.GREEN);
			
			StringTextComponent copiedText = new StringTextComponent("copied!");
			copiedText.setStyle(copiedStyle);
			
			base.appendSibling(copiedText);
			Minecraft.getInstance().ingameGUI.addChatMessage(ChatType.SYSTEM, base);
			
			if (event.isCancelable())
			{
				event.setCanceled(true);
			}
		}
	}
	
	public ScreenshotToClipboard() {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			MinecraftForge.EVENT_BUS.register(this);
			if (Minecraft.IS_RUNNING_ON_MAC) {
				// Test that the coremod was run properly
				// Ensure AWT is loaded by forcing loadLibraries() to be called, will cause a HeadlessException if someone else already called AWT
				try {
					Toolkit.getDefaultToolkit().getSystemClipboard();
				} catch (HeadlessException e) {
					LOGGER.warn("java.awt.headless property was not set properly!");
				}
			}
		});
	}

	private boolean useHackyMode = true;

	@SubscribeEvent
	public void handleScreenshot(ScreenshotEvent event) {
		Style yesStyle = new Style();
		yesStyle.setColor(TextFormatting.GREEN);
		yesStyle.setBold(true);
		yesStyle.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/copyscreenshot"));
		
		StringTextComponent hoverText = new StringTextComponent("Click to copy ");
		StringTextComponent latest = new StringTextComponent("latest");
		latest.setStyle(new Style().setBold(true));
		StringTextComponent hoverText2 = new StringTextComponent(" screenshot");
		
		hoverText.appendSibling(latest);
		hoverText.appendSibling(hoverText2);
		
		yesStyle.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
		
		StringTextComponent base = new StringTextComponent("Screenshot taken! ");
		StringTextComponent yes = new StringTextComponent("[Copy]");
		yes.setStyle(yesStyle);
		
		base.appendSibling(yes);
		event.setResultMessage(base);
		
		NativeImage img = event.getImage();
		// Only allow RGBA
		if (img.getFormat() != NativeImage.PixelFormat.RGBA) {
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
		
		savedArray = array;
		savedWidth = img.getWidth();
		savedHeight = img.getHeight();
		savedEvent = event;
	}

	private Field imagePointerField = null;

	// This method is theoretically faster than safeGetPixelsRGBA but it might explode violently
	private ByteBuffer hackyUnsafeGetPixelsRGBA(NativeImage img) throws Exception {
		if (imagePointerField == null) {
			imagePointerField = ObfuscationReflectionHelper.findField(NativeImage.class, "field_195722_d");
		}
		long imagePointer = imagePointerField.getLong(img);
		ByteBuffer buf = MemoryUtil.memByteBufferSafe(imagePointer, img.getWidth() * img.getHeight() * 4);
		if (buf == null) {
			throw new RuntimeException("Invalid image");
		}
		return buf;
	}

	private ByteBuffer safeGetPixelsRGBA(NativeImage img) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(img.getWidth() * img.getHeight() * 4);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN); // is this system dependent? TEST!!
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				byteBuffer.putInt(img.getPixelRGBA(x, y));
			}
		}
		return byteBuffer;
	}

	private void doCopy(byte[] imageData, int width, int height) {
		new Thread(() -> {
			DataBufferByte buf = new DataBufferByte(imageData, imageData.length);
			// This is RGBA but it doesn't work with ColorModel.getRGBdefault for some reason!
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

	private Transferable getTransferableImage(final BufferedImage bufferedImage) {
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
			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
				if (DataFlavor.imageFlavor.equals(flavor)) {
					return bufferedImage;
				}
				throw new UnsupportedFlavorException(flavor);
			}
		};
	}
}
