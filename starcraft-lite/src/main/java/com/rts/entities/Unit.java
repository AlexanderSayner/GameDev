package com.rts.entities;

public class Unit {
    private float x, y;
    private UnitType type;
    private Team team;
    private int health;
    private float targetX, targetY;
    private Unit targetUnit;
    private Building targetBuilding;
    private ResourceNode targetResource;
    
    private enum State { IDLE, MOVING, ATTACKING, GATHERING, RETURNING }
    private State state = State.IDLE;
    
    private double attackCooldown = 0;
    private double returnTimer = 0;
    private static final double RETURN_TIME = 2.0; // Time to return resources
    
    public Unit(float x, float y, UnitType type, Team team) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.team = team;
        this.health = type.getHealth();
        this.targetX = x;
        this.targetY = y;
    }
    
    public void update(double deltaTime, GameWorld world) {
        if (attackCooldown > 0) attackCooldown -= deltaTime;
        if (returnTimer > 0) returnTimer -= deltaTime;
        
        switch (state) {
            case MOVING:
                moveToTarget(deltaTime);
                break;
            case ATTACKING:
                updateAttacking(deltaTime, world);
                break;
            case GATHERING:
                updateGathering(deltaTime, world);
                break;
            case RETURNING:
                updateReturning(deltaTime, world);
                break;
            case IDLE:
            default:
                // Auto-attack nearby enemies for combat units
                if (type.isCombat() && attackCooldown <= 0) {
                    autoAttack(world);
                }
                break;
        }
    }
    
    private void moveToTarget(double deltaTime) {
        float dx = targetX - x;
        float dy = targetY - y;
        float dist = (float)Math.sqrt(dx * dx + dy * dy);
        
        if (dist < 5) {
            if (targetUnit != null && targetUnit.getHealth() > 0) {
                state = State.ATTACKING;
            } else if (targetBuilding != null && targetBuilding.getHealth() > 0) {
                state = State.ATTACKING;
            } else if (targetResource != null && targetResource.getAmount() > 0) {
                state = State.GATHERING;
            } else {
                state = State.IDLE;
            }
            return;
        }
        
        float moveDist = type.getSpeed() * (float)deltaTime;
        x += (dx / dist) * moveDist;
        y += (dy / dist) * moveDist;
    }
    
    private void updateAttacking(double deltaTime, GameWorld world) {
        if (targetUnit != null) {
            if (targetUnit.getHealth() <= 0) {
                targetUnit = null;
                state = State.IDLE;
                return;
            }
            
            float dx = targetUnit.getX() - x;
            float dy = targetUnit.getY() - y;
            float dist = (float)Math.sqrt(dx * dx + dy * dy);
            
            if (dist <= type.getAttackRange()) {
                if (attackCooldown <= 0) {
                    attack(targetUnit);
                }
            } else {
                // Move towards target
                x += (dx / dist) * type.getSpeed() * (float)deltaTime;
                y += (dy / dist) * type.getSpeed() * (float)deltaTime;
            }
        } else if (targetBuilding != null) {
            if (targetBuilding.getHealth() <= 0) {
                targetBuilding = null;
                state = State.IDLE;
                return;
            }
            
            float dx = targetBuilding.getX() - x;
            float dy = targetBuilding.getY() - y;
            float dist = (float)Math.sqrt(dx * dx + dy * dy);
            
            if (dist <= type.getAttackRange() + targetBuilding.getSize()/2) {
                if (attackCooldown <= 0) {
                    attack(targetBuilding);
                }
            } else {
                x += (dx / dist) * type.getSpeed() * (float)deltaTime;
                y += (dy / dist) * type.getSpeed() * (float)deltaTime;
            }
        } else {
            state = State.IDLE;
        }
    }
    
    private void updateGathering(double deltaTime, GameWorld world) {
        if (targetResource == null || targetResource.getAmount() <= 0) {
            state = State.IDLE;
            return;
        }
        
        // Check if at resource
        float dx = targetResource.getX() - x;
        float dy = targetResource.getY() - y;
        float dist = (float)Math.sqrt(dx * dx + dy * dy);
        
        if (dist > 20) {
            // Move to resource
            x += (dx / dist) * type.getSpeed() * (float)deltaTime;
            y += (dy / dist) * type.getSpeed() * (float)deltaTime;
        } else {
            // Gather for a moment then return
            world.gatherResources(this, targetResource, deltaTime);
            returnTimer = RETURN_TIME;
            state = State.RETURNING;
        }
    }
    
    private void updateReturning(double deltaTime, GameWorld world) {
        if (returnTimer <= 0) {
            // Go back to gathering
            if (targetResource != null && targetResource.getAmount() > 0) {
                state = State.GATHERING;
            } else {
                state = State.IDLE;
            }
            return;
        }
        
        // Find nearest Command Center to drop off resources
        Building nearestCC = null;
        float nearestDist = Float.MAX_VALUE;
        
        for (Building b : world.getBuildings()) {
            if (b.getTeam() == team && b.getType() == BuildingType.COMMAND_CENTER) {
                float dx = b.getX() - x;
                float dy = b.getY() - y;
                float dist = dx * dx + dy * dy;
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearestCC = b;
                }
            }
        }
        
        if (nearestCC != null) {
            float dx = nearestCC.getX() - x;
            float dy = nearestCC.getY() - y;
            float dist = (float)Math.sqrt(dx * dx + dy * dy);
            
            if (dist > 30) {
                x += (dx / dist) * type.getSpeed() * (float)deltaTime;
                y += (dy / dist) * type.getSpeed() * (float)deltaTime;
            }
        }
    }
    
    private void autoAttack(GameWorld world) {
        Unit enemy = world.findNearestEnemy(x, y, team, type.getAttackRange() * 1.5f);
        if (enemy != null) {
            targetUnit = enemy;
            targetBuilding = null;
            targetResource = null;
            state = State.ATTACKING;
        }
    }
    
    private void attack(Unit target) {
        target.takeDamage(type.getDamage());
        attackCooldown = 1.0; // 1 second between attacks
    }
    
    private void attack(Building target) {
        target.takeDamage(type.getDamage());
        attackCooldown = 1.0;
    }
    
    public void takeDamage(int damage) {
        health -= damage;
    }
    
    // Movement commands
    public void move(float x, float y) {
        this.targetX = x;
        this.targetY = y;
        this.targetUnit = null;
        this.targetBuilding = null;
        this.targetResource = null;
        this.state = State.MOVING;
    }
    
    public void attackMove(float x, float y) {
        this.targetX = x;
        this.targetY = y;
        this.state = State.MOVING;
        // Will auto-attack enemies encountered along the way
    }
    
    public void attackUnit(Unit target) {
        this.targetUnit = target;
        this.targetBuilding = null;
        this.targetResource = null;
        this.targetX = target.getX();
        this.targetY = target.getY();
        this.state = State.ATTACKING;
    }
    
    public void attackBuilding(Building target) {
        this.targetBuilding = target;
        this.targetUnit = null;
        this.targetResource = null;
        this.targetX = target.getX();
        this.targetY = target.getY();
        this.state = State.ATTACKING;
    }
    
    public void gather(ResourceNode resource) {
        this.targetResource = resource;
        this.targetUnit = null;
        this.targetBuilding = null;
        this.targetX = resource.getX();
        this.targetY = resource.getY();
        this.state = State.MOVING;
    }
    
    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public UnitType getType() { return type; }
    public Team getTeam() { return team; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return type.getHealth(); }
    public float getAttackRange() { return type.getAttackRange(); }
    public int getSupplyCost() { return type.getSupplyCost(); }
    public float getGatherRate() { return 10.0f; } // Resources per second
    public boolean isCombat() { return type.isCombat(); }
}
