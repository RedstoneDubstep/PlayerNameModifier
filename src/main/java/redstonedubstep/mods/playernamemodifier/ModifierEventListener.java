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
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber
public class ModifierEventListener {
	public static boolean refreshingDisplayNames = false;

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onPlayerName(PlayerEvent.NameFormat event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		if (event.getDisplayname() instanceof MutableComponent displayName) {
			refreshingDisplayNames = true;
			event.setDisplayname(modifyName(player, displayName, getPatterns(player, false)));
			refreshingDisplayNames = false;
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onPlayerTabListName(PlayerEvent.TabListNameFormat event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		Component oldTabName = event.getDisplayName() != null ? event.getDisplayName() : Component.literal(player.getGameProfile().getName());

		if (oldTabName instanceof MutableComponent tabDisplayName) {
			refreshingDisplayNames = true;
			//Display names and tab list names have different logic regarding teams: For the tab list name, the team prefix/suffix/color doesn't automatically get applied, so we have to do it ourselves, while for the display name, the team components automatically get appended to our modified component.
			event.setDisplayName(PlayerTeam.formatNameForTeam(player.getTeam(), modifyName(player, tabDisplayName, getPatterns(player, true))));
			refreshingDisplayNames = false;
		}
	}

	private static MutableComponent modifyName(ServerPlayer player, MutableComponent nameToDecorate, List<NameFormatPattern> patternStack) {
		if (patternStack.isEmpty()) //Only the case if the player has no matching tags
			return nameToDecorate;

		NameFormatPattern pattern = patternStack.getLast();

		//We recursively chop up the pattern stack here in a way that makes the first entry of the stack apply first, which is used as insertion for the second entry, etc.
		if (patternStack.size() > 1) {
			patternStack.remove(pattern);
			nameToDecorate = modifyName(player, nameToDecorate, patternStack);
		}

		Component namePrefix, nameSuffix;
		Style patternStyle;
		CommandSourceStack stack = new CommandSourceStack(player, player.position(), player.getRotationVector(), (ServerLevel)player.level(), 4, player.getName().getString(), nameToDecorate, player.level().getServer(), player);

		try {
			namePrefix = pattern.resolvePrefix(stack, player);
			nameSuffix = pattern.resolveSuffix(stack, player);
			patternStyle = pattern.resolveStyle(player);
		}
		catch (Exception e) {
			PlayerNameModifier.LOGGER.warn(e);
			return nameToDecorate;
		}

		MutableComponent decoratedName = Component.empty().append(namePrefix).append(nameToDecorate).append(nameSuffix);

		if (patternStyle != null)
			decoratedName.setStyle(patternStyle);

		return decoratedName;
	}

	public static List<NameFormatPattern> getPatterns(Player player, boolean tabListName) {
		List<NameFormatPattern> patternStack = new ArrayList<>();

		for (Map.Entry<List<String>, Pair<NameFormatPattern, NameFormatPattern>> modifierEntry : ModifierConfig.CONFIG.replacementMap.entrySet()) {
			NameFormatPattern pattern = tabListName ? modifierEntry.getValue().getRight() : modifierEntry.getValue().getLeft();

			if (!pattern.isEmpty()) {
				for (String tag : modifierEntry.getKey()) {
					if (player.getTags().contains(tag)) {
						patternStack.add(pattern);
						break;
					}
				}
			}
		}

		return patternStack;
	}

	public record NameFormatPattern(String prefix, String suffix, String style) {
		public Component resolvePrefix(CommandSourceStack stack, ServerPlayer player) throws Exception {
			if (prefix == null)
				return Component.empty();

			return ComponentUtils.updateForEntity(stack, ParserUtils.parseJson(player.server.registryAccess(), new StringReader(prefix), ComponentSerialization.CODEC), player, 0);
		}

		public Component resolveSuffix(CommandSourceStack stack, ServerPlayer player) throws Exception {
			if (suffix == null)
				return Component.empty();

			return ComponentUtils.updateForEntity(stack, ParserUtils.parseJson(player.server.registryAccess(), new StringReader(suffix), ComponentSerialization.CODEC), player, 0);
		}

		public Style resolveStyle(ServerPlayer player) {
			if (style == null)
				return null;

			return ParserUtils.parseJson(player.server.registryAccess(), new StringReader(style), Style.Serializer.CODEC);
		}

		public boolean isEmpty() {
			return prefix == null && suffix == null && style == null;
		}
	}
}
