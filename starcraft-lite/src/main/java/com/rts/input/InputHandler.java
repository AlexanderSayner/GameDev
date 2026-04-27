package com.rts.input;

import com.rts.entities.*;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class InputHandler {
    private GameWorld gameWorld;
    private int screenWidth, screenHeight;
    
    private List<Unit> selectedUnits = new ArrayList<>();
    private boolean isSelecting = false;
    private int selectStartX, selectStartY;
    private int selectEndX, selectEndY;
    
    private boolean attackMode = false;
    private boolean moveMode = false;
    
    private float cameraVelocityX = 0;
    private float cameraVelocityY = 0;
    private static final float CAMERA_SPEED = 30f;
    private static final float CAMERA_ACCEL = 80f;
    private static final float CAMERA_FRICTION = 10f;
    
    // Building placement
    private boolean placingBuilding = false;
    private BuildingType buildingToPlace;
    
    public InputHandler(GameWorld gameWorld, int screenWidth, int screenHeight) {
        this.gameWorld = gameWorld;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }
    
    public void resize(int w, int h) {
        screenWidth = w;
        screenHeight = h;
    }
    
    public void update(double deltaTime) {
        // Camera movement with physics
        float targetVelX = cameraVelocityX;
        float targetVelY = cameraVelocityY;
        
        // Apply friction
        targetVelX -= Math.signum(cameraVelocityX) * CAMERA_FRICTION * (float)deltaTime;
        targetVelY -= Math.signum(cameraVelocityY) * CAMERA_FRICTION * (float)deltaTime;
        
        if (Math.abs(targetVelX) < 1f) targetVelX = 0;
        if (Math.abs(targetVelY) < 1f) targetVelY = 0;
        
        cameraVelocityX = targetVelX;
        cameraVelocityY = targetVelY;
        
        float newX = gameWorld.getCameraX() + cameraVelocityX * (float)deltaTime;
        float newY = gameWorld.getCameraY() + cameraVelocityY * (float)deltaTime;
        
        gameWorld.setCameraPosition(newX, newY);
    }
    
    public void handleKeyboard(int key, int action) {
        if (action != GLFW.GLFW_PRESS && action != GLFW.GLFW_RELEASE) return;
        
        float dir = action == GLFW.GLFW_PRESS ? 1f : -1f;
        
        switch (key) {
            case GLFW.GLFW_KEY_W:
            case GLFW.GLFW_KEY_UP:
                cameraVelocityY += dir * CAMERA_ACCEL;
                break;
            case GLFW.GLFW_KEY_S:
            case GLFW.GLFW_KEY_DOWN:
                cameraVelocityY -= dir * CAMERA_ACCEL;
                break;
            case GLFW.GLFW_KEY_A:
            case GLFW.GLFW_KEY_LEFT:
                cameraVelocityX -= dir * CAMERA_ACCEL;
                break;
            case GLFW.GLFW_KEY_D:
            case GLFW.GLFW_KEY_RIGHT:
                cameraVelocityX += dir * CAMERA_ACCEL;
                break;
        }
    }
    
    public void handleMouseInput(int x, int y, int button, int action) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (action == GLFW.GLFW_PRESS) {
                if (placingBuilding) {
                    placeBuilding(x, y);
                } else {
                    isSelecting = true;
                    selectStartX = x;
                    selectStartY = y;
                    selectEndX = x;
                    selectEndY = y;
                }
            } else if (action == GLFW.GLFW_RELEASE) {
                if (!placingBuilding) {
                    isSelecting = false;
                    finalizeSelection();
                }
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && action == GLFW.GLFW_PRESS) {
            handleRightClick(x, y);
        }
    }
    
    public void handleMouseMove(int x, int y) {
        if (isSelecting) {
            selectEndX = x;
            selectEndY = y;
        }
    }
    
    private void finalizeSelection() {
        selectedUnits.clear();
        
        int minX = Math.min(selectStartX, selectEndX);
        int maxX = Math.max(selectStartX, selectEndX);
        int minY = Math.min(selectStartY, selectEndY);
        int maxY = Math.max(selectStartY, selectEndY);
        
        // If click was small (single click), just select unit at position
        if (maxX - minX < 5 && maxY - minY < 5) {
            float[] worldPos = new float[2];
            gameWorld.screenToWorld(selectStartX, selectStartY, worldPos);
            
            for (Unit unit : gameWorld.getPlayerUnits()) {
                float dx = unit.getX() - worldPos[0];
                float dy = unit.getY() - worldPos[1];
                if (dx * dx + dy * dy < 400) { // 20 pixel radius
                    selectedUnits.add(unit);
                    break;
                }
            }
        } else {
            // Box selection
            for (Unit unit : gameWorld.getPlayerUnits()) {
                int[] screenPos = new int[2];
                gameWorld.worldToScreen(unit.getX(), unit.getY(), screenPos);
                
                if (screenPos[0] >= minX && screenPos[0] <= maxX &&
                    screenPos[1] >= minY && screenPos[1] <= maxY) {
                    selectedUnits.add(unit);
                }
            }
        }
        
        if (!selectedUnits.isEmpty()) {
            System.out.println("[INPUT] Selected " + selectedUnits.size() + " units");
        }
    }
    
    private void handleRightClick(int x, int y) {
        float[] worldPos = new float[2];
        gameWorld.screenToWorld(x, y, worldPos);
        float targetX = worldPos[0];
        float targetY = worldPos[1];
        
        if (attackMode) {
            // Attack-move command
            for (Unit unit : selectedUnits) {
                unit.attackMove(targetX, targetY);
            }
            attackMode = false;
            System.out.println("[INPUT] Attack-move to (" + (int)targetX + ", " + (int)targetY + ")");
            return;
        }
        
        if (moveMode) {
            // Move command
            for (Unit unit : selectedUnits) {
                unit.move(targetX, targetY);
            }
            moveMode = false;
            System.out.println("[INPUT] Move to (" + (int)targetX + ", " + (int)targetY + ")");
            return;
        }
        
        // Check if clicking on enemy unit
        Unit clickedEnemy = null;
        for (Unit unit : gameWorld.getEnemyUnits()) {
            float dx = unit.getX() - targetX;
            float dy = unit.getY() - targetY;
            if (dx * dx + dy * dy < 900) { // 30 pixel radius
                clickedEnemy = unit;
                break;
            }
        }
        
        if (clickedEnemy != null) {
            // Attack enemy unit
            for (Unit unit : selectedUnits) {
                unit.attackUnit(clickedEnemy);
            }
            System.out.println("[INPUT] Attacking enemy unit!");
            return;
        }
        
        // Check if clicking on enemy building
        Building clickedEnemyBuilding = null;
        for (Building building : gameWorld.getBuildings()) {
            if (building.getTeam() == Team.ENEMY) {
                float dx = building.getX() - targetX;
                float dy = building.getY() - targetY;
                if (dx * dx + dy * dy < building.getSize() * building.getSize()) {
                    clickedEnemyBuilding = building;
                    break;
                }
            }
        }
        
        if (clickedEnemyBuilding != null) {
            for (Unit unit : selectedUnits) {
                unit.attackBuilding(clickedEnemyBuilding);
            }
            System.out.println("[INPUT] Attacking enemy building!");
            return;
        }
        
        // Check if clicking on resource
        ResourceNode clickedResource = null;
        for (ResourceNode resource : gameWorld.getResources()) {
            float dx = resource.getX() - targetX;
            float dy = resource.getY() - targetY;
            if (dx * dx + dy * dy < resource.getSize() * resource.getSize()) {
                clickedResource = resource;
                break;
            }
        }
        
        if (clickedResource != null) {
            // Send SCVs to gather
            for (Unit unit : selectedUnits) {
                if (unit.getType() == UnitType.SCV) {
                    unit.gather(clickedResource);
                } else {
                    unit.move(targetX, targetY);
                }
            }
            System.out.println("[INPUT] Gathering from " + clickedResource.getType());
            return;
        }
        
        // Default: move to location
        for (Unit unit : selectedUnits) {
            unit.move(targetX, targetY);
        }
        
        if (!selectedUnits.isEmpty()) {
            System.out.println("[INPUT] Moving " + selectedUnits.size() + " units to (" + (int)targetX + ", " + (int)targetY + ")");
        }
    }
    
    private void placeBuilding(int screenX, int screenY) {
        float[] worldPos = new float[2];
        gameWorld.screenToWorld(screenX, screenY, worldPos);
        
        int mineralCost = 0, gasCost = 0;
        switch (buildingToPlace) {
            case BARRACKS: mineralCost = 150; gasCost = 0; break;
            case REFINERY: mineralCost = 75; gasCost = 0; break;
            case SUPPLY_DEPOT: mineralCost = 100; gasCost = 0; break;
        }
        
        if (gameWorld.spendResources(mineralCost, gasCost)) {
            if (gameWorld.isPositionValid(worldPos[0], worldPos[1], 40)) {
                Building building = new Building(worldPos[0], worldPos[1], buildingToPlace, Team.PLAYER);
                gameWorld.addBuilding(building);
                
                if (buildingToPlace == BuildingType.SUPPLY_DEPOT) {
                    gameWorld.addSupply(8);
                }
                
                placingBuilding = false;
                buildingToPlace = null;
            } else {
                // Invalid position, refund
                gameWorld.getPlayerMinerals(); // Just for logging
                System.out.println("[INPUT] Cannot place building here!");
            }
        }
    }
    
    public void setAttackMode(boolean attackMode) {
        this.attackMode = attackMode;
        if (attackMode) {
            System.out.println("[INPUT] Attack mode enabled - right-click to attack-move");
        }
    }
    
    public void setMoveMode(boolean moveMode) {
        this.moveMode = moveMode;
        if (moveMode) {
            System.out.println("[INPUT] Move mode enabled - right-click to move");
        }
    }
    
    public boolean isPlacingBuilding() { return placingBuilding; }
    public BuildingType getBuildingToPlace() { return buildingToPlace; }
    
    public void startBuildingPlacement(BuildingType type) {
        this.placingBuilding = true;
        this.buildingToPlace = type;
        System.out.println("[INPUT] Placing " + type + " - left-click to place, right-click to cancel");
    }
    
    public void cancelBuildingPlacement() {
        placingBuilding = false;
        buildingToPlace = null;
    }
    
    public List<Unit> getSelectedUnits() { return selectedUnits; }
    public boolean isSelecting() { return isSelecting; }
    public int getSelectStartX() { return selectStartX; }
    public int getSelectStartY() { return selectStartY; }
    public int getSelectEndX() { return selectEndX; }
    public int getSelectEndY() { return selectEndY; }
}
