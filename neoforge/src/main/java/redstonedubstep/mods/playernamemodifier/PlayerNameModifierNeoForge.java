package redstonedubstep.mods.playernamemodifier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod(PlayerNameModifier.MOD_ID)
@EventBusSubscriber
public class PlayerNameModifierNeoForge {
	public PlayerNameModifierNeoForge(ModContainer container) {
		container.registerConfig(ModConfig.Type.SERVER, ModifierConfig.SERVER_SPEC);
	}

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event){
		RefreshPlayerNameCommand.register(event.getDispatcher());
	}

	@SubscribeEvent
	public static void onConfigUpdate(ModConfigEvent event) {
		if (!(event instanceof ModConfigEvent.Unloading))
			ModifierConfig.onConfigUpdate();
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onPlayerName(PlayerEvent.NameFormat event) {
		event.setDisplayname(PlayerNameModifier.onPlayerDisplayName(event.getEntity(), event.getDisplayname()));
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onPlayerTabListName(PlayerEvent.TabListNameFormat event) {
		Player player = event.getEntity();
		Component oldTabName = event.getDisplayName() != null ? event.getDisplayName() : Component.literal(player.getGameProfile().name());

		event.setDisplayName(PlayerNameModifier.onPlayerTabListName(player, oldTabName));
	}
}
