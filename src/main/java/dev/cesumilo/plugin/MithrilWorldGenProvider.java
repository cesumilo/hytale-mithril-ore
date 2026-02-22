package dev.cesumilo.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.LongPredicate;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec.Builder;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.IWorldGen;
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenLoadException;
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenTimingsCollector;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import com.hypixel.hytale.server.worldgen.HytaleWorldGenProvider;

/**
 * A custom World Generation Provider that intercepts chunk generation to
 * replace
 * vanilla ores with Mithril based on configurable probabilities.
 */
public class MithrilWorldGenProvider implements IWorldGenProvider, IWorldGen {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final BuilderCodec<MithrilWorldGenProvider> CODEC = ((Builder<MithrilWorldGenProvider>) BuilderCodec
            .builder(MithrilWorldGenProvider.class, MithrilWorldGenProvider::new)
            .documentation("Configuration for the Mithril Ore Injector"))
            .build();

    // -- Regex Patterns --
    private static final Pattern ORE_COPPER_PATTERN = Pattern.compile("Ore_Copper.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern ORE_IRON_PATTERN = Pattern.compile("Ore_Iron.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern ORE_GOLD_PATTERN = Pattern.compile("Ore_Gold.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern ORE_THORIUM_PATTERN = Pattern.compile("Ore_Thorium.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern ORE_COBALT_PATTERN = Pattern.compile("Ore_Cobalt.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern ORE_ADAMANTITE_PATTERN = Pattern.compile("Ore_Adamantite.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern ORE_MITHRIL_PATTERN = Pattern.compile("Ore_Mithril.*", Pattern.CASE_INSENSITIVE);

    // -- Probabilities --
    private static final double ORE_COPPER_CHANCE = 0.0001;
    private static final double ORE_IRON_CHANCE = 0.0005;
    private static final double ORE_GOLD_CHANCE = 0.01;
    private static final double ORE_THORIUM_CHANCE = 0.001;
    private static final double ORE_COBALT_CHANCE = 0.01;
    private static final double ORE_ADAMANTITE_CHANCE = 0.08;

    private IWorldGen originalWorldGenProvider;

    /**
     * A unified map for O(1) lookups.
     * Key: Vanilla Block ID
     * Value: The probability chance to replace this block with Mithril.
     */
    private final Map<Integer, Double> oreReplacementMap = new HashMap<>();

    /**
     * Cached array of Mithril Block IDs for fast random access.
     */
    private Integer[] mithrilBlockIds = new Integer[0];

    @Override
    public CompletableFuture<GeneratedChunk> generate(int seed, long index, int x, int z, LongPredicate stillNeeded) {
        // Create deterministic random based on seed and coordinates
        Random random = new Random((long) seed ^ (long) x * 31L + (long) z);

        return this.originalWorldGenProvider.generate(seed, index, x, z, stillNeeded)
                .thenApply((GeneratedChunk chunk) -> {
                    // Fail-safe: If no Mithril blocks were found during load, return early
                    if (chunk == null || mithrilBlockIds.length == 0) {
                        return chunk;
                    }

                    GeneratedBlockChunk blockChunk = chunk.getBlockChunk();

                    // Iterate over the chunk volume (32x32x320)
                    for (int bx = 0; bx < 32; ++bx) {
                        for (int bz = 0; bz < 32; ++bz) {
                            for (int by = 0; by < 320; ++by) {
                                int blockId = blockChunk.getBlock(bx, by, bz);

                                // Fast Lookup: Is this block in our replacement list?
                                Double chance = oreReplacementMap.get(blockId);

                                if (chance != null) {
                                    // It is an ore, check probability
                                    if (random.nextDouble() < chance) {
                                        int rotation = blockChunk.getRotationIndex(bx, by, bz);

                                        // Pick a random Mithril variant from the cached array
                                        int mithIdx = random.nextInt(mithrilBlockIds.length);
                                        blockChunk.setBlock(bx, by, bz, mithrilBlockIds[mithIdx], rotation, 0);
                                    }
                                }
                            }
                        }
                    }

                    return chunk;
                });
    }

    @Override
    public IWorldGen getGenerator() throws WorldGenLoadException {
        if (this.originalWorldGenProvider == null) {
            this.originalWorldGenProvider = new HytaleWorldGenProvider().getGenerator();
            this.loadAssets();
        }
        return this;
    }

    /**
     * Scans the BlockType registry to populate the replacement map and mithril ID
     * list.
     * This runs once during initialization.
     */
    private void loadAssets() {
        var assetsMap = BlockType.getAssetMap();
        var count = assetsMap.getAssetCount();

        List<Integer> tempMithrilList = new ArrayList<>();

        for (var i = 0; i < count; i++) {
            BlockType block = BlockType.getAssetMap().getAsset(i);
            if (block != null) {
                String id = block.getId();

                // Populate the unified replacement map
                if (ORE_COPPER_PATTERN.matcher(id).find()) {
                    oreReplacementMap.put(i, ORE_COPPER_CHANCE);
                } else if (ORE_IRON_PATTERN.matcher(id).find()) {
                    oreReplacementMap.put(i, ORE_IRON_CHANCE);
                } else if (ORE_GOLD_PATTERN.matcher(id).find()) {
                    oreReplacementMap.put(i, ORE_GOLD_CHANCE);
                } else if (ORE_THORIUM_PATTERN.matcher(id).find()) {
                    oreReplacementMap.put(i, ORE_THORIUM_CHANCE);
                } else if (ORE_COBALT_PATTERN.matcher(id).find()) {
                    oreReplacementMap.put(i, ORE_COBALT_CHANCE);
                } else if (ORE_ADAMANTITE_PATTERN.matcher(id).find()) {
                    oreReplacementMap.put(i, ORE_ADAMANTITE_CHANCE);
                }

                // Collect Mithril IDs
                else if (ORE_MITHRIL_PATTERN.matcher(id).find()) {
                    tempMithrilList.add(i);
                }
            }
        }

        // Convert List to Array for faster access during generation
        this.mithrilBlockIds = tempMithrilList.toArray(new Integer[0]);

        LOGGER.atInfo().log("Mithril WorldGen initialized.");
        LOGGER.atInfo().log("Found " + oreReplacementMap.size() + " vanilla ores to potentially replace.");
        LOGGER.atInfo().log("Found " + mithrilBlockIds.length + " mithril block variants.");
    }

    // --- Delegation Methods ---

    @Override
    @Deprecated
    public Transform[] getSpawnPoints(int arg0) {
        if (this.originalWorldGenProvider == null) {
            return null;
        }
        return this.originalWorldGenProvider.getSpawnPoints(arg0);
    }

    @Override
    @Nullable
    public WorldGenTimingsCollector getTimings() {
        if (this.originalWorldGenProvider == null) {
            return null;
        }
        return this.originalWorldGenProvider.getTimings();
    }
}
