package com.rts.game.systems;

import com.rts.game.entities.*;

import java.util.ArrayList;
import java.util.List;

public class GameMap {
    private int width;
    private int height;
    private List<Entity> entities = new ArrayList<>();
    
    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public void addEntity(Entity entity) {
        entities.add(entity);
    }
    
    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }
    
    public void updateEntities(double dt) {
        // Update all entities
        for (Entity entity : entities) {
            entity.update(dt);
        }
        
        // Remove dead entities
        entities.removeIf(e -> e.getHealth() <= 0 && 
                              e.getType() != EntityType.MINERAL_PATCH && 
                              e.getType() != EntityType.VESPENE_GEYSER);
    }
    
    public List<Entity> getEntities() {
        return entities;
    }
    
    public List<Entity> getEntitiesByOwner(EntityOwner owner) {
        List<Entity> result = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity.getOwner() == owner) {
                result.add(entity);
            }
        }
        return result;
    }
    
    public List<Entity> getEntitiesByOwnerAndType(EntityOwner owner, EntityType type) {
        List<Entity> result = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity.getOwner() == owner && entity.getType() == type) {
                result.add(entity);
            }
        }
        return result;
    }
    
    public Entity getFirstEntityOfTypeAndOwner(EntityType type, EntityOwner owner) {
        for (Entity entity : entities) {
            if (entity.getType() == type && entity.getOwner() == owner) {
                return entity;
            }
        }
        return null;
    }
    
    public boolean hasEntityOfTypeAndOwner(EntityType type, EntityOwner owner) {
        for (Entity entity : entities) {
            if (entity.getType() == type && entity.getOwner() == owner) {
                return true;
            }
        }
        return false;
    }
    
    public List<Entity> getEntitiesInArea(float x1, float y1, float x2, float y2) {
        List<Entity> result = new ArrayList<>();
        float minX = Math.min(x1, x2);
        float maxX = Math.max(x1, x2);
        float minY = Math.min(y1, y2);
        float maxY = Math.max(y1, y2);
        
        for (Entity entity : entities) {
            if (entity.getX() >= minX && entity.getX() <= maxX &&
                entity.getY() >= minY && entity.getY() <= maxY) {
                result.add(entity);
            }
        }
        return result;
    }
    
    public Entity getEntityAt(float x, float y, float radius) {
        for (Entity entity : entities) {
            float dx = entity.getX() - x;
            float dy = entity.getY() - y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance <= radius) {
                return entity;
            }
        }
        return null;
    }
    
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
