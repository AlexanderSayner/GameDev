package com.rts.game.entities;

public class Entity {
    private float x, y;
    private EntityType type;
    private EntityOwner owner;
    private int health;
    private int maxHealth;
    private float moveSpeed = 2.0f;
    private int attackDamage = 5;
    private float attackRange = 50.0f;
    private float attackCooldown = 1.0f;
    private float lastAttackTime = 0;
    
    // State
    private boolean selected = false;
    private boolean gathering = false;
    private Entity targetResource = null;
    private Entity targetEntity = null;
    private float targetX = 0;
    private float targetY = 0;
    private boolean isMoving = false;
    private boolean isAttacking = false;
    private boolean isBuilding = false;
    private float buildProgress = 0;
    
    public Entity(float x, float y, EntityType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.health = type.getMaxHealth();
        this.maxHealth = type.getMaxHealth();
        
        // Set unit-specific stats
        switch (type) {
            case SCV:
                moveSpeed = 2.5f;
                attackDamage = 5;
                break;
            case MARINE:
                moveSpeed = 3.0f;
                attackDamage = 6;
                attackRange = 80.0f;
                break;
            case TANK:
                moveSpeed = 1.5f;
                attackDamage = 15;
                attackRange = 120.0f;
                break;
        }
    }
    
    public void update(double dt) {
        if (isMoving && !isBuilding) {
            moveToTarget(dt);
        }
        
        if (isAttacking && targetEntity != null) {
            updateCombat(dt);
        }
        
        if (gathering && targetResource != null) {
            updateGathering(dt);
        }
        
        if (isBuilding) {
            buildProgress += dt * 0.1; // Build over time
            if (buildProgress >= 1.0f) {
                isBuilding = false;
                buildProgress = 1.0f;
            }
        }
    }
    
    private void moveToTarget(double dt) {
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 1.0f) {
            x += (dx / distance) * moveSpeed * dt * 60;
            y += (dy / distance) * moveSpeed * dt * 60;
        } else {
            isMoving = false;
        }
    }
    
    private void updateCombat(double dt) {
        if (targetEntity == null || targetEntity.getHealth() <= 0) {
            isAttacking = false;
            targetEntity = null;
            return;
        }
        
        float dx = targetEntity.getX() - x;
        float dy = targetEntity.getY() - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance <= attackRange) {
            // In range, attack
            lastAttackTime += dt;
            if (lastAttackTime >= attackCooldown) {
                targetEntity.takeDamage(attackDamage);
                lastAttackTime = 0;
            }
        } else {
            // Move towards target
            targetX = targetEntity.getX();
            targetY = targetEntity.getY();
            isMoving = true;
        }
    }
    
    private void updateGathering(double dt) {
        if (targetResource == null || targetResource.getResourceAmount() <= 0) {
            gathering = false;
            targetResource = null;
            return;
        }
        
        float dx = targetResource.getX() - x;
        float dy = targetResource.getY() - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 10.0f) {
            targetX = targetResource.getX();
            targetY = targetResource.getY();
            isMoving = true;
        } else {
            isMoving = false;
            // Gather resources (simplified)
        }
    }
    
    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }
    
    public void setTarget(float x, float y) {
        this.targetX = x;
        this.targetY = y;
        this.isMoving = true;
        this.isAttacking = false;
        this.targetEntity = null;
    }
    
    public void setTarget(Entity entity) {
        this.targetEntity = entity;
        this.isAttacking = true;
        this.isMoving = false;
    }
    
    public void setGatheringTarget(Entity resource) {
        this.targetResource = resource;
        this.gathering = true;
    }
    
    // Getters and setters
    public float getX() { return x; }
    public float getY() { return y; }
    public EntityType getType() { return type; }
    public EntityOwner getOwner() { return owner; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isSelected() { return selected; }
    public boolean isGathering() { return gathering; }
    public boolean isBuilding() { return isBuilding; }
    public float getBuildProgress() { return buildProgress; }
    public int getResourceAmount() { return type == EntityType.MINERAL_PATCH || type == EntityType.VESPENE_GEYSER ? 
        (int)(maxHealth * 0.1f) : 0; }
    
    public void setOwner(EntityOwner owner) { this.owner = owner; }
    public void setMaxHealth(int maxHealth) { 
        this.maxHealth = maxHealth; 
        if (health > maxHealth) health = maxHealth;
    }
    public void setHealth(int health) { this.health = health; }
    public void setSelected(boolean selected) { this.selected = selected; }
    public void setResourceAmount(int amount) { /* For resources */ }
    public void startBuilding() { this.isBuilding = true; this.buildProgress = 0; }
}
