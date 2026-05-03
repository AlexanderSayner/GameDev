package org.sandbox.starbat;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Worker extends Unit {
    private boolean carryingMinerals = false;
    private Mineral targetMineral = null;
    private Building targetBuilding = null;
    
    public Worker(double x, double y, StarBatGame game) {
        super(x, y, game);
    }
    
    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(carryingMinerals ? Color.ORANGE : Color.CYAN);
        gc.fillOval(x - 15, y - 15, 30, 30);
        
        gc.setFill(Color.WHITE);
        gc.fillText("W", x - 5, y + 5);
    }
    
    @Override
    public void update() {
        // Handle building construction
        if (targetBuilding != null) {
            double dx = targetBuilding.getX() + targetBuilding.getWidth()/2 - x;
            double dy = targetBuilding.getY() + targetBuilding.getHeight()/2 - y;
            double dist = Math.sqrt(dx*dx + dy*dy);
            
            if (dist < 50) {
                // Building complete (simplified)
                targetBuilding = null;
            } else {
                moveTowardsTarget();
                return;
            }
        }
        
        // Handle mineral mining
        if (targetMineral != null) {
            if (!carryingMinerals) {
                // Go to mineral
                double dx = targetMineral.getX() - x;
                double dy = targetMineral.getY() - y;
                double dist = Math.sqrt(dx*dx + dy*dy);
                
                if (dist < 20) {
                    carryingMinerals = true;
                    // Find base to return to
                    for (Building building : new java.util.ArrayList<>(game.getBuildings())) {
                        if (building.getType() == BuildingType.BASE) {
                            setTarget(building.getX() + building.getWidth()/2, 
                                      building.getY() + building.getHeight());
                            break;
                        }
                    }
                } else {
                    moveTowardsTarget();
                }
            } else {
                // Return to base
                Building base = null;
                for (Building building : new java.util.ArrayList<>(game.getBuildings())) {
                    if (building.getType() == BuildingType.BASE) {
                        base = building;
                        break;
                    }
                }
                
                if (base != null) {
                    double dx = (base.getX() + base.getWidth()/2) - x;
                    double dy = (base.getY() + base.getHeight()) - y;
                    double dist = Math.sqrt(dx*dx + dy*dy);
                    
                    if (dist < 30) {
                        carryingMinerals = false;
                        game.addMinerals(5);
                        // Go back to mineral
                        setTarget(targetMineral.getX(), targetMineral.getY());
                    } else {
                        moveTowardsTarget();
                    }
                }
            }
            return;
        }
        
        // Normal movement
        moveTowardsTarget();
    }
    
    public void setTargetMineral(Mineral mineral) {
        this.targetMineral = mineral;
    }
    
    public void setTargetBuilding(Building building) {
        this.targetBuilding = building;
    }
}
