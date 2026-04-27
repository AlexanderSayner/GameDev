package com.rts.game.systems;

import com.rts.game.entities.*;

import java.util.ArrayList;
import java.util.List;

public class CommandSystem {
    private EntityType buildingToPlace = null;
    
    public void issueCommand(float worldX, float worldY, List<Entity> selectedUnits, 
                             GameMap gameMap, GameState gameState) {
        if (buildingToPlace != null) {
            // Place building
            placeBuilding(worldX, worldY, gameMap, gameState);
            return;
        }
        
        if (selectedUnits.isEmpty()) {
            return;
        }
        
        // Check if clicking on an enemy unit
        Entity clickedEntity = gameMap.getEntityAt(worldX, worldY, 20.0f);
        if (clickedEntity != null && clickedEntity.getOwner() == EntityOwner.AI) {
            // Attack command
            for (Entity unit : selectedUnits) {
                unit.setTarget(clickedEntity);
            }
            return;
        }
        
        // Check if clicking on a resource
        if (clickedEntity != null && 
            (clickedEntity.getType() == EntityType.MINERAL_PATCH || 
             clickedEntity.getType() == EntityType.VESPENE_GEYSER)) {
            // Gather command
            for (Entity unit : selectedUnits) {
                if (unit.getType() == EntityType.SCV) {
                    unit.setGatheringTarget(clickedEntity);
                }
            }
            return;
        }
        
        // Move command
        for (Entity unit : selectedUnits) {
            unit.setTarget(worldX, worldY);
        }
    }
    
    public void queueBuildingPlacement(EntityType type) {
        buildingToPlace = type;
    }
    
    public void cancelBuildingPlacement() {
        buildingToPlace = null;
    }
    
    private void placeBuilding(float worldX, float worldY, GameMap gameMap, GameState gameState) {
        if (buildingToPlace == null) return;
        
        EntityOwner owner = EntityOwner.PLAYER; // Assuming player is placing
        
        if (!gameState.canAfford(buildingToPlace, owner)) {
            System.out.println("Not enough resources!");
            return;
        }
        
        // Create the building
        Entity building = new Entity(worldX - buildingToPlace.getWidth()/2, 
                                      worldY - buildingToPlace.getHeight()/2, 
                                      buildingToPlace);
        building.setOwner(owner);
        building.startBuilding();
        
        gameState.spendResources(buildingToPlace, owner);
        
        // Increase supply if supply depot
        if (buildingToPlace == EntityType.SUPPLY_DEPOT) {
            gameState.increasePlayerMaxSupply(8);
        }
        
        gameMap.addEntity(building);
        
        // Clear building placement mode
        buildingToPlace = null;
        
        System.out.println("Building placed: " + buildingToPlace);
    }
    
    public void trainUnit(EntityType unitType, Entity producer, GameMap gameMap, GameState gameState) {
        if (!gameState.canAfford(unitType, producer.getOwner())) {
            System.out.println("Not enough resources!");
            return;
        }
        
        // Spawn unit near producer
        Entity unit = new Entity(producer.getX() + 30, producer.getY(), unitType);
        unit.setOwner(producer.getOwner());
        
        gameState.spendResources(unitType, producer.getOwner());
        
        gameMap.addEntity(unit);
        
        System.out.println("Unit trained: " + unitType);
    }
}
