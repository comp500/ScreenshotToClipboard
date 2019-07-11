package link.infra.screenshotclipboard;

import ca.weblite.objc.Client;
import ca.weblite.objc.Proxy;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.client.event.ScreenshotEvent;

import javax.annotation.Nonnull;
import java.util.Iterator;

class MacOSCompat {
	// macOS requires some ugly hacks to get it to work, because it doesn't allow GLFW and AWT to load at the same time
	// See: https://github.com/MinecraftForge/MinecraftForge/pull/5591#issuecomment-470805491
	// Thanks to @juliand665 for writing and testing most of this code, I don't have a Mac!

	static void handleScreenshot(ScreenshotEvent event) {
		String name = event.getScreenshotFile().getName();
		String path = event.getScreenshotFile().getAbsolutePath();
		// Replicate the default result message, but make it a NotifierTranslationTextComponent
		// that does the copy when it is displayed (very sneakyyyyy)
		ITextComponent itextcomponent = (new StringTextComponent(name)).applyTextStyle(TextFormatting.UNDERLINE).applyTextStyle((style) -> {
			style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path));
		});
		TranslationTextComponent msg = new NotifierTranslationTextComponent("screenshot.success", path, itextcomponent);
		event.setResultMessage(msg);
	}

	private static class NotifierTranslationTextComponent extends TranslationTextComponent {
		final String path;
		private boolean isDone = false;
		NotifierTranslationTextComponent(String translationKey, String path, Object... args) {
			super(translationKey, args);
			this.path = path;
		}

		@Override
		@Nonnull
		public Iterator<ITextComponent> iterator() {
			if (!isDone) {
				isDone = true;
				doCopyMacOS(path);
			}
			return super.iterator();
		}
	}

	private static void doCopyMacOS(String path) {
		if (!Minecraft.IS_RUNNING_ON_MAC) {
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
		assert wasSuccessful;
	}
}
