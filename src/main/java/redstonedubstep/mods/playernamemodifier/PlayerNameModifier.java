package redstonedubstep.mods.playernamemodifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import redstonedubstep.mods.playernamemodifier.command.RefreshPlayerNameCommand;

@Mod("playernamemodifier")
@Mod.EventBusSubscriber
public class PlayerNameModifier {
	protected static final Logger LOGGER = LogManager.getLogger();

	public PlayerNameModifier() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ModifierConfig.SERVER_SPEC);
	}

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event){
		RefreshPlayerNameCommand.register(event.getDispatcher());
	}
}
