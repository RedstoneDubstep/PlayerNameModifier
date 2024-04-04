package redstonedubstep.mods.playernamemodifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkConstants;
import redstonedubstep.mods.playernamemodifier.command.RefreshPlayerNameCommand;

@Mod("playernamemodifier")
@Mod.EventBusSubscriber
public class PlayerNameModifier {
	protected static final Logger LOGGER = LogManager.getLogger();

	public PlayerNameModifier() {
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ModifierConfig.SERVER_SPEC);
	}

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event){
		RefreshPlayerNameCommand.register(event.getDispatcher());
	}
}
