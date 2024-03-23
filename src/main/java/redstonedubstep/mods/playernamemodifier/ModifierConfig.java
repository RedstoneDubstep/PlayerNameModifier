package redstonedubstep.mods.playernamemodifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.electronwill.nightconfig.core.AbstractCommentedConfig;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.DEDICATED_SERVER)
public class ModifierConfig {
	public static final ModConfigSpec SERVER_SPEC;
	public static final Config CONFIG;

	static {
		final Pair<Config, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Config::new);

		SERVER_SPEC = specPair.getRight();
		CONFIG = specPair.getLeft();
	}

	public static class Config {
		public ConfigValue<List<AbstractCommentedConfig>> playerNameModifiers;
		public HashMap<List<String>, Pair<String, String>> replacementMap;

		Config(ModConfigSpec.Builder builder) {
			playerNameModifiers = builder
					.comment(" --- PlayerNameModifier Config File --- ",
							"A list of all player name modifiers that should apply to players with the given entity tag.",
							"Entity tags can be added to selected players via the command \"/tag <targets> add <tag>\"",
							"Multiple entries can be added to this config, to support different player name modifiers for different tags. If the tag filter of more than one entry matches a player, all matching modifier patterns will be applied.",
							"An individual config entry firstly needs a list of \"tags\", which determines the tags that a player needs to have their name modified.",
							"After the tag list, the modifier patterns for the player's display and tab list name are specified, via \"displayName\" and \"tabListName\" respectively.",
							"These modifier patterns can be a simple string literal (encased in escaped quotes, like \\\"this\\\"), but also support JSON component definitions (if you happen to know what that means).",
							"Important: By default, the name of the player does not automatically get appended to the modifier pattern. You need to insert the variable \"%player\" into your pattern, which will be replaced with the player's unmodified name.",
							"Also of note: Without other mods, the player display and tab list name of a player only gets updated when they join the server. If you want to manually update these names, e.g. after you reassigned some tags, use the command \"/refreshplayername <players>\".",
							"",
							"An example config which adds some rank information to player names would look like this:",
							"[[playerNameModifiers]]",
							"	tags = [\"admin\", \"moderator\"]",
							"	displayName = \"\\\"Admin %player says:\\\"\"",
							"	tabListName = \"\\\"[Admin] %player\\\"\"",
							"",
							"[[playerNameModifiers]]",
							"	tags = [\"badperson\"]",
							"	displayName = \"\\\"Do not listen to %player\\\"\"",
							"	tabListName = \"\\\"[Bad person] %player\\\"\"")
					.define("playerNameModifiers", new ArrayList<>());
		}
	}

	@SubscribeEvent
	public static void onConfigUpdate(ModConfigEvent event) {
		List<AbstractCommentedConfig> configList = CONFIG.playerNameModifiers.get();
		HashMap<List<String>, Pair<String, String>> replacementMap = new LinkedHashMap<>();

		for (AbstractCommentedConfig config : configList) {
			List<String> tags = config.get("tags");
			String displayName = config.get("displayName");
			String tabListName = config.get("tabListName");

			replacementMap.put(tags, Pair.of(displayName, tabListName));
		}

		CONFIG.replacementMap = replacementMap;
	}
}
