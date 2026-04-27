package com.rts.game.entities;

public enum EntityType {
    // Buildings (width, height, maxHealth, mineralCost, gasCost, buildTimeSeconds, isBuilding)
    COMMAND_CENTER(60, 60, 1500, 400, 0, 60, true),
    BARRACKS(40, 40, 1000, 150, 0, 45, true),
    REFINERY(35, 35, 750, 100, 0, 30, true),
    SUPPLY_DEPOT(25, 25, 500, 100, 0, 30, true),
    
    // Units (width, height, maxHealth, mineralCost, gasCost, buildTimeSeconds, isBuilding)
    SCV(12, 12, 60, 50, 0, 15, false),
    MARINE(10, 10, 40, 50, 0, 12, false),
    TANK(18, 18, 150, 150, 100, 30, false),
    
    // Resources
    MINERAL_PATCH(20, 20, 0, 0, 0, 0, true),
    VESPENE_GEYSER(30, 30, 0, 0, 0, 0, true);
    
    private final int width;
    private final int height;
    private final int maxHealth;
    private final int mineralCost;
    private final int gasCost;
    private final int buildTime;
    private final boolean isBuilding;
    
    EntityType(int width, int height, int maxHealth, int mineralCost, int gasCost, int buildTime, boolean isBuilding) {
        this.width = width;
        this.height = height;
        this.maxHealth = maxHealth;
        this.mineralCost = mineralCost;
        this.gasCost = gasCost;
        this.buildTime = buildTime;
        this.isBuilding = isBuilding;
    }
    
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getMaxHealth() { return maxHealth; }
    public int getMineralCost() { return mineralCost; }
    public int getGasCost() { return gasCost; }
    public int getBuildTime() { return buildTime; }
    public boolean isBuilding() { return isBuilding; }
}
