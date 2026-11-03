package com.jdz.vmd;

import net.minecraftforge.common.MinecraftForge;

import com.jdz.vmd.client.FluidDrillHud;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        FluidDrillHud hud = new FluidDrillHud();
        MinecraftForge.EVENT_BUS.register(hud);
        FMLCommonHandler.instance()
            .bus()
            .register(hud);
    }
}
