package redstonedubstep.mods.playernamemodifier.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import redstonedubstep.mods.playernamemodifier.PlayerNameModifier;

@Mixin(Player.class)
public class PlayerMixin {
	//Fabric: Decorates the player's display name using this mod's configuration before team information is appended onto it, and returns the decorated name
	@WrapOperation(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getName()Lnet/minecraft/network/chat/Component;"))
	private Component playernamemodifier$modifyOriginalPlayerName(Player player, Operation<Component> original) {
		return PlayerNameModifier.onPlayerDisplayName(player, original.call(player));
	}
}
