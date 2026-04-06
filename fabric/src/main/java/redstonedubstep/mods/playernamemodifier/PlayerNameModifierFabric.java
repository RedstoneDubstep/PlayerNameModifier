package redstonedubstep.mods.playernamemodifier;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.neoforged.fml.config.ModConfig;

public class PlayerNameModifierFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> RefreshPlayerNameCommand.register(dispatcher)));
		ConfigRegistry.INSTANCE.register(PlayerNameModifier.MOD_ID, ModConfig.Type.SERVER, ModifierConfig.SERVER_SPEC);
		ModConfigEvents.loading(PlayerNameModifier.MOD_ID).register(config -> ModifierConfig.onConfigUpdate());
		ModConfigEvents.reloading(PlayerNameModifier.MOD_ID).register(config -> ModifierConfig.onConfigUpdate());
	}
}
