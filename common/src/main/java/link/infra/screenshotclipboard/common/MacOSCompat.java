package link.infra.screenshotclipboard.common;

import ca.weblite.objc.Client;
import ca.weblite.objc.Proxy;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MacOSCompat {
	private static final Logger LOGGER = LogManager.getFormatterLogger("ScreenshotToClipboard-MacOSCompat");

	// macOS requires some ugly hacks to get it to work, because it doesn't allow GLFW and AWT to load at the same time
	// See: https://github.com/MinecraftForge/MinecraftForge/pull/5591#issuecomment-470805491
	// Thanks to @juliand665 for writing and testing most of this code, I don't have a Mac!

	public static void doCopyMacOS(String path) {
		if (!MinecraftClient.IS_SYSTEM_MAC) {
			return;
		}

		Client client = Client.getInstance();
		Proxy url = client.sendProxy("NSURL", "fileURLWithPath:", path);

		Proxy image = client.sendProxy("NSImage", "alloc");
		image.send("initWithContentsOfURL:", url);

		Proxy array = client.sendProxy("NSArray", "array");
		array = array.sendProxy("arrayByAddingObject:", image);

		Proxy pasteboard = client.sendProxy("NSPasteboard", "generalPasteboard");
		pasteboard.send("clearContents");
		boolean wasSuccessful = pasteboard.sendBoolean("writeObjects:", array);
		if (!wasSuccessful) {
			LOGGER.error("Failed to write image to pasteboard!");
		}
	}
}
