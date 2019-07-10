package link.infra.screenshotclipboard;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = ScreenshotToClipboard.MODID, name = ScreenshotToClipboard.NAME, version = ScreenshotToClipboard.VERSION)
public class ScreenshotToClipboard {
	public static final String MODID = "screenshotclipboard";
	public static final String NAME = "Screenshot to Clipboard";
	public static final String VERSION = "1.12.2-1.0.0";

	private static Logger logger;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		// some example code
		logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
	}
}
