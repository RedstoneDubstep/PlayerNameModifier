package redstonedubstep.mods.playernamemodifier.platform;

import net.minecraft.server.level.ServerPlayer;

public class NeoForgePlatformHelper extends PlatformHelper {
	@Override
	public void refreshDisplayName(ServerPlayer player) {
		player.refreshDisplayName();
	}

	@Override
	public void refreshTabListName(ServerPlayer player) {
		player.refreshTabListName();
	}
}
