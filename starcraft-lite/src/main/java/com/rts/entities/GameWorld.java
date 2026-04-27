package com.rts.entities;

import java.util.*;

public class GameWorld {
    private int screenWidth, screenHeight;
    private float cameraX = 0, cameraY = 0;
    private float zoom = 1.0f;
    
    private List<Unit> units = new ArrayList<>();
    private List<Building> buildings = new ArrayList<>();
    private List<ResourceNode> resources = new ArrayList<>();
    
    private int playerMinerals = 400;
    private int playerGas = 100;
    private int playerSupply = 10;
    private int playerSupplyUsed = 4;
    
    private int enemyMinerals = 400;
    private int enemyGas = 100;
    private int enemySupply = 10;
    private int enemySupplyUsed = 0;
    
    private boolean playerCommandCenterExists = true;
    private boolean enemyCommandCenterExists = true;
    
    private int mapWidth = 3000;
    private int mapHeight = 3000;
    
    // Event log
    private List<String> eventLog = new ArrayList<>();
    private List<String> questGoals = Arrays.asList(
        "PRIMARY: Destroy enemy Command Center",
        "SECONDARY: Build an army of at least 10 units",
        "TIP: Gather minerals with SCVs by right-clicking on mineral patches"
    );
    
    public GameWorld(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        cameraX = mapWidth / 2f;
        cameraY = mapHeight / 2f;
        
        // Generate resource nodes
        generateResources();
        
        addLog("Game started! Build your base and destroy the enemy.");
    }
    
    private void generateResources() {
        // Player starting minerals (cluster near spawn)
        for (int i = 0; i < 8; i++) {
            float angle = (i / 8f) * (float)Math.PI * 2;
            float x = 500 + (float)Math.cos(angle) * 200;
            float y = 500 + (float)Math.sin(angle) * 200;
            resources.add(new ResourceNode(x, y, ResourceType.MINERALS, 1500));
        }
        
        // Enemy starting minerals
        for (int i = 0; i < 8; i++) {
            float angle = (i / 8f) * (float)Math.PI * 2;
            float x = (mapWidth - 500) + (float)Math.cos(angle) * 200;
            float y = (mapHeight - 500) + (float)Math.sin(angle) * 200;
            resources.add(new ResourceNode(x, y, ResourceType.MINERALS, 1500));
        }
        
        // Player vespene geysers
        resources.add(new ResourceNode(700, 400, ResourceType.VESPENE_GAS, 5000));
        resources.add(new ResourceNode(700, 600, ResourceType.VESPENE_GAS, 5000));
        
        // Enemy vespene geysers
        resources.add(new ResourceNode(mapWidth - 700, mapHeight - 400, ResourceType.VESPENE_GAS, 5000));
        resources.add(new ResourceNode(mapWidth - 700, mapHeight - 600, ResourceType.VESPENE_GAS, 5000));
        
        // Neutral mineral clusters in the middle
        for (int cluster = 0; cluster < 3; cluster++) {
            float cx = mapWidth/2 + (cluster - 1) * 400;
            float cy = mapHeight/2;
            for (int i = 0; i < 6; i++) {
                float angle = (i / 6f) * (float)Math.PI * 2;
                float x = cx + (float)Math.cos(angle) * 150;
                float y = cy + (float)Math.sin(angle) * 150;
                resources.add(new ResourceNode(x, y, ResourceType.MINERALS, 1000));
            }
        }
    }
    
    public void initializePlayerBase() {
        // Create player Command Center
        Building cc = new Building(400, 500, BuildingType.COMMAND_CENTER, Team.PLAYER);
        buildings.add(cc);
        
        // Create player Barracks
        Building barracks = new Building(500, 600, BuildingType.BARRACKS, Team.PLAYER);
        buildings.add(barracks);
        
        // Create 4 SCVs as requested
        for (int i = 0; i < 4; i++) {
            Unit scv = new Unit(450 + i * 30, 550, UnitType.SCV, Team.PLAYER);
            units.add(scv);
        }
        
        playerSupplyUsed = 4;
        addLog("Base established: Command Center, Barracks, and 4 SCVs ready!");
    }
    
