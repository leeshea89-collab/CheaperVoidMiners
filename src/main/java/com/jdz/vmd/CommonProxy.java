package com.jdz.vmd;

import com.jdz.vmd.common.loaders.ItemRegistry;
import com.jdz.vmd.common.loaders.RecipeLoader;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        ItemRegistry.run();
    }

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        RecipeLoader.run();
    }

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        // Every dimension is registered by now, so rebuild the planet -> fluid table and log it.
        com.jdz.vmd.common.fluids.PlanetFluids.reset();
        try {
            int planets = 0;
            for (String abbr : com.jdz.vmd.common.fluids.PlanetFluids.planetsWithFluids()) {
                planets++;
                StringBuilder sb = new StringBuilder("[CheaperVoidMiners] Void Fluid Drill ").append(abbr)
                    .append(':');
                for (com.jdz.vmd.common.fluids.PlanetFluids.Entry e : com.jdz.vmd.common.fluids.PlanetFluids
                    .forAbbr(abbr)) {
                    sb.append(' ')
                        .append(e.registry)
                        .append('=')
                        .append(e.lens() == null ? "none" : e.lens().mName)
                        .append(" (")
                        .append(Math.round(e.share * 100))
                        .append("%)");
                }
                System.out.println(sb);
            }
            System.out.println("[CheaperVoidMiners] Void Fluid Drill planets with fluids: " + planets);
            System.out.println("[CheaperVoidMiners] Void Fluid Drill planet lock: "
                + com.jdz.vmd.common.tileentities.MTEVoidFluidDrill.gatingText() + "; LV drill on an LuV hatch pumps x"
                + Math.pow(Config.energyRateMultiplier, 5) + "; T0 needs voltage "
                + com.jdz.vmd.common.tileentities.MTEVoidFluidDrill.voltageFor(0));
        } catch (RuntimeException e) {
            System.out.println("[CheaperVoidMiners] Void Fluid Drill fluid table failed: " + e);
            e.printStackTrace();
        }
    }
}
