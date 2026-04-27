package com.rts.game.ai;

import com.rts.game.entities.*;
import com.rts.game.systems.*;

import java.util.*;

public class AISystem {
    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
    
    private Difficulty difficulty;
    private double buildTimer = 0;
    private double attackTimer = 0;
    private double economyTimer = 0;
    
    // AI behavior parameters based on difficulty
    private double buildInterval;
    private double attackInterval;
    private int maxArmySize;
    private float aggressionLevel;
    
    public AISystem(Difficulty difficulty) {
        this.difficulty = difficulty;
        
        switch (difficulty) {
            case EASY:
                buildInterval = 5.0;
                attackInterval = 30.0;
                maxArmySize = 8;
                aggressionLevel = 0.3f;
                break;
            case MEDIUM:
                buildInterval = 3.0;
                attackInterval = 20.0;
                maxArmySize = 15;
                aggressionLevel = 0.6f;
                break;
            case HARD:
                buildInterval = 1.5;
                attackInterval = 12.0;
                maxArmySize = 25;
                aggressionLevel = 0.9f;
                break;
        }
    }
    
    public void update(GameMap gameMap, GameState gameState, double dt) {
        buildTimer += dt;
        attackTimer += dt;
        economyTimer += dt;
        
        // Economy management
        if (economyTimer >= 2.0) {
            manageEconomy(gameMap, gameState);
            economyTimer = 0;
        }
        
        // Building and unit production
        if (buildTimer >= buildInterval) {
            manageProduction(gameMap, gameState);
            buildTimer = 0;
        }
        
        // Army attacks
        if (attackTimer >= attackInterval) {
            launchAttack(gameMap, gameState);
            attackTimer = 0;
        }
    }
    
    private void manageEconomy(GameMap gameMap, GameState gameState) {
        List<Entity> aiWorkers = gameMap.getEntitiesByOwnerAndType(EntityOwner.AI, EntityType.SCV);
        List<Entity> minerals = gameMap.getEntitiesByOwnerAndType(EntityOwner.NEUTRAL, EntityType.MINERAL_PATCH);
        
        // Assign workers to minerals if not already gathering
        for (Entity worker : aiWorkers) {
            if (!worker.isGathering() && !minerals.isEmpty()) {
                Entity nearestMineral = findNearestEntity(worker, minerals);
                if (nearestMineral != null) {
                    worker.setGatheringTarget(nearestMineral);
                }
            }
        }
        
        // Build more workers if we have resources and supply
        if (aiWorkers.size() < 20 && gameState.getAIMinerals() >= 50) {
            Entity commandCenter = gameMap.getFirstEntityOfTypeAndOwner(EntityType.COMMAND_CENTER, EntityOwner.AI);
            if (commandCenter != null) {
                // Train SCV
                trainUnit(EntityType.SCV, commandCenter, gameMap, gameState);
            }
        }
    }
    
    private void manageProduction(GameMap gameMap, GameState gameState) {
        List<Entity> barracks = gameMap.getEntitiesByOwnerAndType(EntityOwner.AI, EntityType.BARRACKS);
        List<Entity> aiUnits = gameMap.getEntitiesByOwner(EntityOwner.AI);
        
        int militaryUnits = countMilitaryUnits(aiUnits);
        
        // Build barracks if we don't have one and can afford it
        if (barracks.isEmpty() && gameState.getAIMinerals() >= 150) {
            Entity commandCenter = gameMap.getFirstEntityOfTypeAndOwner(EntityType.COMMAND_CENTER, EntityOwner.AI);
            if (commandCenter != null) {
                placeBuildingNear(commandCenter, EntityType.BARRACKS, gameMap, gameState);
            }
        }
        
        // Build military units
        if (!barracks.isEmpty() && militaryUnits < maxArmySize) {
            Entity barracksBuilding = barracks.get(0);
            
            // Choose unit type based on difficulty and resources
            if (gameState.getAIMinerals() >= 150 && difficulty == Difficulty.HARD) {
                trainUnit(EntityType.TANK, barracksBuilding, gameMap, gameState);
            } else if (gameState.getAIMinerals() >= 50) {
                trainUnit(EntityType.MARINE, barracksBuilding, gameMap, gameState);
            }
        }
        
        // Build supply depots if needed
        if (gameState.getAISupply() >= gameState.getAIMaxSupply() - 2) {
            Entity commandCenter = gameMap.getFirstEntityOfTypeAndOwner(EntityType.COMMAND_CENTER, EntityOwner.AI);
            if (commandCenter != null && gameState.getAIMinerals() >= 100) {
                placeBuildingNear(commandCenter, EntityType.SUPPLY_DEPOT, gameMap, gameState);
            }
        }
    }
    
    private void launchAttack(GameMap gameMap, GameState gameState) {
        List<Entity> aiMilitary = new ArrayList<>();
        List<Entity> aiUnits = gameMap.getEntitiesByOwner(EntityOwner.AI);
        
        for (Entity unit : aiUnits) {
            if (unit.getType() == EntityType.MARINE || unit.getType() == EntityType.TANK) {
                aiMilitary.add(unit);
            }
        }
        
        if (aiMilitary.size() >= maxArmySize * aggressionLevel) {
            // Find player base
            Entity playerBase = gameMap.getFirstEntityOfTypeAndOwner(EntityType.COMMAND_CENTER, EntityOwner.PLAYER);
            if (playerBase != null) {
                // Order all military units to attack player base
                for (Entity unit : aiMilitary) {
                    unit.setTarget(playerBase);
                }
            }
        }
    }
    
    private void trainUnit(EntityType unitType, Entity producer, GameMap gameMap, GameState gameState) {
        if (gameState.canAfford(unitType, EntityOwner.AI)) {
            Entity unit = new Entity(producer.getX() + 30, producer.getY(), unitType);
            unit.setOwner(EntityOwner.AI);
            gameState.spendResources(unitType, EntityOwner.AI);
            gameMap.addEntity(unit);
        }
    }
    
    private void placeBuildingNear(Entity reference, EntityType buildingType, GameMap gameMap, GameState gameState) {
        if (gameState.canAfford(buildingType, EntityOwner.AI)) {
            float offsetX = (float) (Math.random() * 80 - 40);
            float offsetY = (float) (Math.random() * 80 - 40);
            
            Entity building = new Entity(reference.getX() + offsetX, reference.getY() + offsetY, buildingType);
            building.setOwner(EntityOwner.AI);
            building.startBuilding();
            gameState.spendResources(buildingType, EntityOwner.AI);
            
            if (buildingType == EntityType.SUPPLY_DEPOT) {
                gameState.increaseAIMaxSupply(8);
            }
            
            gameMap.addEntity(building);
        }
    }
    
    private int countMilitaryUnits(List<Entity> units) {
        int count = 0;
        for (Entity unit : units) {
            if (unit.getType() == EntityType.MARINE || unit.getType() == EntityType.TANK) {
                count++;
            }
        }
        return count;
    }
    
    private Entity findNearestEntity(Entity from, List<Entity> candidates) {
        Entity nearest = null;
        float minDistance = Float.MAX_VALUE;
        
        for (Entity candidate : candidates) {
            float dx = candidate.getX() - from.getX();
            float dy = candidate.getY() - from.getY();
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            if (distance < minDistance) {
                minDistance = distance;
                nearest = candidate;
            }
        }
        
        return nearest;
    }
}