    public void initializeEnemyBase() {
        // Create enemy Command Center
        Building cc = new Building(mapWidth - 400, mapHeight - 500, BuildingType.COMMAND_CENTER, Team.ENEMY);
        buildings.add(cc);
        
        // Create enemy Barracks
        Building barracks = new Building(mapWidth - 500, mapHeight - 600, BuildingType.BARRACKS, Team.ENEMY);
        buildings.add(barracks);
        
        // Create 4 enemy SCVs
        for (int i = 0; i < 4; i++) {
            Unit scv = new Unit(mapWidth - 450 - i * 30, mapHeight - 550, UnitType.SCV, Team.ENEMY);
            units.add(scv);
        }
        
        enemySupplyUsed = 4;
        addLog("Enemy base detected!");
    }
    
    public void update(double deltaTime) {
        // Update all units
        for (Unit unit : units) {
            unit.update(deltaTime, this);
        }
        
        // Update all buildings
        for (Building building : buildings) {
            building.update(deltaTime);
        }
        
        // Check if command centers still exist
        playerCommandCenterExists = false;
        enemyCommandCenterExists = false;
        
        for (Building b : buildings) {
            if (b.getType() == BuildingType.COMMAND_CENTER) {
                if (b.getTeam() == Team.PLAYER) playerCommandCenterExists = true;
                else enemyCommandCenterExists = true;
            }
        }
        
        // Remove dead units and buildings
        units.removeIf(u -> u.getHealth() <= 0);
        buildings.removeIf(b -> b.getHealth() <= 0);
    }
    
    public void addUnit(Unit unit) {
        units.add(unit);
        if (unit.getTeam() == Team.PLAYER) {
            playerSupplyUsed += unit.getSupplyCost();
            addLog("Unit created: " + unit.getType());
        } else {
            enemySupplyUsed += unit.getSupplyCost();
        }
    }
    
    public void addBuilding(Building building) {
        buildings.add(building);
        if (building.getTeam() == Team.PLAYER) {
            addLog("Building constructed: " + building.getType());
        }
    }
    
    public boolean canAfford(int minerals, int gas) {
        return playerMinerals >= minerals && playerGas >= gas;
    }
    
    public boolean spendResources(int minerals, int gas) {
        if (!canAfford(minerals, gas)) {
            addLog("Not enough resources! Need " + minerals + " minerals, " + gas + " gas");
            return false;
        }
        playerMinerals -= minerals;
        playerGas -= gas;
        return true;
    }
    
    public boolean hasSupplySpace(int amount) {
        return playerSupplyUsed + amount <= playerSupply;
    }
    
    public void addSupply(int amount) {
        playerSupply += amount;
        addLog("Supply increased to " + playerSupply);
    }
    
    public void gatherResources(Unit scv, ResourceNode node, double deltaTime) {
        if (node.getAmount() <= 0) return;
        
        int gathered = (int)(scv.getGatherRate() * deltaTime);
        if (gathered > node.getAmount()) gathered = (int)node.getAmount();
        
        if (node.getType() == ResourceType.MINERALS) {
            if (scv.getTeam() == Team.PLAYER) {
                playerMinerals += gathered;
            } else {
                enemyMinerals += gathered;
            }
        } else {
            if (scv.getTeam() == Team.PLAYER) {
                playerGas += gathered;
            } else {
                enemyGas += gathered;
            }
        }
        
        node.deplete(gathered);
    }
    
    public void dealDamageToEnemy(float x, float y, float radius, int damage, Team attackerTeam) {
        for (Unit unit : units) {
            if (unit.getTeam() != attackerTeam) {
                float dx = unit.getX() - x;
                float dy = unit.getY() - y;
                if (dx * dx + dy * dy <= radius * radius) {
                    unit.takeDamage(damage);
                    if (unit.getHealth() <= 0) {
                        addLog(attackerTeam == Team.PLAYER ? "Enemy unit destroyed!" : "Your unit was destroyed!");
                    }
                }
            }
        }
        
        for (Building building : buildings) {
            if (building.getTeam() != attackerTeam) {
                float dx = building.getX() - x;
                float dy = building.getY() - y;
                if (dx * dx + dy * dy <= radius * radius) {
                    building.takeDamage(damage);
                }
            }
        }
    }
    
    // Getters
    public List<Unit> getUnits() { return units; }
    public List<Unit> getPlayerUnits() {
        List<Unit> result = new ArrayList<>();
        for (Unit u : units) if (u.getTeam() == Team.PLAYER) result.add(u);
        return result;
    }
    public List<Unit> getEnemyUnits() {
        List<Unit> result = new ArrayList<>();
        for (Unit u : units) if (u.getTeam() == Team.ENEMY) result.add(u);
        return result;
    }
    public List<Building> getBuildings() { return buildings; }
    public List<Building> getPlayerBuildings() {
        List<Building> result = new ArrayList<>();
        for (Building b : buildings) if (b.getTeam() == Team.PLAYER) result.add(b);
        return result;
    }
    public List<ResourceNode> getResources() { return resources; }
    
