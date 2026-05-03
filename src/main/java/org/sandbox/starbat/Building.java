package org.sandbox.starbat;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Building {
    private double x, y;
    private final double width, height;
    private final BuildingType type;
    private final Color color;
    private boolean markedForRemoval = false;
    
    public Building(double x, double y, double width, double height, 
                    BuildingType type, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.color = color;
    }
    
    public void render(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillRect(x, y, width, height);
        
        gc.setFill(Color.WHITE);
        String label = (type == BuildingType.BASE) ? "Base" : "Barracks";
        gc.fillText(label, x + 10, y + height/2 + 5);
    }
    
    public boolean contains(double x, double y) {
        return x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public BuildingType getType() { return type; }
    public boolean isMarkedForRemoval() { return markedForRemoval; }
    public void setMarkedForRemoval(boolean marked) { this.markedForRemoval = marked; }
}
