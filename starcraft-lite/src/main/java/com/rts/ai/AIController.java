package com.rts.ai;

import com.rts.entities.*;

import java.util.*;

public class AIController {
    private GameWorld gameWorld;
    private Difficulty difficulty = Difficulty.MEDIUM;
    
    // AI timing based on difficulty
    private double buildInterval;
    private int maxArmySize;
    private float aggressionLevel;
    
    private double lastBuildTime = 0;
    private double lastAttackTime = 0;
    private boolean enemyBaseInitialized = false;
    
    public AIController(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
        setDifficulty(Difficulty.MEDIUM);
    }
    
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        switch (difficulty) {
            case EASY:
                buildInterval = 5.0;
                maxArmySize = 8;
                aggressionLevel = 0.3f;
                break;
            case MEDIUM:
                buildInterval = 3.0;
                maxArmySize = 15;
                aggressionLevel = 0.6f;
                break;
            case HARD:
                buildInterval = 1.5;
                maxArmySize = 25;
                aggressionLevel = 0.9f;
                break;
        }
        System.out.println("[AI] Difficulty set to " + difficulty + 
                          " (build: " + buildInterval + "s, army: " + maxArmySize + 
                          ", aggression: " + aggressionLevel + ")");
    }
    
    public void update(double deltaTime) {
        // Initialize enemy base if not done
        if (!enemyBaseInitialized && gameWorld.getEnemyUnits().isEmpty()) {
            gameWorld.initializeEnemyBase();
            enemyBaseInitialized = true;
            System.out.println("[AI] Enemy base initialized!");
        }
        
        // Count current army
        int currentArmy = 0;
        for (Unit unit : gameWorld.getEnemyUnits()) {
            if (unit.isCombat()) currentArmy++;
        }
        
        // Build units periodically
        lastBuildTime += deltaTime;
        if (lastBuildTime >= buildInterval) {
            lastBuildTime = 0;
            makeDecision(currentArmy);
        }
        
        // Attack periodically
        lastAttackTime += deltaTime;
        if (lastAttackTime >= 30.0 / aggressionLevel) {
            lastAttackTime = 0;
            launchAttack();
        }
    }
    
    private void makeDecision(int currentArmy) {
        List<Unit> enemyUnits = gameWorld.getEnemyUnits();
        List<Building> enemyBuildings = gameWorld.getBuildings();
        
        // Check if we have enough SCVs (aim for 8 workers)
        int scvCount = 0;
        for (Unit unit : enemyUnits) {
            if (unit.getType() == UnitType.SCV) scvCount++;
        }
        
        if (scvCount < 8) {
            produceUnit(UnitType.SCV);
            return;
        }
        
        // If army is small, build combat units
        if (currentArmy < maxArmySize) {
            // Prefer marines early, tanks later
            if (currentArmy < 4 || Math.random() > 0.3) {
                produceUnit(UnitType.MARINE);
            } else {
                produceUnit(UnitType.TANK);
            }
            return;
        }
        
        // If army is at cap, consider building expansion or more production
        // For simplicity, just save resources
    }
    
    private void produceUnit(UnitType type) {
        // Find a barracks
        Building barracks = null;
        for (Building b : gameWorld.getBuildings()) {
            if (b.getTeam() == Team.ENEMY && b.getType() == BuildingType.BARRACKS) {
                barracks = b;
                break;
            }
        }
        
        if (barracks == null) return;
        
        // Check resources and supply
        int mineralCost = type.getMineralCost();
        int gasCost = type.getGasCost();
        int supplyCost = type.getSupplyCost();
        
        if (gameWorld.getEnemyMinerals() >= mineralCost && 
            gameWorld.getEnemyGas() >= gasCost) {
            
            // Deduct resources
            // Note: In a real implementation, we'd have proper enemy resource tracking
            // For now, the AI gets "free" units within balance constraints
            
            // Spawn unit near barracks
            float spawnX = barracks.getX() + 40 + (float)(Math.random() * 20);
            float spawnY = barracks.getY() + (float)(Math.random() * 40 - 20);
            
            Unit unit = new Unit(spawnX, spawnY, type, Team.ENEMY);
            gameWorld.addUnit(unit);
            
            // Deduct from AI's virtual resources
            // This is simplified - in production would track separately
        }
    }
    
    private void launchAttack() {
        List<Unit> army = new ArrayList<>();
        for (Unit unit : gameWorld.getEnemyUnits()) {
            if (unit.isCombat()) {
                army.add(unit);
            }
        }
        
        if (army.isEmpty()) return;
        
        // Find player Command Center as target
        Building targetCC = null;
        for (Building b : gameWorld.getPlayerBuildings()) {
            if (b.getType() == BuildingType.COMMAND_CENTER) {
                targetCC = b;
                break;
            }
        }
        
        if (targetCC != null) {
            // Order all combat units to attack the CC
            for (Unit unit : army) {
                unit.attackBuilding(targetCC);
            }
            System.out.println("[AI] Launching attack with " + army.size() + " units!");
        } else {
            // No CC found, attack any player building
            if (!gameWorld.getPlayerBuildings().isEmpty()) {
                Building target = gameWorld.getPlayerBuildings().get(0);
                for (Unit unit : army) {
                    unit.attackBuilding(target);
                }
            }
        }
    }
    
    // AI micro management
    public void updateUnitBehavior(Unit unit) {
        if (!unit.isCombat()) return;
        
        // Simple aggro logic: attack nearest enemy if in range
        Unit nearestEnemy = gameWorld.findNearestEnemy(
            unit.getX(), unit.getY(), Team.ENEMY, unit.getAttackRange() * 2);
        
        if (nearestEnemy != null) {
            unit.attackUnit(nearestEnemy);
        }
    }
}
