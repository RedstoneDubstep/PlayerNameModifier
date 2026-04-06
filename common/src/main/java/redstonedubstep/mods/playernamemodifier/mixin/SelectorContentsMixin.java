package redstonedubstep.mods.playernamemodifier.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ResolutionContext;
import net.minecraft.network.chat.contents.SelectorContents;
import redstonedubstep.mods.playernamemodifier.PlayerNameModifier;

@Mixin(SelectorContents.class)
public class SelectorContentsMixin {
	//Prevents selector contents in name modifying patterns from working, which fixes infinite recursion if a player's display name hasn't been initialized yet
	@Inject(method = "resolve", at = @At("HEAD"), cancellable = true)
	private void playernamemodifier$onResolve(ResolutionContext context, int recursionDepth, CallbackInfoReturnable<MutableComponent> callbackInfo) {
		if (PlayerNameModifier.refreshingDisplayNames)
			callbackInfo.setReturnValue(Component.empty());
	}
}
