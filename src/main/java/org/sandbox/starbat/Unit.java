package org.sandbox.starbat;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Unit {
    protected double x, y;
    protected Double targetX = null;
    protected Double targetY = null;
    protected double speed = 2.0;
    protected boolean selected = false;
    protected StarBatGame game;
    
    public Unit(double x, double y, StarBatGame game) {
        this.x = x;
        this.y = y;
        this.game = game;
    }
    
    public abstract void render(GraphicsContext gc);
    public abstract void update();
    
    public void setTarget(double x, double y) {
        this.targetX = x;
        this.targetY = y;
    }
    
    public boolean contains(double x, double y) {
        return Math.abs(x - this.x) < 20 && Math.abs(y - this.y) < 20;
    }
    
    public void moveTowardsTarget() {
        if (targetX != null && targetY != null) {
            double dx = targetX - x;
            double dy = targetY - y;
            double dist = Math.sqrt(dx*dx + dy*dy);
            
            if (dist > 5) {
                x += (dx / dist) * speed;
                y += (dy / dist) * speed;
            } else {
                targetX = null;
                targetY = null;
            }
        }
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}
