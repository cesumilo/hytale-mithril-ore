package dev.cesumilo.plugin;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;

/**
 * The main entry point for the Mithril Ore plugin.
 * <p>
 * This class is responsible for initializing the plugin and registering the
 * custom {@link MithrilWorldGenProvider} into the Hytale server's World
 * Generation registry.
 * By registering with {@link Priority#NORMAL}, we ensure our provider is
 * available
 * for selection in the world configuration.
 * </p>
 */
public class MithrilOre extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    /**
     * Standard plugin constructor.
     * 
     * @param init The initialization context provided by the Hytale server loader.
     */
    public MithrilOre(@Nonnull JavaPluginInit init) {
        super(init);
    }

    /**
     * Called during the server startup phase.
     * <p>
     * Use this method to register event listeners, commands, and custom systems
     * like WorldGen providers.
     * </p>
     */
    @Override
    protected void setup() {
        LOGGER.atInfo().log("Initializing MithrilOre Plugin: " + this.getName());

        // Register the custom WorldGen provider so it can be used in
        // worlds/*/config.json
        // or selected during server setup.
        IWorldGenProvider.CODEC.register(
                Priority.NORMAL,
                "MithrilOre",
                MithrilWorldGenProvider.class,
                MithrilWorldGenProvider.CODEC);

        LOGGER.atInfo().log("Successfully registered 'MithrilOre' provider.");
    }
}
