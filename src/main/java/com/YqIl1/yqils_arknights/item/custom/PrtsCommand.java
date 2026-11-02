package com.YqIl1.yqils_arknights.item.custom;

// The entries of PRTS's radial menu, in menu order (clockwise from the top). The ordinal is what travels
// in PrtsCommandPacket, so append new entries at the end.
public enum PrtsCommand {
    ATTACK("attack"),  // everyone attacks the entity under the crosshair
    STAY("stay"),      // hold position
    FOLLOW("follow"),  // cancel orders, back to following the owner
    MOVE("move"),      // walk to the block under the crosshair, then hold there
    SKILLS("skills");  // the old PRTS: activate every ready skill item in the inventory

    public final String id;

    PrtsCommand(String id) {
        this.id = id;
    }

    // Needs an entity under the crosshair / a block under the crosshair to be issued.
    public boolean needsTarget() {
        return this == ATTACK;
    }

    public boolean needsPos() {
        return this == MOVE;
    }

    public static PrtsCommand byOrdinal(int ordinal) {
        PrtsCommand[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : null;
    }
}
