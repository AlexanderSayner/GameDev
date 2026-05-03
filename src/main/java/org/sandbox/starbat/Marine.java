package org.sandbox.starbat;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Marine extends Unit {
    
    public Marine(double x, double y, StarBatGame game) {
        super(x, y, game);
    }
    
    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.RED);
        gc.fillOval(x - 15, y - 15, 30, 30);
        
        gc.setFill(Color.WHITE);
        gc.fillText("M", x - 5, y + 5);
    }
    
    @Override
    public void update() {
        moveTowardsTarget();
    }
}
