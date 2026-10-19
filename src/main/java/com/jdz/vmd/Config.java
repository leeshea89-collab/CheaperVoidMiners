package com.jdz.vmd;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    /** When true, each Void Miner tier can only target planets up to a maximum rocket tier. */
    public static boolean planetTierGating = true;
    /** Highest planet tier (T0-T10) each miner may target: LV, MV, HV, EV, IV. */
    public static int[] maxPlanetTier = { 2, 4, 6, 8, 10 };
    /** Blocks produced per output run in Surface mode. */
    public static int surfaceBlocksPerRun = 4;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        planetTierGating = configuration.getBoolean(
            "planetTierGating",
            "planets",
            planetTierGating,
            "Limit which planet tiers each Void Miner can target with a planet block in its controller slot.");
        maxPlanetTier = configuration
            .get(
                "planets",
                "maxPlanetTier",
                maxPlanetTier,
                "Highest planet tier (0-10) allowed for the LV, MV, HV, EV and IV Void Miners, in that order.")
            .getIntList();
        if (maxPlanetTier.length != 5) maxPlanetTier = new int[] { 2, 4, 6, 8, 10 };
        surfaceBlocksPerRun = configuration.getInt(
            "surfaceBlocksPerRun",
            "planets",
            surfaceBlocksPerRun,
            1,
            64,
            "Blocks produced per output run in Surface mode.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
