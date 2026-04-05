package redstonedubstep.mods.playernamemodifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import redstonedubstep.mods.playernamemodifier.ModifierEventListener.NameFormatPattern;

@EventBusSubscriber(value = Dist.DEDICATED_SERVER)
public class ModifierConfig {
	public static final ModConfigSpec SERVER_SPEC;
	public static final Config CONFIG;

	static {
		final Pair<Config, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Config::new);

		SERVER_SPEC = specPair.getRight();
		CONFIG = specPair.getLeft();
	}

	public static class Config {
		public ConfigValue<List<SynchronizedConfig>> playerNameModifiers;
		public HashMap<List<String>, Pair<NameFormatPattern, NameFormatPattern>> replacementMap;

		Config(ModConfigSpec.Builder builder) {
			builder.comment(" --- PlayerNameModifier Config File --- ",
					"This file contains a list of all player name modifiers that should apply to players with the given entity tag.",
					"Entity tags can be added to selected players via the command \"/tag <targets> add <tag>\".",
					"Multiple entries can be added to this config, to support different player name modifiers for different tags. ",
					"If the tag filter of more than one entry matches a player, all matching modifier patterns will be applied, with the topmost pattern being applied first.",
					"",
					"- Each pattern entry needs a list of \"tags\", which determines the entity tags that a player needs for the pattern to be applied.",
					"- After the tag list, the modifiers for the player's display and tab list name are specified. All of these are optional, i.e. not all of them need to be defined for every pattern.",
					"- In the example of the player's display name (which is the one that shows up in chat), the modifier is defined by \"displayNamePrefix\", \"displayNameSuffix\", and \"displayNamePlayerStyle\".",
					"The prefix and suffix entries will be added before and after the player name, respectively.",
					"Both of these entries are parsed as a text component. They can be a simple string literal (surrounded by an extra set of quotes; see below for an example), or a proper SNBT-defined component.",
					"Please note that scoreboard, NBT and selector text component types are not supported due to recursion issues.",
					"- The \"displayNameStyle\" entry is parsed as a style object. It can be treated like an SBT text component where the \"text\" property is ignored. See below for examples.",
					"After applying the prefix and suffix to the player name, this style will be applied to the whole resulting component.",
					"",
					"For formatting a player's tab list name, separate modifiers exist in the form of \"tabListPrefix\", \"tabListSuffix\", and \"tabListPlayerStyle\". They work analoguous to the modifiers described above.",
					"",
					"Note: By default, the player display and tab list name of a player only gets updated when they join the server. If you want to manually update these names, e.g. after you reassigned some tags, use the command \"/refreshplayername <players>\".",
					"",
					"An example config which adds some rank information to player names would look like this:",
					"[[playerNameModifiers]]",
					"	tags = [\"admin\", \"moderator\"]",
					"	displayNamePrefix = '\"Admin \"'",
					"	displayNameSuffix = '\" §osays:\"'",
					"	displayNameStyle = '{bold:true, color:gold}'",
					"	tabListPrefix = '\"[Admin] \"'",
					"	tabListSuffix = '\" [A]\"'",
					"	tabListStyle = '{color: yellow}'",
					"",
					"[[playerNameModifiers]]",
					"	tags = [\"badperson\"]",
					"	displayNamePrefix = '[\"\", {text: \"Do not \", color:dark_red, underlined:true, hover_event:{action: show_text, value: \"I really mean it!\"}}, {text: \"listen to \", color: red}]'",
					"	displayNameSuffix = '{text: \"...\", color:dark_gray}'")
					.define("_comment", "");

			playerNameModifiers = builder
					.comment("The actual config containing the list described above.")
					.define("playerNameModifiers", new ArrayList<>());
		}
	}

	@SubscribeEvent
	public static void onConfigUpdate(ModConfigEvent event) {
		if (event instanceof ModConfigEvent.Unloading)
			return;

		List<SynchronizedConfig> configList = CONFIG.playerNameModifiers.get();
		HashMap<List<String>, Pair<NameFormatPattern, NameFormatPattern>> replacementMap = new LinkedHashMap<>();

		for (SynchronizedConfig config : configList) {
			List<String> tags = config.get("tags");
			String displayNamePrefix = config.get("displayNamePrefix");
			String displayNameSuffix = config.get("displayNameSuffix");
			String displayNameStyle = config.get("displayNameStyle");
			String tabListPrefix = config.get("tabListPrefix");
			String tabListSuffix = config.get("tabListSuffix");
			String tabListStyle = config.get("tabListStyle");

			replacementMap.put(tags, Pair.of(new NameFormatPattern(displayNamePrefix, displayNameSuffix, displayNameStyle), new NameFormatPattern(tabListPrefix, tabListSuffix, tabListStyle)));
		}

		CONFIG.replacementMap = replacementMap;
	}
}
