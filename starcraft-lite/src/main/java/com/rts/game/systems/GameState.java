package com.rts.game.systems;

import com.rts.game.entities.*;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private int playerMinerals = 50;
    private int playerGas = 0;
    private int playerSupply = 0;
    private int playerMaxSupply = 10;
    
    private int aiMinerals = 50;
    private int aiGas = 0;
    private int aiSupply = 0;
    private int aiMaxSupply = 10;
    
    public void setPlayerResources(int minerals, int gas, int supply) {
        this.playerMinerals = minerals;
        this.playerGas = gas;
        this.playerSupply = supply;
    }
    
    public void setAIResources(int minerals, int gas, int supply) {
        this.aiMinerals = minerals;
        this.aiGas = gas;
        this.aiSupply = supply;
    }
    
    public void addPlayerMinerals(float amount) {
        playerMinerals += (int)amount;
    }
    
    public void addAIMinerals(float amount) {
        aiMinerals += (int)amount;
    }
    
    public boolean canAfford(EntityType type, EntityOwner owner) {
        if (owner == EntityOwner.PLAYER) {
            return playerMinerals >= type.getMineralCost() && 
                   playerGas >= type.getGasCost() &&
                   playerSupply + 1 <= playerMaxSupply;
        } else {
            return aiMinerals >= type.getMineralCost() && 
                   aiGas >= type.getGasCost() &&
                   aiSupply + 1 <= aiMaxSupply;
        }
    }
    
    public void spendResources(EntityType type, EntityOwner owner) {
        if (owner == EntityOwner.PLAYER) {
            playerMinerals -= type.getMineralCost();
            playerGas -= type.getGasCost();
            playerSupply++;
        } else {
            aiMinerals -= type.getMineralCost();
            aiGas -= type.getGasCost();
            aiSupply++;
        }
    }
    
    // Getters
    public int getPlayerMinerals() { return playerMinerals; }
    public int getPlayerGas() { return playerGas; }
    public int getPlayerSupply() { return playerSupply; }
    public int getPlayerMaxSupply() { return playerMaxSupply; }
    
    public int getAIMinerals() { return aiMinerals; }
    public int getAIGas() { return aiGas; }
    public int getAISupply() { return aiSupply; }
    public int getAIMaxSupply() { return aiMaxSupply; }
    
    public void increasePlayerMaxSupply(int amount) { playerMaxSupply += amount; }
    public void increaseAIMaxSupply(int amount) { aiMaxSupply += amount; }
}
