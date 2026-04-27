package com.rts.entities;

public class ResourceNode {
    private float x, y;
    private ResourceType type;
    private int amount;
    private int maxSize;
    
    public ResourceNode(float x, float y, ResourceType type, int amount) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.amount = amount;
        this.maxSize = type == ResourceType.MINERALS ? 30 : 40;
    }
    
    public void deplete(int amount) {
        this.amount -= amount;
        if (this.amount < 0) this.amount = 0;
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    public ResourceType getType() { return type; }
    public int getAmount() { return amount; }
    public int getSize() { return maxSize; }
    public boolean isEmpty() { return amount <= 0; }
}
