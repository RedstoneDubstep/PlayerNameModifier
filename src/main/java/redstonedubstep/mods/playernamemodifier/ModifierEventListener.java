package redstonedubstep.mods.playernamemodifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.brigadier.StringReader;

import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.StyleArgument;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.SnbtGrammar;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ResolutionContext;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.parsing.packrat.commands.CommandArgumentParser;
import net.minecraft.util.parsing.packrat.commands.Grammar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber
public class ModifierEventListener {
	private static final Grammar<Tag> TAG_PARSER = SnbtGrammar.createParser(NbtOps.INSTANCE);
	private static final CommandArgumentParser<Component> COMPONENT_PARSER = TAG_PARSER.withCodec(NbtOps.INSTANCE, TAG_PARSER, ComponentSerialization.CODEC, ComponentArgument.ERROR_INVALID_COMPONENT);
	private static final CommandArgumentParser<Style> STYLE_PARSER = TAG_PARSER.withCodec(NbtOps.INSTANCE, TAG_PARSER, Style.Serializer.CODEC, StyleArgument.ERROR_INVALID_STYLE);
	private static final List<String> SCHEDULED_REPEATS = new ArrayList<>();

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onPlayerName(PlayerEvent.NameFormat event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		if (event.getDisplayname() instanceof MutableComponent displayName)
			event.setDisplayname(modifyName(player, displayName, getPatterns(player, false)));
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onPlayerTabListName(PlayerEvent.TabListNameFormat event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		Component oldTabName = event.getDisplayName() != null ? event.getDisplayName() : Component.literal(player.getGameProfile().name());

		if (oldTabName instanceof MutableComponent tabDisplayName) {
			//Display names and tab list names have different logic regarding teams: For the tab list name, the team prefix/suffix/color doesn't automatically get applied, so we have to do it ourselves, while for the display name, the team components automatically get appended to our modified component.
			event.setDisplayName(PlayerTeam.formatNameForTeam(player.getTeam(), modifyName(player, tabDisplayName, getPatterns(player, true))));
		}
	}

	private static MutableComponent modifyName(ServerPlayer player, MutableComponent nameToDecorate, List<NameFormatPattern> patternStack) {
		if (patternStack.isEmpty()) //Should never happen, but just in case
			return nameToDecorate;

		NameFormatPattern pattern = patternStack.getLast();

		//We recursively chop up the pattern stack here in a way that makes the first entry of the stack apply first, which is used as insertion for the second entry, etc.
		if (patternStack.size() > 1) {
			patternStack.remove(pattern);
			nameToDecorate = modifyName(player, nameToDecorate, patternStack);
		}

		Component namePrefix, nameSuffix;
		Style patternStyle;
		ResolutionContext resolutionContext = ResolutionContext.builder().withEntityOverride(player).build(); //Not setting a CommandSourceStack here is unorthodox, but fixes all recursion issues due to no level being present to check

		try {
			namePrefix = pattern.resolvePrefix(resolutionContext);
			nameSuffix = pattern.resolveSuffix(resolutionContext);
			patternStyle = pattern.resolveStyle();
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
			for (String tag : modifierEntry.getKey()) {
				if (player.entityTags().contains(tag)) {
					patternStack.add(tabListName ? modifierEntry.getValue().getRight() : modifierEntry.getValue().getLeft());
					break;
				}
			}
		}

		return patternStack;
	}

	public record NameFormatPattern(String prefix, String suffix, String style) {
		public Component resolvePrefix(ResolutionContext ctx) throws Exception {
			if (prefix == null)
				return Component.empty();

			return ComponentUtils.resolve(ctx, COMPONENT_PARSER.parseForCommands(new StringReader(prefix)));
		}

		public Component resolveSuffix(ResolutionContext ctx) throws Exception {
			if (suffix == null)
				return Component.empty();

			return ComponentUtils.resolve(ctx, COMPONENT_PARSER.parseForCommands(new StringReader(suffix)));
		}

		public Style resolveStyle() throws Exception {
			if (style == null)
				return null;

			return STYLE_PARSER.parseForCommands(new StringReader(style));
		}
	}
}
