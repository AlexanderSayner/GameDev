package com.rts.entities;

public enum UnitType {
    SCV(50, 0, 1, 60, 2.0f, 3.5f, 5, false),
    MARINE(50, 25, 1, 100, 4.0f, 3.0f, 8, true),
    TANK(150, 100, 2, 200, 6.0f, 2.0f, 15, true);
    
    private final int mineralCost;
    private final int gasCost;
    private final int supplyCost;
    private final int health;
    private final float speed;
    private final float attackRange;
    private final int damage;
    private final boolean isCombat;
    
    UnitType(int mineralCost, int gasCost, int supplyCost, int health, 
             float speed, float attackRange, int damage, boolean isCombat) {
        this.mineralCost = mineralCost;
        this.gasCost = gasCost;
        this.supplyCost = supplyCost;
        this.health = health;
        this.speed = speed;
        this.attackRange = attackRange;
        this.damage = damage;
        this.isCombat = isCombat;
    }
    
    public int getMineralCost() { return mineralCost; }
    public int getGasCost() { return gasCost; }
    public int getSupplyCost() { return supplyCost; }
    public int getHealth() { return health; }
    public float getSpeed() { return speed; }
    public float getAttackRange() { return attackRange; }
    public int getDamage() { return damage; }
    public boolean isCombat() { return isCombat; }
}
