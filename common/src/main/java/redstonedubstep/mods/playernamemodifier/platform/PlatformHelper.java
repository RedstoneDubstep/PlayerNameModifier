package redstonedubstep.mods.playernamemodifier.platform;

import java.util.ServiceLoader;

import net.minecraft.server.level.ServerPlayer;
import redstonedubstep.mods.playernamemodifier.PlayerNameModifier;

// Service loaders are a built-in Java feature that allow us to locate implementations of an interface that vary from one
// environment to another. In the context of MultiLoader we use this feature to access a mock API in the common code that
// is swapped out for the platform specific implementation at runtime.
public abstract class PlatformHelper {
    public static final PlatformHelper INSTANCE = load(PlatformHelper.class);

    // This code is used to load a service for the current environment. Your implementation of the service must be defined
    // manually by including a text file in META-INF/services named with the fully qualified class name of the service.
    // Inside the file you should write the fully qualified class name of the implementation to load for the platform. For
    // example our file on Forge points to ForgePlatformHelper while Fabric points to FabricPlatformHelper.
    public static <T> T load(Class<T> clazz) {
        T loadedService = ServiceLoader.load(clazz).findFirst().orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));

        PlayerNameModifier.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }

    public abstract void refreshDisplayName(ServerPlayer player);

    public abstract void refreshTabListName(ServerPlayer player);
}
