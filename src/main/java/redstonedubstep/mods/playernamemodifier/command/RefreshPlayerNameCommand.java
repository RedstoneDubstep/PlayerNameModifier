package redstonedubstep.mods.playernamemodifier.command;

import java.util.Collection;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class RefreshPlayerNameCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("refreshplayername")
				.requires(source -> source.hasPermission(2))
				.executes(ctx -> refreshPlayerName(ctx, List.of(ctx.getSource().getPlayerOrException())))
				.then(Commands.argument("players", EntityArgument.players())
						.executes(ctx -> refreshPlayerName(ctx, EntityArgument.getPlayers(ctx, "players")))));
	}

	private static int refreshPlayerName(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> players) {
		for (ServerPlayer player : players) {
			player.refreshDisplayName();
			player.refreshTabListName();
		}

		ctx.getSource().sendSuccess(() -> Component.literal("Updated display name of " + players.size() + " players"), true);
		return players.size();
	}
}
