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
    /** GregTech machine id of the LV Void Fluid Drill (MV-IV follow). 0 = off. */
    public static int fluidDrillBaseId = 31769;
    /** Litres per second = fluid MaxAmount x factor, per drill tier LV-IV. */
    public static double[] fluidRateFactor = { 0.25, 0.5, 1.0, 2.0, 4.0 };
    /** Rate fraction when no lens is fitted (random fluid). */
    public static double noLensFactor = 0.5;
    /** Fluid drill planet lock by voltage: first voltage tier (2 = MV) and the highest planet tier per tier from it. */
    public static int fluidPlanetFirstVoltage = 2;
    public static int[] fluidPlanetMaxTier = { 2, 4, 6, 8, 10 };
    /** Rate multiplier per voltage tier the energy hatches are above the drill. */
    public static double energyRateMultiplier = 2.0;

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

        fluidDrillBaseId = configuration.getInt(
            "baseMachineId",
            "fluid_drill",
            fluidDrillBaseId,
            0,
            32000,
            "GregTech machine id of the LV Void Fluid Drill; MV-IV use the next four. 0 turns the drills off.");
        double[] rates = configuration
            .get(
                "fluid_drill",
                "rateFactor",
                fluidRateFactor,
                "Litres per second = the fluid's MaxAmount in GT's underground fluid config x this factor, for the LV, MV, HV, EV and IV drills.")
            .getDoubleList();
        if (rates.length == 5) fluidRateFactor = rates;
        noLensFactor = configuration
            .get(
                "fluid_drill",
                "noLensFactor",
                noLensFactor,
                "With no lens the drill pumps a random fluid of the planet at this fraction of the normal rate.")
            .getDouble();

        fluidPlanetFirstVoltage = configuration.getInt(
            "planetLockFirstVoltage",
            "fluid_drill",
            fluidPlanetFirstVoltage,
            0,
            9,
            "Planets are locked behind the voltage the drill runs at (its energy hatch tier). This is the first voltage tier that can drill anything: 1 LV, 2 MV, 3 HV...");
        int[] lock = configuration
            .get(
                "fluid_drill",
                "planetLockMaxTier",
                fluidPlanetMaxTier,
                "Highest planet tier (0-10) reachable at each voltage tier, starting at planetLockFirstVoltage. Default: MV T0-2, HV T3-4, EV T5-6, IV T7-8, LuV T9-10.")
            .getIntList();
        if (lock.length > 0) fluidPlanetMaxTier = lock;
        energyRateMultiplier = configuration
            .get(
                "fluid_drill",
                "energyRateMultiplier",
                energyRateMultiplier,
                "Each voltage tier the drill's energy hatches are above the drill's own tier multiplies the rate by this (and the drill uses that tier's power).")
            .getDouble();

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
