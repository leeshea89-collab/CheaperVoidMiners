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

    public void init(FMLInitializationEvent event) {
        // Register the machine during the GregTech-supported load phase.
        ItemRegistry.run();
    }

    public void postInit(FMLPostInitializationEvent event) {
        // Register only the affordable LV recipe after all GT items exist.
        RecipeLoader.run();
    }

    public void serverStarting(FMLServerStartingEvent event) {}
}
