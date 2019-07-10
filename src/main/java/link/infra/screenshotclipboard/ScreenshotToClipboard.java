package link.infra.screenshotclipboard;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = ScreenshotToClipboard.MODID, name = ScreenshotToClipboard.NAME, version = ScreenshotToClipboard.VERSION, clientSideOnly = true, acceptedMinecraftVersions = "[1.10.2,1.12.2]", acceptableRemoteVersions = "*")
@Mod.EventBusSubscriber
public class ScreenshotToClipboard {
	public static final String MODID = "screenshotclipboard";
	public static final String NAME = "Screenshot to Clipboard";
	public static final String VERSION = "1.12.2-1.0.0";

	@SubscribeEvent
	public static void handleScreenshot(ScreenshotEvent event) {
		Transferable trans = getTransferableImage(event.getImage());
		Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
		c.setContents(trans, null);
	}

	private static Transferable getTransferableImage(final BufferedImage bufferedImage) {
		return new Transferable() {
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] { DataFlavor.imageFlavor };
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
				return null;
			}
		};
	}

}
