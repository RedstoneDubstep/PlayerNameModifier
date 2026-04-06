package redstonedubstep.mods.playernamemodifier;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.neoforged.fml.config.ModConfig;

public class PlayerNameModifierFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> RefreshPlayerNameCommand.register(dispatcher)));
		NeoForgeConfigRegistry.INSTANCE.register(PlayerNameModifier.MOD_ID, ModConfig.Type.SERVER, ModifierConfig.SERVER_SPEC);
		NeoForgeModConfigEvents.loading(PlayerNameModifier.MOD_ID).register(config -> ModifierConfig.onConfigUpdate());
		NeoForgeModConfigEvents.reloading(PlayerNameModifier.MOD_ID).register(config -> ModifierConfig.onConfigUpdate());
	}
}
