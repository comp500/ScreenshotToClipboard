package link.infra.screenshotclipboard;

import net.minecraft.client.Minecraft;

public class HeadlessFix {
	public static void run() {
		// A bit dangerous, but shouldn't technically cause any issues on most platforms - headless mode just disables the awt API
		// Minecraft usually has this enabled because it's using GLFW rather than AWT/Swing
		// Also causes problems on macOS, see: https://github.com/MinecraftForge/MinecraftForge/pull/5591#issuecomment-470805491

		// This uses a coremod / mixin because this must be done as early as possible - before other mods load that use awt
		// see https://github.com/BuiltBrokenModding/SBM-SheepMetal/issues/2
		if (!Minecraft.IS_RUNNING_ON_MAC) {
			System.out.println("[Screenshot to Clipboard] Setting java.awt.headless to false");
			System.setProperty("java.awt.headless", "false");
		}
	}
}
