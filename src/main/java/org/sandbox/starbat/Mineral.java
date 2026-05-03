package org.sandbox.starbat;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Mineral {
    private final double x, y;
    private int amount;
    
    public Mineral(double x, double y, int amount) {
        this.x = x;
        this.y = y;
        this.amount = amount;
    }
    
    public void render(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 200, 200));
        gc.fillOval(x - 20, y - 20, 40, 40);
        
        gc.setFill(Color.WHITE);
        gc.fillText(String.valueOf(amount), x - 10, y + 5);
    }
    
    public boolean contains(double x, double y) {
        return Math.abs(x - this.x) < 25 && Math.abs(y - this.y) < 25;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
}
