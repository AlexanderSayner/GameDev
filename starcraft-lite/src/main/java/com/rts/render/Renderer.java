package com.rts.render;

import com.rts.entities.*;
import com.rts.input.InputHandler;
import com.rts.ui.UIManager;

import org.lwjgl.opengl.GL11;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {
    public void render(GameWorld gameWorld, InputHandler inputHandler, UIManager uiManager) {
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        
        float zoom = gameWorld.getZoom();
        int screenWidth = 1280; // Would get from actual window
        int screenHeight = 720;
        
        float camX = gameWorld.getCameraX();
        float camY = gameWorld.getCameraY();
        
        // Apply camera transform
        glTranslatef(screenWidth / 2f, screenHeight / 2f, 0);
        glScalef(zoom, zoom, 1);
        glTranslatef(-camX, -camY, 0);
        
        // Draw map background
        drawMapBackground(gameWorld);
        
        // Draw resources
        drawResources(gameWorld.getResources());
        
        // Draw buildings
        drawBuildings(gameWorld.getBuildings());
        
        // Draw units
        drawUnits(gameWorld.getUnits());
        
        // Draw selection box
        if (inputHandler.isSelecting()) {
            drawSelectionBox(inputHandler);
        }
        
        // Draw building placement preview
        if (inputHandler.isPlacingBuilding()) {
            drawBuildingPreview(inputHandler, gameWorld);
        }
        
        // Reset transform for UI elements that should not be affected by camera
        glLoadIdentity();
    }
    
    private void drawMapBackground(GameWorld gameWorld) {
        glBegin(GL_QUADS);
        
        // Ground color - terrain green/brown
        glColor3f(0.15f, 0.25f, 0.15f);
        glVertex2f(0, 0);
        glVertex2f(gameWorld.getMapWidth(), 0);
        glVertex2f(gameWorld.getMapWidth(), gameWorld.getMapHeight());
        glVertex2f(0, gameWorld.getMapHeight());
        
        glEnd();
        
        // Draw grid lines for visual reference
        glColor4f(0.2f, 0.3f, 0.2f, 0.3f);
        glBegin(GL_LINES);
        for (int x = 0; x <= gameWorld.getMapWidth(); x += 100) {
            glVertex2f(x, 0);
            glVertex2f(x, gameWorld.getMapHeight());
        }
        for (int y = 0; y <= gameWorld.getMapHeight(); y += 100) {
            glVertex2f(0, y);
            glVertex2f(gameWorld.getMapWidth(), y);
        }
        glEnd();
    }
    
    private void drawResources(List<ResourceNode> resources) {
        for (ResourceNode resource : resources) {
            if (resource.isEmpty()) continue;
            
            float x = resource.getX();
            float y = resource.getY();
            float size = resource.getSize();
            
            if (resource.getType() == ResourceType.MINERALS) {
                // Blue crystals for minerals
                float alpha = 0.5f + 0.5f * (resource.getAmount() / 1500f);
                glColor4f(0.2f, 0.4f, 0.9f, alpha);
            } else {
                // Green gas geysers
                float alpha = 0.5f + 0.5f * (resource.getAmount() / 5000f);
                glColor4f(0.1f, 0.7f, 0.3f, alpha);
            }
            
            glBegin(GL_QUADS);
            glVertex2f(x - size/2, y - size/2);
            glVertex2f(x + size/2, y - size/2);
            glVertex2f(x + size/2, y + size/2);
            glVertex2f(x - size/2, y + size/2);
            glEnd();
            
            // Outline
            glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
            glBegin(GL_LINE_LOOP);
            glVertex2f(x - size/2, y - size/2);
            glVertex2f(x + size/2, y - size/2);
            glVertex2f(x + size/2, y + size/2);
            glVertex2f(x - size/2, y + size/2);
            glEnd();
        }
    }
    
    private void drawBuildings(List<Building> buildings) {
        for (Building building : buildings) {
            float x = building.getX();
            float y = building.getY();
            float size = building.getSize();
            
            // Building color based on team and type
            if (building.getTeam() == Team.PLAYER) {
                switch (building.getType()) {
                    case COMMAND_CENTER: glColor3f(0.6f, 0.6f, 0.8f); break;
                    case BARRACKS: glColor3f(0.5f, 0.5f, 0.7f); break;
                    case REFINERY: glColor3f(0.4f, 0.6f, 0.4f); break;
                    case SUPPLY_DEPOT: glColor3f(0.7f, 0.5f, 0.5f); break;
                }
            } else {
                // Enemy - red tint
                switch (building.getType()) {
                    case COMMAND_CENTER: glColor3f(0.8f, 0.4f, 0.4f); break;
                    case BARRACKS: glColor3f(0.7f, 0.3f, 0.3f); break;
                    case REFINERY: glColor3f(0.4f, 0.6f, 0.4f); break;
                    case SUPPLY_DEPOT: glColor3f(0.7f, 0.5f, 0.5f); break;
                }
            }
            
            // Draw building
            glBegin(GL_QUADS);
            glVertex2f(x - size/2, y - size/2);
            glVertex2f(x + size/2, y - size/2);
            glVertex2f(x + size/2, y + size/2);
            glVertex2f(x - size/2, y + size/2);
            glEnd();
            
            // Health bar
            float healthPercent = (float)building.getHealth() / building.getMaxHealth();
            float barWidth = size;
            float barHeight = 4;
            float barY = y + size/2 + 8;
            
            // Background
            glColor3f(0.3f, 0.3f, 0.3f);
            glBegin(GL_QUADS);
            glVertex2f(x - barWidth/2, barY);
            glVertex2f(x + barWidth/2, barY);
            glVertex2f(x + barWidth/2, barY + barHeight);
            glVertex2f(x - barWidth/2, barY + barHeight);
            glEnd();
            
            // Health
            if (healthPercent > 0.6f) glColor3f(0.2f, 0.8f, 0.2f);
            else if (healthPercent > 0.3f) glColor3f(0.8f, 0.8f, 0.2f);
            else glColor3f(0.8f, 0.2f, 0.2f);
            
            glBegin(GL_QUADS);
            glVertex2f(x - barWidth/2, barY);
            glVertex2f(x - barWidth/2 + barWidth * healthPercent, barY);
            glVertex2f(x - barWidth/2 + barWidth * healthPercent, barY + barHeight);
            glVertex2f(x - barWidth/2, barY + barHeight);
            glEnd();
        }
    }
    
    private void drawUnits(List<Unit> units) {
        for (Unit unit : units) {
            float x = unit.getX();
            float y = unit.getY();
            float size = 12;
            
            // Unit color based on team and type
            if (unit.getTeam() == Team.PLAYER) {
                switch (unit.getType()) {
                    case SCV: glColor3f(0.3f, 0.7f, 0.3f); break;
                    case MARINE: glColor3f(0.3f, 0.5f, 0.9f); break;
                    case TANK: glColor3f(0.3f, 0.7f, 0.3f); break;
                }
            } else {
                // Enemy - red tint
                switch (unit.getType()) {
                    case SCV: glColor3f(0.9f, 0.3f, 0.3f); break;
                    case MARINE: glColor3f(0.9f, 0.3f, 0.5f); break;
                    case TANK: glColor3f(0.9f, 0.5f, 0.3f); break;
                }
            }
            
            // Draw unit as circle (approximated with quad for simplicity)
            glBegin(GL_QUADS);
            glVertex2f(x - size/2, y - size/2);
            glVertex2f(x + size/2, y - size/2);
            glVertex2f(x + size/2, y + size/2);
            glVertex2f(x - size/2, y + size/2);
            glEnd();
            
            // Selection circle for player units
            if (unit.getTeam() == Team.PLAYER) {
                glColor4f(0.2f, 1.0f, 0.2f, 0.5f);
                glBegin(GL_LINE_LOOP);
                for (int i = 0; i < 16; i++) {
                    double angle = (i / 16.0) * Math.PI * 2;
                    float sx = x + (float)Math.cos(angle) * size;
                    float sy = y + (float)Math.sin(angle) * size;
                    glVertex2f(sx, sy);
                }
                glEnd();
            }
            
            // Health bar for damaged units
            if (unit.getHealth() < unit.getMaxHealth()) {
                float healthPercent = (float)unit.getHealth() / unit.getMaxHealth();
                float barWidth = size;
                float barHeight = 3;
                float barY = y + size/2 + 5;
                
                glColor3f(0.3f, 0.3f, 0.3f);
                glBegin(GL_QUADS);
                glVertex2f(x - barWidth/2, barY);
                glVertex2f(x + barWidth/2, barY);
                glVertex2f(x + barWidth/2, barY + barHeight);
                glVertex2f(x - barWidth/2, barY + barHeight);
                glEnd();
                
                if (healthPercent > 0.6f) glColor3f(0.2f, 0.8f, 0.2f);
                else if (healthPercent > 0.3f) glColor3f(0.8f, 0.8f, 0.2f);
                else glColor3f(0.8f, 0.2f, 0.2f);
                
                glBegin(GL_QUADS);
                glVertex2f(x - barWidth/2, barY);
                glVertex2f(x - barWidth/2 + barWidth * healthPercent, barY);
                glVertex2f(x - barWidth/2 + barWidth * healthPercent, barY + barHeight);
                glVertex2f(x - barWidth/2, barY + barHeight);
                glEnd();
            }
        }
    }
    
    private void drawSelectionBox(InputHandler inputHandler) {
        int x1 = inputHandler.getSelectStartX();
        int y1 = inputHandler.getSelectStartY();
        int x2 = inputHandler.getSelectEndX();
        int y2 = inputHandler.getSelectEndY();
        
        glColor4f(0.2f, 0.8f, 0.2f, 0.3f);
        glBegin(GL_QUADS);
        glVertex2i(Math.min(x1, x2), Math.min(y1, y2));
        glVertex2i(Math.max(x1, x2), Math.min(y1, y2));
        glVertex2i(Math.max(x1, x2), Math.max(y1, y2));
        glVertex2i(Math.min(x1, x2), Math.max(y1, y2));
        glEnd();
        
        glColor4f(0.3f, 1.0f, 0.3f, 0.8f);
        glBegin(GL_LINE_LOOP);
        glVertex2i(Math.min(x1, x2), Math.min(y1, y2));
        glVertex2i(Math.max(x1, x2), Math.min(y1, y2));
        glVertex2i(Math.max(x1, x2), Math.max(y1, y2));
        glVertex2i(Math.min(x1, x2), Math.max(y1, y2));
        glEnd();
    }
    
    private void drawBuildingPreview(InputHandler inputHandler, GameWorld gameWorld) {
        // Get cursor position in world space (would need to pass mouse position)
        // For now, just show a semi-transparent building at camera center
        float x = gameWorld.getCameraX();
        float y = gameWorld.getCameraY();
        float size = 40;
        
        glColor4f(0.5f, 0.8f, 0.5f, 0.5f);
        glBegin(GL_QUADS);
        glVertex2f(x - size/2, y - size/2);
        glVertex2f(x + size/2, y - size/2);
        glVertex2f(x + size/2, y + size/2);
        glVertex2f(x - size/2, y + size/2);
        glEnd();
    }
}
