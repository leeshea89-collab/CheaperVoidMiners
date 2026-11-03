package com.jdz.vmd.nei;

import net.minecraft.item.ItemStack;

import com.jdz.vmd.Tags;
import com.jdz.vmd.common.loaders.ItemRegistry;
import com.jdz.vmd.common.tileentities.MTETieredVoidMiner.Tier;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

/** Found by NEI by its name (NEI...Config); only loaded when NEI is installed. */
@SuppressWarnings("unused")
public class NEICvmConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        PlanetFluidsHandler handler = new PlanetFluidsHandler();
        API.registerRecipeHandler(handler);
        API.registerUsageHandler(handler);
        for (Tier tier : Tier.values()) {
            ItemStack drill = ItemRegistry.fluidDrill(tier);
            if (drill != null) API.addRecipeCatalyst(drill.copy(), handler);
        }
    }

    @Override
    public String getName() {
        return "Cheaper Void Miners";
    }

    @Override
    public String getVersion() {
        return Tags.VERSION;
    }
}
