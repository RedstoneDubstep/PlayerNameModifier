package redstonedubstep.mods.playernamemodifier.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import redstonedubstep.mods.playernamemodifier.PlayerNameModifier;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
	public ServerPlayerMixin(Level level, GameProfile gameProfile) {
		super(level, gameProfile);
	}

	//Fabric: Decorates any pre-existing tab list display name using this mod's configuration, and returns the decorated name
	@ModifyReturnValue(method = "getTabListDisplayName", at = @At("RETURN"))
	private Component playernamemodifier$modifyTabListDisplayName(Component original) {
		Component oldTabName = original != null ? original : Component.literal(getGameProfile().name());

		return PlayerNameModifier.onPlayerTabListName(this, oldTabName);
	}
}
