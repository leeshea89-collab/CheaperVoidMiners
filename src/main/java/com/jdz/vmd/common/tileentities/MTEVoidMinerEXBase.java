package com.jdz.vmd.common.tileentities;

import bwcrossmod.galacticgreg.MTEVoidMinerBase;
import com.google.common.collect.ImmutableList;
import gregtech.api.interfaces.IHatchElement;
import gregtech.common.tileentities.machines.multi.MTEDrillerBase;

import java.util.List;

import static gregtech.api.enums.HatchElement.*;

public abstract class MTEVoidMinerEXBase<T extends MTEVoidMinerEXBase<T>> extends MTEVoidMinerBase<T> {

    public MTEVoidMinerEXBase(int aID, String aName, String aNameRegional, int multiplier) {
        super(aID, aName, aNameRegional, multiplier);
    }

    public MTEVoidMinerEXBase(String aName, int tier) {
        super(aName, tier);
    }

    // @Override
    // protected void setElectricityStats() {
    //     try {
    //         this.mEUt = this.isPickingPipes ? 30 : Math.toIntExact(GTValues.V[this.getMinTier()]);
    //     } catch (ArithmeticException e) {
    //         e.printStackTrace();
    //         this.mEUt = Integer.MAX_VALUE - 7;
    //     }
    //     this.mOutputItems = new ItemStack[0];
    //     this.mProgresstime = 0;
    //     this.mMaxProgresstime = 10;
    //     this.mEfficiency = this.getCurrentEfficiency(null);
    //     this.mEfficiencyIncrease = 10000;
    //     this.mEUt = this.mEUt > 0 ? -this.mEUt : this.mEUt;
    // }

    @Override
    protected List<IHatchElement<? super MTEDrillerBase>> getAllowedHatches() {
        return ImmutableList.of(InputBus, OutputBus, Maintenance, Energy);
    }
}
