package com.jdz.vmd.api.enums;

public enum MetaTileEntityIDs {

    VoidMinerEX1Controller(31763),
    VoidMinerEX2Controller(26764),
    VoidMinerEX3Controller(26765),
    VoidMinerEX4Controller(26766),

    // The original LV-IV controller stays at 31763 for existing saves.
    VoidMinerLVController(31764),
    VoidMinerMVController(31765),
    VoidMinerHVController(31766),
    VoidMinerEVController(31767),
    VoidMinerIVController(31768);

    public final int ID;

    MetaTileEntityIDs(int ID) {
        this.ID = ID;
    }
}