    public int getPlayerMinerals() { return playerMinerals; }
    public int getPlayerGas() { return playerGas; }
    public int getPlayerSupply() { return playerSupply; }
    public int getPlayerSupplyUsed() { return playerSupplyUsed; }
    
    public int getEnemyMinerals() { return enemyMinerals; }
    public int getEnemyGas() { return enemyGas; }
    
    public float getCameraX() { return cameraX; }
    public float getCameraY() { return cameraY; }
    public float getZoom() { return zoom; }
    
    public int getMapWidth() { return mapWidth; }
    public int getMapHeight() { return mapHeight; }
    
    public boolean hasPlayerCommandCenter() { return playerCommandCenterExists; }
    public boolean hasEnemyCommandCenter() { return enemyCommandCenterExists; }
    
    public List<String> getEventLog() { return eventLog; }
    public List<String> getQuestGoals() { return questGoals; }
    
    public void setCameraPosition(float x, float y) {
        cameraX = Math.max(0, Math.min(mapWidth, x));
        cameraY = Math.max(0, Math.min(mapHeight, y));
    }
    
    public void centerCameraOnBase() {
        cameraX = 500;
        cameraY = 500;
    }
    
    public void adjustZoom(float delta) {
        zoom = Math.max(0.5f, Math.min(3.0f, zoom + delta * 0.1f));
    }
    
    public void screenToWorld(int screenX, int screenY, float[] worldPos) {
        float halfWidth = (screenWidth / 2f) / zoom;
        float halfHeight = (screenHeight / 2f) / zoom;
        
        float worldX = cameraX + (screenX - screenWidth / 2f) / zoom;
        float worldY = cameraY - (screenY - screenHeight / 2f) / zoom;
        
        worldPos[0] = Math.max(0, Math.min(mapWidth, worldX));
        worldPos[1] = Math.max(0, Math.min(mapHeight, worldY));
    }
    
    public void worldToScreen(float worldX, float worldY, int[] screenPos) {
        screenPos[0] = (int)((screenWidth / 2f) + (worldX - cameraX) * zoom);
        screenPos[1] = (int)((screenHeight / 2f) - (worldY - cameraY) * zoom);
    }
    
    public Unit findNearestEnemy(float x, float y, Team team, float maxRange) {
        Unit nearest = null;
        float nearestDist = maxRange;
        
        for (Unit unit : units) {
            if (unit.getTeam() != team) {
                float dx = unit.getX() - x;
                float dy = unit.getY() - y;
                float dist = (float)Math.sqrt(dx * dx + dy * dy);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = unit;
                }
            }
        }
        
        return nearest;
    }
    
    public Building findNearestEnemyBuilding(float x, float y, Team team, float maxRange) {
        Building nearest = null;
        float nearestDist = maxRange;
        
        for (Building building : buildings) {
            if (building.getTeam() != team) {
                float dx = building.getX() - x;
                float dy = building.getY() - y;
                float dist = (float)Math.sqrt(dx * dx + dy * dy);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = building;
                }
            }
        }
        
        return nearest;
    }
    
    public boolean isPositionValid(float x, float y, float size) {
        // Check bounds
        if (x < size || x > mapWidth - size || y < size || y > mapHeight - size) {
            return false;
        }
        
        // Check collision with other buildings
        for (Building b : buildings) {
            float dx = Math.abs(b.getX() - x);
            float dy = Math.abs(b.getY() - y);
            if (dx < (b.getSize() + size) / 2 && dy < (b.getSize() + size) / 2) {
                return false;
            }
        }
        
        // Check collision with resources
        for (ResourceNode r : resources) {
            float dx = Math.abs(r.getX() - x);
            float dy = Math.abs(r.getY() - y);
            if (dx < (r.getSize() + size) / 2 && dy < (r.getSize() + size) / 2) {
                return false;
            }
        }
        
        return true;
    }
    
    private void addLog(String message) {
        String timestamp = String.format("[%.1fs]", (System.currentTimeMillis() % 100000) / 1000f);
        eventLog.add(0, timestamp + " " + message);
        if (eventLog.size() > 20) eventLog.remove(eventLog.size() - 1);
        System.out.println("[LOG] " + message);
    }
}
