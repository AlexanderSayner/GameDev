package com.rts.game.core;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30.*;

import com.rts.game.entities.*;
import com.rts.game.systems.*;
import com.rts.game.ai.*;

public class Game implements Runnable {
    private long window;
    private int width = 1280;
    private int height = 720;
    private boolean running = false;
    
    // Game state
    private GameState gameState;
    private Renderer renderer;
    private InputHandler inputHandler;
    private SelectionSystem selectionSystem;
    private CommandSystem commandSystem;
    private AISystem aiSystem;
    private GameMap gameMap;
    
    // Camera
    private float cameraX = 0;
    private float cameraY = 0;
    private float cameraSpeed = 8.0f;
    
    // Difficulty levels - use AI's Difficulty enum
    private AISystem.Difficulty difficulty = AISystem.Difficulty.MEDIUM;
    
    public static void main(String[] args) {
        new Game().run();
    }
    
    @Override
    public void run() {
        try {
            init();
            loop();
            cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void init() throws Exception {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        
        window = GLFW.glfwCreateWindow(width, height, "StarCraft Lite - RTS Game", 0, 0);
        if (window == 0) {
            throw new RuntimeException("Failed to create window");
        }
        
        GLFW.glfwSetKeyCallback(window, this::keyCallback);
        GLFW.glfwSetMouseButtonCallback(window, this::mouseButtonCallback);
        GLFW.glfwSetCursorPosCallback(window, this::cursorPosCallback);
        GLFW.glfwSetScrollCallback(window, this::scrollCallback);
        GLFW.glfwSetFramebufferSizeCallback(window, this::framebufferSizeCallback);
        
        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();
        GLFW.glfwShowWindow(window);
        
        // Initialize game systems
        renderer = new Renderer();
        inputHandler = new InputHandler();
        selectionSystem = new SelectionSystem();
        commandSystem = new CommandSystem();
        aiSystem = new AISystem(difficulty);
        gameMap = new GameMap(2000, 2000);
        
        // Create initial entities for both players
        createInitialEntities();
        
        gameState = new GameState();
        gameState.setPlayerResources(15, 50, 0); // Starting minerals, gas, supply
        gameState.setAIResources(15, 50, 0);
        
        running = true;
        
        System.out.println("Game initialized successfully!");
        System.out.println("Controls:");
        System.out.println("- Mouse drag to select units");
        System.out.println("- Right-click to move/attack/build");
        System.out.println("- WASD or Arrow keys to scroll camera");
        System.out.println("- B: Build Barracks, R: Refinery, S: Supply Depot");
        System.out.println("- 1-4: Select unit types to build");
        System.out.println("- Space: Center camera on base");
    }
    
    private void createInitialEntities() {
        // Player base (top-left)
        Entity playerCommand = new Entity(100, 100, EntityType.COMMAND_CENTER);
        playerCommand.setOwner(EntityOwner.PLAYER);
        playerCommand.setMaxHealth(1500);
        playerCommand.setHealth(1500);
        gameMap.addEntity(playerCommand);
        
        // Create some worker units
        for (int i = 0; i < 4; i++) {
            Entity worker = new Entity(150 + i * 30, 100, EntityType.SCV);
            worker.setOwner(EntityOwner.PLAYER);
            worker.setMaxHealth(60);
            worker.setHealth(60);
            gameMap.addEntity(worker);
        }
        
        // AI base (bottom-right)
        Entity aiCommand = new Entity(1900, 1900, EntityType.COMMAND_CENTER);
        aiCommand.setOwner(EntityOwner.AI);
        aiCommand.setMaxHealth(1500);
        aiCommand.setHealth(1500);
        gameMap.addEntity(aiCommand);
        
        // AI workers
        for (int i = 0; i < 4; i++) {
            Entity worker = new Entity(1850 - i * 30, 1900, EntityType.SCV);
            worker.setOwner(EntityOwner.AI);
            worker.setMaxHealth(60);
            worker.setHealth(60);
            gameMap.addEntity(worker);
        }
        
        // Add some mineral patches near player base
        for (int i = 0; i < 5; i++) {
            Entity mineral = new Entity(50 + i * 40, 200, EntityType.MINERAL_PATCH);
            mineral.setOwner(EntityOwner.NEUTRAL);
            mineral.setResourceAmount(1500);
            gameMap.addEntity(mineral);
        }
        
        // Add some mineral patches near AI base
        for (int i = 0; i < 5; i++) {
            Entity mineral = new Entity(1950 - i * 40, 1800, EntityType.MINERAL_PATCH);
            mineral.setOwner(EntityOwner.NEUTRAL);
            mineral.setResourceAmount(1500);
            gameMap.addEntity(mineral);
        }
        
        // Add vespene geysers
        Entity playerGeyser = new Entity(300, 150, EntityType.VESPENE_GEYSER);
        playerGeyser.setOwner(EntityOwner.NEUTRAL);
        playerGeyser.setResourceAmount(5000);
        gameMap.addEntity(playerGeyser);
        
        Entity aiGeyser = new Entity(1700, 1850, EntityType.VESPENE_GEYSER);
        aiGeyser.setOwner(EntityOwner.NEUTRAL);
        aiGeyser.setResourceAmount(5000);
        gameMap.addEntity(aiGeyser);
    }
    
    private void loop() {
        double lastTime = GLFW.glfwGetTime();
        double accumulator = 0;
        double timeStep = 1.0 / 60.0;
        
        while (running && !GLFW.glfwWindowShouldClose(window)) {
            double currentTime = GLFW.glfwGetTime();
            double delta = currentTime - lastTime;
            lastTime = currentTime;
            
            accumulator += delta;
            
            while (accumulator >= timeStep) {
                update(timeStep);
                accumulator -= timeStep;
            }
            
            render();
            
            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }
    
    private void update(double dt) {
        // Handle camera movement
        handleCameraMovement(dt);
        
        // Update all entities
        gameMap.updateEntities(dt);
        
        // Update AI
        aiSystem.update(gameMap, gameState, dt);
        
        // Check win conditions
        checkWinConditions();
        
        // Update resources from workers
        updateResourceCollection(dt);
    }
    
    private void handleCameraMovement(double dt) {
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS ||
            GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) {
            cameraY -= cameraSpeed * dt * 60;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS ||
            GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) {
            cameraY += cameraSpeed * dt * 60;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS ||
            GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) {
            cameraX -= cameraSpeed * dt * 60;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS ||
            GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
            cameraX += cameraSpeed * dt * 60;
        }
        
        // Clamp camera to map bounds
        cameraX = Math.max(0, Math.min(cameraX, gameMap.getWidth() - width));
        cameraY = Math.max(0, Math.min(cameraY, gameMap.getHeight() - height));
    }
    
    private void updateResourceCollection(double dt) {
        // Simplified resource collection
        List<Entity> playerWorkers = gameMap.getEntitiesByOwnerAndType(EntityOwner.PLAYER, EntityType.SCV);
        for (Entity worker : playerWorkers) {
            if (worker.isGathering()) {
                gameState.addPlayerMinerals((float)(0.5 * dt * 60)); // Collect minerals over time
            }
        }
        
        List<Entity> aiWorkers = gameMap.getEntitiesByOwnerAndType(EntityOwner.AI, EntityType.SCV);
        for (Entity worker : aiWorkers) {
            if (worker.isGathering()) {
                gameState.addAIMinerals((float)(0.5 * dt * 60));
            }
        }
    }
    
    private void checkWinConditions() {
        boolean playerHasBase = gameMap.hasEntityOfTypeAndOwner(EntityType.COMMAND_CENTER, EntityOwner.PLAYER);
        boolean aiHasBase = gameMap.hasEntityOfTypeAndOwner(EntityType.COMMAND_CENTER, EntityOwner.AI);
        
        if (!playerHasBase && aiHasBase) {
            System.out.println("DEFEAT! The AI has destroyed your base.");
            running = false;
        } else if (playerHasBase && !aiHasBase) {
            System.out.println("VICTORY! You have destroyed the enemy base.");
            running = false;
        } else if (!playerHasBase && !aiHasBase) {
            System.out.println("DRAW! Both bases destroyed.");
            running = false;
        }
    }
    
    private void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        glClearColor(0.1f, 0.15f, 0.1f, 1.0f);
        
        renderer.beginBatch(cameraX, cameraY, width, height);
        
        // Render map background
        renderer.renderMapBackground(gameMap);
        
        // Render all entities
        for (Entity entity : gameMap.getEntities()) {
            renderer.renderEntity(entity);
        }
        
        // Render selection box
        if (inputHandler.isSelecting()) {
            renderer.renderSelectionBox(inputHandler.getSelectionStart(), inputHandler.getSelectionEnd(), cameraX, cameraY);
        }
        
        // Render UI
        renderer.renderUI(gameState, cameraX, cameraY);
        
        renderer.endBatch();
    }
    
    private void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW.GLFW_PRESS) {
            switch (key) {
                case GLFW.GLFW_KEY_B:
                    commandSystem.queueBuildingPlacement(EntityType.BARRACKS);
                    break;
                case GLFW.GLFW_KEY_R:
                    commandSystem.queueBuildingPlacement(EntityType.REFINERY);
                    break;
                case GLFW.GLFW_KEY_S:
                    commandSystem.queueBuildingPlacement(EntityType.SUPPLY_DEPOT);
                    break;
                case GLFW.GLFW_KEY_SPACE:
                    centerCameraOnBase();
                    break;
                case GLFW.GLFW_KEY_ESCAPE:
                    selectionSystem.clearSelection();
                    commandSystem.cancelBuildingPlacement();
                    break;
            }
        }
    }
    
    private void mouseButtonCallback(long window, int button, int action, int mods) {
        double[] xpos = new double[1];
        double[] ypos = new double[1];
        GLFW.glfwGetCursorPos(window, xpos, ypos);
        
        float worldX = (float) xpos[0] + cameraX;
        float worldY = (float) ypos[0] + cameraY;
        
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (action == GLFW.GLFW_PRESS) {
                inputHandler.startSelection((float) xpos[0], (float) ypos[0]);
            } else {
                inputHandler.endSelection();
                selectionSystem.selectUnitsInArea(
                    inputHandler.getSelectionStartX(),
                    inputHandler.getSelectionStartY(),
                    inputHandler.getCurrentX(),
                    inputHandler.getCurrentY(),
                    gameMap,
                    cameraX,
                    cameraY
                );
            }
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (action == GLFW.GLFW_PRESS) {
                commandSystem.issueCommand(worldX, worldY, selectionSystem.getSelectedUnits(), gameMap, gameState);
            }
        }
    }
    
    private void cursorPosCallback(long window, double xpos, double ypos) {
        inputHandler.updateCursorPosition((float) xpos, (float) ypos);
    }
    
    private void scrollCallback(long window, double xoffset, double yoffset) {
        // Could implement zoom functionality here
    }
    
    private void framebufferSizeCallback(long window, int w, int h) {
        width = w;
        height = h;
        glViewport(0, 0, w, h);
    }
    
    private void centerCameraOnBase() {
        Entity base = gameMap.getFirstEntityOfTypeAndOwner(EntityType.COMMAND_CENTER, EntityOwner.PLAYER);
        if (base != null) {
            cameraX = base.getX() - width / 2;
            cameraY = base.getY() - height / 2;
        }
    }
    
    private void cleanup() {
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
}
