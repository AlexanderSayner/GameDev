package com.rts.entities;

public enum BuildingType {
    COMMAND_CENTER(400, 0, 1500, 100, 80, true),
    BARRACKS(150, 0, 600, 50, 40, false),
    REFINERY(75, 0, 400, 30, 30, false),
    SUPPLY_DEPOT(100, 0, 300, 25, 25, false);
    
    private final int mineralCost;
    private final int gasCost;
    private final int health;
    private final int size;
    private final int buildTime;
    private final boolean producesSupply;
    
    BuildingType(int mineralCost, int gasCost, int health, int size, int buildTime, boolean producesSupply) {
        this.mineralCost = mineralCost;
        this.gasCost = gasCost;
        this.health = health;
        this.size = size;
        this.buildTime = buildTime;
        this.producesSupply = producesSupply;
    }
    
    public int getMineralCost() { return mineralCost; }
    public int getGasCost() { return gasCost; }
    public int getHealth() { return health; }
    public int getSize() { return size; }
    public int getBuildTime() { return buildTime; }
    public boolean producesSupply() { return producesSupply; }
}
