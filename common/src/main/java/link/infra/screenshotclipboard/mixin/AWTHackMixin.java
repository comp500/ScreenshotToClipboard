package link.infra.screenshotclipboard.mixin;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(Main.class)
public class AWTHackMixin {
	// Inject as early as possible (but after Main statics execute), and disable java.awt.headless on non-macOS systems
	@Inject(method = "main", at = @At("HEAD"))
	private static void awtHack(CallbackInfo ci) {
		// A bit dangerous, but shouldn't technically cause any issues on most platforms - headless mode just disables the awt API
		// Minecraft usually has this enabled because it's using GLFW rather than AWT/Swing
		// Also causes problems on macOS, see: https://github.com/MinecraftForge/MinecraftForge/pull/5591#issuecomment-470805491

		// This uses a Mixin because this must be done as early as possible - before other mods load that use AWT
		// see https://github.com/BuiltBrokenModding/SBM-SheepMetal/issues/2
		if (!System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac")) {
			System.out.println("[Screenshot to Clipboard] Setting java.awt.headless to false");
			System.setProperty("java.awt.headless", "false");
		}

		// TODO: is this necessary?
//		try {
//			Class<?> clazz = Class.forName("java.awt.Toolkit");
//			Field field = clazz.getDeclaredField("loaded");
//			field.setAccessible(true);
//			field.setBoolean(null, true);
//		} catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
//			e.printStackTrace();
//		}
	}

}
