package com.rts.game.systems;

import com.rts.game.entities.*;

import java.util.ArrayList;
import java.util.List;

public class SelectionSystem {
    private List<Entity> selectedUnits = new ArrayList<>();
    
    public void selectUnitsInArea(float startX, float startY, float endX, float endY, 
                                   GameMap gameMap, float cameraX, float cameraY) {
        // Convert screen coordinates to world coordinates
        float worldStartX = startX + cameraX;
        float worldStartY = startY + cameraY;
        float worldEndX = endX + cameraX;
        float worldEndY = endY + cameraY;
        
        List<Entity> entitiesInArea = gameMap.getEntitiesInArea(worldStartX, worldStartY, worldEndX, worldEndY);
        
        // Clear previous selection
        for (Entity entity : selectedUnits) {
            entity.setSelected(false);
        }
        selectedUnits.clear();
        
        // Select player units in area
        for (Entity entity : entitiesInArea) {
            if (entity.getOwner() == EntityOwner.PLAYER && 
                !entity.getType().isBuilding()) {
                entity.setSelected(true);
                selectedUnits.add(entity);
            }
        }
    }
    
    public void selectUnit(Entity entity) {
        // Clear previous selection
        for (Entity e : selectedUnits) {
            e.setSelected(false);
        }
        selectedUnits.clear();
        
        if (entity != null && entity.getOwner() == EntityOwner.PLAYER) {
            entity.setSelected(true);
            selectedUnits.add(entity);
        }
    }
    
    public void addUnitToSelection(Entity entity) {
        if (entity != null && entity.getOwner() == EntityOwner.PLAYER && !selectedUnits.contains(entity)) {
            entity.setSelected(true);
            selectedUnits.add(entity);
        }
    }
    
    public void clearSelection() {
        for (Entity entity : selectedUnits) {
            entity.setSelected(false);
        }
        selectedUnits.clear();
    }
    
    public List<Entity> getSelectedUnits() {
        return selectedUnits;
    }
    
    public boolean hasSelection() {
        return !selectedUnits.isEmpty();
    }
}
