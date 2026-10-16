package com.jdz.vmd.common.loaders;

import gregtech.api.enums.ItemList;
import gregtech.api.util.GTModHandler;

public class RecipeLoader {

    public static void run() {
        GTModHandler
            .addShapelessCraftingRecipe(ItemRegistry.VoidMinerEX1, new Object[] { ItemList.Machine_LV_Miner.get(1) });
    }
}
