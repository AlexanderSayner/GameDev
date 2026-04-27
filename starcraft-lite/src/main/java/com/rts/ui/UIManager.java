package com.rts.ui;

import com.rts.entities.*;
import org.lwjgl.opengl.GL11;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class UIManager {
    private int screenWidth, screenHeight;
    private BuildingType buildingMode = null;
    
    // Panel dimensions
    private final int RESOURCE_PANEL_HEIGHT = 50;
    private final int BOTTOM_PANEL_HEIGHT = 180;
    private final int SIDE_PANEL_WIDTH = 250;
    
    public UIManager(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }
    
    public void resize(int w, int h) {
        screenWidth = w;
        screenHeight = h;
    }
    
    public void setBuildingMode(BuildingType type) {
        this.buildingMode = type;
    }
    
    public void cancelBuildingMode() {
        this.buildingMode = null;
    }
    
    public void render(GameWorld gameWorld) {
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, screenWidth, screenHeight, 0, -1, 1);
        
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        
        // Render UI components
        drawResourcePanel(gameWorld);
        drawMinimap(gameWorld);
        drawEventLog(gameWorld);
        drawBottomPanel(gameWorld);
        drawQuestGoals(gameWorld);
        drawBuildingModeIndicator();
        
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
    }
    
    private void drawResourcePanel(GameWorld gameWorld) {
        // Background
        glColor4f(0.1f, 0.15f, 0.2f, 0.9f);
        glBegin(GL_QUADS);
        glVertex2i(0, 0);
        glVertex2i(screenWidth, 0);
        glVertex2i(screenWidth, RESOURCE_PANEL_HEIGHT);
        glVertex2i(0, RESOURCE_PANEL_HEIGHT);
        glEnd();
        
        // Border
        glColor3f(0.4f, 0.5f, 0.6f);
        glBegin(GL_LINE_LOOP);
        glVertex2i(0, 0);
        glVertex2i(screenWidth, 0);
        glVertex2i(screenWidth, RESOURCE_PANEL_HEIGHT);
        glVertex2i(0, RESOURCE_PANEL_HEIGHT);
        glEnd();
        
        // Resources text (simplified - in production would use actual font rendering)
        int minerals = gameWorld.getPlayerMinerals();
        int gas = gameWorld.getPlayerGas();
        int supplyUsed = gameWorld.getPlayerSupplyUsed();
        int supplyTotal = gameWorld.getPlayerSupply();
        
        // Mineral icon (blue square)
        glColor3f(0.2f, 0.4f, 0.9f);
        glBegin(GL_QUADS);
        glVertex2i(20, 15);
        glVertex2i(40, 15);
        glVertex2i(40, 35);
        glVertex2i(20, 35);
        glEnd();
        
        // Gas icon (green circle approximation)
        glColor3f(0.1f, 0.7f, 0.3f);
        glBegin(GL_QUADS);
        glVertex2i(90, 15);
        glVertex2i(110, 15);
        glVertex2i(110, 35);
        glVertex2i(90, 35);
        glEnd();
        
        // Supply icon (yellow square)
        glColor3f(0.9f, 0.8f, 0.2f);
        glBegin(GL_QUADS);
        glVertex2i(160, 15);
        glVertex2i(180, 15);
        glVertex2i(180, 35);
        glVertex2i(160, 35);
        glEnd();
        
        // Resource values (represented as colored bars since we don't have fonts)
        // Minerals bar
        glColor3f(0.2f, 0.4f, 0.9f);
        glBegin(GL_QUADS);
        glVertex2i(45, 18);
        glVertex2i(Math.min(45 + minerals / 10, 85), 18);
        glVertex2i(Math.min(45 + minerals / 10, 85), 32);
        glVertex2i(45, 32);
        glEnd();
        
        // Gas bar
        glColor3f(0.1f, 0.7f, 0.3f);
        glBegin(GL_QUADS);
        glVertex2i(115, 18);
        glVertex2i(Math.min(115 + gas / 10, 155), 18);
        glVertex2i(Math.min(115 + gas / 10, 155), 32);
        glVertex2i(115, 32);
        glEnd();
        
        // Supply text representation
        glColor3f(0.9f, 0.8f, 0.2f);
        glBegin(GL_QUADS);
        glVertex2i(185, 18);
        glVertex2i(185 + supplyUsed * 3, 18);
        glVertex2i(185 + supplyUsed * 3, 32);
        glVertex2i(185, 32);
        glEnd();
        
        // Supply fraction
        glColor3f(0.8f, 0.8f, 0.8f);
        String supplyText = supplyUsed + "/" + supplyTotal;
        // Would render text here in production
    }
    
    private void drawMinimap(GameWorld gameWorld) {
        int mapSize = Math.min(200, screenHeight - RESOURCE_PANEL_HEIGHT - BOTTOM_PANEL_HEIGHT - 20);
        int mapX = screenWidth - mapSize - 10;
        int mapY = RESOURCE_PANEL_HEIGHT + 10;
        
        // Minimap background
        glColor4f(0.05f, 0.1f, 0.15f, 0.9f);
        glBegin(GL_QUADS);
        glVertex2i(mapX, mapY);
        glVertex2i(mapX + mapSize, mapY);
        glVertex2i(mapX + mapSize, mapY + mapSize);
        glVertex2i(mapX, mapY + mapSize);
        glEnd();
        
        // Border
        glColor3f(0.5f, 0.6f, 0.7f);
        glBegin(GL_LINE_LOOP);
        glVertex2i(mapX, mapY);
        glVertex2i(mapX + mapSize, mapY);
        glVertex2i(mapX + mapSize, mapY + mapSize);
        glVertex2i(mapX, mapY + mapSize);
        glEnd();
        
        // Draw units on minimap
        float scaleX = (float)mapSize / gameWorld.getMapWidth();
        float scaleY = (float)mapSize / gameWorld.getMapHeight();
        
        // Player units (green dots)
        glColor3f(0.2f, 0.8f, 0.2f);
        glBegin(GL_POINTS);
        for (Unit unit : gameWorld.getPlayerUnits()) {
            float mx = mapX + unit.getX() * scaleX;
            float my = mapY + (gameWorld.getMapHeight() - unit.getY()) * scaleY;
            glVertex2f(mx, my);
        }
        glEnd();
        
        // Enemy units (red dots)
        glColor3f(0.9f, 0.2f, 0.2f);
        glBegin(GL_POINTS);
        for (Unit unit : gameWorld.getEnemyUnits()) {
            float mx = mapX + unit.getX() * scaleX;
            float my = mapY + (gameWorld.getMapHeight() - unit.getY()) * scaleY;
            glVertex2f(mx, my);
        }
        glEnd();
        
        // Player buildings (green squares)
        glColor3f(0.3f, 0.6f, 0.3f);
        glBegin(GL_QUADS);
        for (Building b : gameWorld.getPlayerBuildings()) {
            float mx = mapX + b.getX() * scaleX - 2;
            float my = mapY + (gameWorld.getMapHeight() - b.getY()) * scaleY - 2;
            glVertex2f(mx, my);
            glVertex2f(mx + 4, my);
            glVertex2f(mx + 4, my + 4);
            glVertex2f(mx, my + 4);
        }
        glEnd();
        
        // Enemy buildings (red squares)
        glColor3f(0.7f, 0.3f, 0.3f);
        glBegin(GL_QUADS);
        for (Building b : gameWorld.getBuildings()) {
            if (b.getTeam() == Team.ENEMY) {
                float mx = mapX + b.getX() * scaleX - 2;
                float my = mapY + (gameWorld.getMapHeight() - b.getY()) * scaleY - 2;
                glVertex2f(mx, my);
                glVertex2f(mx + 4, my);
                glVertex2f(mx + 4, my + 4);
                glVertex2f(mx, my + 4);
            }
        }
        glEnd();
        
        // Camera viewport rectangle
        float camWidth = (screenWidth - SIDE_PANEL_WIDTH) / gameWorld.getZoom();
        float camHeight = (screenHeight - RESOURCE_PANEL_HEIGHT - BOTTOM_PANEL_HEIGHT) / gameWorld.getZoom();
        float camX = mapX + (gameWorld.getCameraX() - camWidth/2) * scaleX;
        float camY = mapY + (gameWorld.getMapHeight() - gameWorld.getCameraY() - camHeight/2) * scaleY;
        
        glColor4f(1.0f, 1.0f, 0.2f, 0.8f);
        glBegin(GL_LINE_LOOP);
        glVertex2f(camX, camY);
        glVertex2f(camX + camWidth * scaleX, camY);
        glVertex2f(camX + camWidth * scaleX, camY + camHeight * scaleY);
        glVertex2f(camX, camY + camHeight * scaleY);
        glEnd();
        
        // Label
        glColor3f(0.7f, 0.7f, 0.7f);
        // "MINIMAP" label would be rendered here
    }
    
    private void drawEventLog(GameWorld gameWorld) {
        int logX = 10;
        int logY = RESOURCE_PANEL_HEIGHT + 10;
        int logWidth = 300;
        int logHeight = 150;
        
        // Semi-transparent background
        glColor4f(0.0f, 0.0f, 0.0f, 0.6f);
        glBegin(GL_QUADS);
        glVertex2i(logX, logY);
        glVertex2i(logX + logWidth, logY);
        glVertex2i(logX + logWidth, logY + logHeight);
        glVertex2i(logX, logY + logHeight);
        glEnd();
        
        // Border
        glColor3f(0.4f, 0.4f, 0.5f);
        glBegin(GL_LINE_LOOP);
        glVertex2i(logX, logY);
        glVertex2i(logX + logWidth, logY);
        glVertex2i(logX + logWidth, logY + logHeight);
        glVertex2i(logX, logY + logHeight);
        glEnd();
        
        // Event entries (as colored lines since no font)
        List<String> events = gameWorld.getEventLog();
        int lineHeight = 12;
        int startY = logY + logHeight - 15;
        
        for (int i = 0; i < Math.min(events.size(), 8); i++) {
            // Alternate colors for readability
            if (i % 2 == 0) {
                glColor4f(0.3f, 0.3f, 0.4f, 0.3f);
                glBegin(GL_QUADS);
                glVertex2i(logX + 2, startY - i * lineHeight - 8);
                glVertex2i(logX + logWidth - 2, startY - i * lineHeight - 8);
                glVertex2i(logX + logWidth - 2, startY - i * lineHeight + 4);
                glVertex2i(logX + 2, startY - i * lineHeight + 4);
                glEnd();
            }
            
            // Status indicator color based on event type
            String event = events.get(i);
            if (event.contains("created") || event.contains("constructed")) {
                glColor3f(0.2f, 0.8f, 0.2f);
            } else if (event.contains("destroyed") || event.contains("DEFEAT")) {
                glColor3f(0.9f, 0.3f, 0.3f);
            } else if (event.contains("VICTORY") || event.contains("Base established")) {
                glColor3f(0.3f, 0.8f, 0.9f);
            } else {
                glColor3f(0.7f, 0.7f, 0.7f);
            }
            
            // Draw a small indicator dot
            glBegin(GL_POINTS);
            glVertex2f(logX + 10, startY - i * lineHeight);
            glEnd();
        }
        
        // Label
        glColor3f(0.6f, 0.6f, 0.7f);
        // "EVENT LOG" label would be rendered here
    }
    
    private void drawBottomPanel(GameWorld gameWorld) {
        int panelY = screenHeight - BOTTOM_PANEL_HEIGHT;
        
        // Background
        glColor4f(0.1f, 0.15f, 0.2f, 0.95f);
        glBegin(GL_QUADS);
        glVertex2i(0, panelY);
        glVertex2i(screenWidth - SIDE_PANEL_WIDTH, panelY);
        glVertex2i(screenWidth - SIDE_PANEL_WIDTH, screenHeight);
        glVertex2i(0, screenHeight);
        glEnd();
        
        // Border
        glColor3f(0.4f, 0.5f, 0.6f);
        glBegin(GL_LINE_LOOP);
        glVertex2i(0, panelY);
        glVertex2i(screenWidth - SIDE_PANEL_WIDTH, panelY);
        glVertex2i(screenWidth - SIDE_PANEL_WIDTH, screenHeight);
        glVertex2i(0, screenHeight);
        glEnd();
        
        // Unit selection info
        List<Unit> selectedUnits = new java.util.ArrayList<>(); // Would get from input handler
        
        if (!selectedUnits.isEmpty()) {
            // Unit portrait area
            glColor4f(0.2f, 0.25f, 0.3f, 0.8f);
            glBegin(GL_QUADS);
            glVertex2i(20, panelY + 20);
            glVertex2i(100, panelY + 20);
            glVertex2i(100, panelY + 100);
            glVertex2i(20, panelY + 100);
            glEnd();
            
            // Unit stats area
            glColor3f(0.5f, 0.5f, 0.6f);
            // Stats would be rendered here
        }
        
        // Command buttons area
        int btnStartX = 120;
        int btnY = panelY + 20;
        int btnSize = 50;
        int btnGap = 10;
        
        // Move button (green)
        glColor3f(0.2f, 0.6f, 0.2f);
        drawButton(btnStartX, btnY, btnSize, btnSize);
        
        // Attack button (red)
        glColor3f(0.7f, 0.2f, 0.2f);
        drawButton(btnStartX + btnSize + btnGap, btnY, btnSize, btnSize);
        
        // Stop button (yellow)
        glColor3f(0.7f, 0.6f, 0.2f);
        drawButton(btnStartX + 2 * (btnSize + btnGap), btnY, btnSize, btnSize);
        
        // Hold position button (orange)
        glColor3f(0.7f, 0.4f, 0.2f);
        drawButton(btnStartX + 3 * (btnSize + btnGap), btnY, btnSize, btnSize);
        
        // Build buttons section
        int buildY = panelY + 80;
        
        // Barracks button
        glColor3f(0.4f, 0.4f, 0.7f);
        drawButton(btnStartX, buildY, btnSize, btnSize);
        
        // SCV button
        glColor3f(0.3f, 0.6f, 0.3f);
        drawButton(btnStartX + btnSize + btnGap, buildY, btnSize, btnSize);
        
        // Marine button
        glColor3f(0.3f, 0.4f, 0.8f);
        drawButton(btnStartX + 2 * (btnSize + btnGap), buildY, btnSize, btnSize);
        
        // Tank button
        glColor3f(0.3f, 0.7f, 0.3f);
        drawButton(btnStartX + 3 * (btnSize + btnGap), buildY, btnSize, btnSize);
        
        // Labels for buttons (would use actual text rendering)
        glColor3f(0.8f, 0.8f, 0.8f);
    }
    
    private void drawQuestGoals(GameWorld gameWorld) {
        int panelX = 10;
        int panelY = RESOURCE_PANEL_HEIGHT + 160;
        int panelWidth = 280;
        int panelHeight = 100;
        
        // Background
        glColor4f(0.1f, 0.1f, 0.15f, 0.8f);
        glBegin(GL_QUADS);
        glVertex2i(panelX, panelY);
        glVertex2i(panelX + panelWidth, panelY);
        glVertex2i(panelX + panelWidth, panelY + panelHeight);
        glVertex2i(panelX, panelY + panelHeight);
        glEnd();
        
        // Border
        glColor3f(0.5f, 0.4f, 0.2f);
        glBegin(GL_LINE_LOOP);
        glVertex2i(panelX, panelY);
        glVertex2i(panelX + panelWidth, panelY);
        glVertex2i(panelX + panelWidth, panelY + panelHeight);
        glVertex2i(panelX, panelY + panelHeight);
        glEnd();
        
        // Quest title
        glColor3f(0.9f, 0.8f, 0.3f);
        // "OBJECTIVES" title would be rendered here
        
        // Primary objective indicator
        boolean primaryComplete = !gameWorld.hasEnemyCommandCenter();
        glColor3f(primaryComplete ? 0.2f : 0.8f, primaryComplete ? 0.8f : 0.2f, 0.2f);
        glBegin(GL_TRIANGLES);
        glVertex2f(panelX + 15, panelY + 35);
        glVertex2f(panelX + 25, panelY + 35);
        glVertex2f(panelX + 20, panelY + 45);
        glEnd();
        
        // Secondary objective indicator
        int unitCount = gameWorld.getPlayerUnits().size();
        boolean secondaryComplete = unitCount >= 10;
        glColor3f(secondaryComplete ? 0.2f : 0.8f, secondaryComplete ? 0.8f : 0.2f, 0.2f);
        glBegin(GL_TRIANGLES);
        glVertex2f(panelX + 15, panelY + 55);
        glVertex2f(panelX + 25, panelY + 55);
        glVertex2f(panelX + 20, panelY + 65);
        glEnd();
        
        // Objective progress bars
        // Primary: Destroy enemy CC
        glColor4f(0.8f, 0.2f, 0.2f, 0.5f);
        if (primaryComplete) glColor4f(0.2f, 0.8f, 0.2f, 0.5f);
        glBegin(GL_QUADS);
        glVertex2i(panelX + 30, panelY + 32);
        glVertex2i(panelX + 30 + (primaryComplete ? 240 : 0), panelY + 32);
        glVertex2i(panelX + 30 + (primaryComplete ? 240 : 0), panelY + 42);
        glVertex2i(panelX + 30, panelY + 42);
        glEnd();
        
        // Secondary: Army size
        float armyProgress = Math.min(unitCount / 10f, 1.0f);
        glColor4f(0.2f, 0.6f, 0.9f, 0.5f);
        glBegin(GL_QUADS);
        glVertex2i(panelX + 30, panelY + 52);
        glVertex2i(panelX + 30 + (int)(240 * armyProgress), panelY + 52);
        glVertex2i(panelX + 30 + (int)(240 * armyProgress), panelY + 62);
        glVertex2i(panelX + 30, panelY + 62);
        glEnd();
    }
    
    private void drawBuildingModeIndicator() {
        if (buildingMode != null) {
            // Flashing indicator at top center
            float alpha = 0.5f + 0.5f * (float)Math.sin(System.currentTimeMillis() / 200.0);
            glColor4f(0.9f, 0.8f, 0.2f, alpha);
            
            int width = 200;
            int height = 30;
            int x = screenWidth / 2 - width / 2;
            int y = RESOURCE_PANEL_HEIGHT + 5;
            
            glBegin(GL_QUADS);
            glVertex2i(x, y);
            glVertex2i(x + width, y);
            glVertex2i(x + width, y + height);
            glVertex2i(x, y + height);
            glEnd();
            
            glColor3f(0.0f, 0.0f, 0.0f);
            // "PLACING: " + buildingMode name would be rendered here
        }
    }
    
    private void drawButton(int x, int y, int w, int h) {
        glBegin(GL_QUADS);
        glVertex2i(x, y);
        glVertex2i(x + w, y);
        glVertex2i(x + w, y + h);
        glVertex2i(x, y + h);
        glEnd();
        
        glColor3f(0.6f, 0.6f, 0.7f);
        glBegin(GL_LINE_LOOP);
        glVertex2i(x, y);
        glVertex2i(x + w, y);
        glVertex2i(x + w, y + h);
        glVertex2i(x, y + h);
        glEnd();
    }
}
