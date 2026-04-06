package redstonedubstep.mods.playernamemodifier.platform;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;

public class FabricPlatformHelper extends PlatformHelper {
	@Override
	public void refreshDisplayName(ServerPlayer player) {
		//no-op, since recomputation is automatic and nothing else needs to be notified when the name updates
	}

	@Override
	public void refreshTabListName(ServerPlayer player) {
		player.level().getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player));
	}
}
