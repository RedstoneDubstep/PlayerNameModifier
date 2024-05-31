package redstonedubstep.mods.playernamemodifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.brigadier.StringReader;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.ParserUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber
public class ModifierEventListener {
	private static final List<String> SCHEDULED_REPEATS = new ArrayList<>();

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Pre event) {
		List<String> scheduledRepeats = new ArrayList<>(SCHEDULED_REPEATS);

		scheduledRepeats.forEach(s -> {
			Player player = event.getServer().getPlayerList().getPlayerByName(s);

			if (player != null)
				player.refreshDisplayName();
		});
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onPlayerName(PlayerEvent.NameFormat event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;
		//The logic of ignoring, re-requesting and using the name format event is required, because the component pattern can reference back to the player's prior display name (e.g. via selector components such as @s)
		//To prevent the display name from consequently getting longer every time it refreshes, we need to run the refreshing once without our modification (to get a "clean" display name).
		//Only after such a "clean" display name refreshing (and after a delay of about 1 tick) we refresh the display name again and apply our patterns, so our selectors resolve to the "clean" display name that was computed in the first run.
		else if (!SCHEDULED_REPEATS.contains(player.getGameProfile().getName())) {
			SCHEDULED_REPEATS.add(player.getGameProfile().getName());
			return;
		}

		Component modifiedName = modifyName(player, event.getDisplayname(), getPatterns(player, false));

		SCHEDULED_REPEATS.remove(player.getGameProfile().getName());
		event.setDisplayname(modifiedName);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onPlayerTabListName(PlayerEvent.TabListNameFormat event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		Component tabDisplayName = event.getDisplayName();
		Component modifiedName = modifyName(player, tabDisplayName != null ? tabDisplayName : Component.literal(player.getGameProfile().getName()), getPatterns(player, true));

		//Display names and tab list names have different logic regarding teams: For the tab list name, the team prefix/suffix/color doesn't automatically get applied, so we have to do it ourselves, while for the display name, the team components automatically get appended to our modified component.
		event.setDisplayName(PlayerTeam.formatNameForTeam(player.getTeam(), modifiedName));
	}

	private static Component modifyName(ServerPlayer player, Component oldDisplayName, List<String> patternStack) {
		if (patternStack.isEmpty())
			return oldDisplayName;

		String pattern = patternStack.get(patternStack.size() - 1);

		//We recursively chop up the pattern stack here in a way that makes the first entry of the stack apply first, which is used as insertion for the second entry, etc.
		if (patternStack.size() > 1) {
			patternStack.remove(pattern);
			oldDisplayName = modifyName(player, oldDisplayName, patternStack);
		}

		Component modifiedName;
		CommandSourceStack stack = new CommandSourceStack(player, player.position(), player.getRotationVector(), (ServerLevel)player.level(), 4, player.getName().getString(), oldDisplayName, player.level().getServer(), player);

		try {
			modifiedName = ComponentUtils.updateForEntity(stack, ParserUtils.parseJson(((ServerPlayer) player).server.registryAccess(), new StringReader(pattern), ComponentSerialization.CODEC), player, 0);
		}
		catch (Exception e) {
			PlayerNameModifier.LOGGER.warn(e);
			return oldDisplayName;
		}

		List<Component> nameParts = modifiedName.getSiblings();
		MutableComponent playerInsertedName = Component.empty();

		nameParts.add(modifiedName.plainCopy().withStyle(modifiedName.getStyle()));

		for (Component sibling : nameParts) {
			if (sibling.getContents() instanceof PlainTextContents.LiteralContents contents && contents.text().contains("%player")) {
				String[] splitString = contents.text().split("%player", -1); //-1 so empty trailing strings are preserved
				StringBuilder resultString = new StringBuilder(splitString[0]);

				for (int i = 1; i < splitString.length; i++) {
					resultString.append(oldDisplayName.getString());
					resultString.append(splitString[i]);
				}

				sibling = Component.literal(resultString.toString()).withStyle(sibling.getStyle());
			}

			playerInsertedName.append(sibling);
		}

		return playerInsertedName;
	}

	public static List<String> getPatterns(Player player, boolean tabListName) {
		List<String> patternStack = new ArrayList<>();

		for (Map.Entry<List<String>, Pair<String, String>> modifierEntry : ModifierConfig.CONFIG.replacementMap.entrySet()) {
			for (String tag : modifierEntry.getKey()) {
				if (player.getTags().contains(tag))
					patternStack.add("[" + (tabListName ? modifierEntry.getValue().getRight() : modifierEntry.getValue().getLeft()) + "]");
			}
		}

		return patternStack;
	}
}
