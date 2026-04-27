package com.rts.entities;

public class Building {
    private float x, y;
    private BuildingType type;
    private Team team;
    private int health;
    private float buildProgress;
    private boolean isComplete;
    
    private double productionTimer = 0;
    private UnitType producingUnit;
    
    public Building(float x, float y, BuildingType type, Team team) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.team = team;
        this.health = type.getHealth();
        this.buildProgress = 100; // Start complete for simplicity
        this.isComplete = true;
    }
    
    public void update(double deltaTime) {
        if (!isComplete) {
            buildProgress += (100.0f / type.getBuildTime()) * (float)deltaTime;
            if (buildProgress >= 100) {
                buildProgress = 100;
                isComplete = true;
            }
        }
        
        if (producingUnit != null && isComplete) {
            productionTimer -= deltaTime;
            if (productionTimer <= 0) {
                // Unit production complete - handled by GameWorld
                producingUnit = null;
            }
        }
    }
    
    public void startProduction(UnitType unitType) {
        this.producingUnit = unitType;
        this.productionTimer = 10.0; // 10 seconds to produce any unit
    }
    
    public boolean isProducing() { return producingUnit != null; }
    public UnitType getProducingUnit() { return producingUnit; }
    public float getProductionProgress() { 
        return producingUnit != null ? (float)(1.0 - productionTimer / 10.0) : 0; 
    }
    
    public void takeDamage(int damage) {
        health -= damage;
    }
    
    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public BuildingType getType() { return type; }
    public Team getTeam() { return team; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return type.getHealth(); }
    public int getSize() { return type.getSize(); }
    public float getBuildProgress() { return buildProgress; }
    public boolean isComplete() { return isComplete; }
}
