package com.rts.game.systems;

import java.util.*;

public class InputHandler {
    private float selectionStartX, selectionStartY;
    private float currentX, currentY;
    private boolean selecting = false;
    
    public void startSelection(float x, float y) {
        selectionStartX = x;
        selectionStartY = y;
        currentX = x;
        currentY = y;
        selecting = true;
    }
    
    public void endSelection() {
        selecting = false;
    }
    
    public void updateCursorPosition(float x, float y) {
        currentX = x;
        currentY = y;
    }
    
    public float getSelectionStartX() { return selectionStartX; }
    public float getSelectionStartY() { return selectionStartY; }
    public float getCurrentX() { return currentX; }
    public float getCurrentY() { return currentY; }
    
    public float[] getSelectionStart() { 
        return new float[]{selectionStartX, selectionStartY}; 
    }
    
    public float[] getSelectionEnd() { 
        return new float[]{currentX, currentY}; 
    }
    
    public boolean isSelecting() { return selecting; }
}
