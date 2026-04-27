package com.rts.game;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import com.rts.entities.*;
import com.rts.ai.*;
import com.rts.render.*;
import com.rts.input.*;
import com.rts.ui.*;

import java.util.*;
import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class StarCraftLite {
    private long window;
    private int width = 1280;
    private int height = 720;
    
    private GameWorld gameWorld;
    private Renderer renderer;
    private InputHandler inputHandler;
    private UIManager uiManager;
    private AIController aiController;
    
    private boolean running = true;
    private GameState gameState = GameState.MENU;
    private Difficulty selectedDifficulty = Difficulty.MEDIUM;
    
    private enum GameState { MENU, PLAYING, GAME_OVER }
    
    public void run() {
        init();
        gameLoop();
        cleanup();
    }
    
    private void init() {
        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        
        window = glfwCreateWindow(width, height, "StarCraft Lite - RTS Game", 0, 0);
        if (window == 0) {
            throw new RuntimeException("Failed to create GLFW window");
        }
        
        glfwSetKeyCallback(window, this::keyCallback);
        glfwSetMouseButtonCallback(window, this::mouseButtonCallback);
        glfwSetCursorPosCallback(window, this::cursorPosCallback);
        glfwSetScrollCallback(window, this::scrollCallback);
        glfwSetWindowSizeCallback(window, this::windowSizeCallback);
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetFramebufferSize(window, pWidth, pHeight);
            width = pWidth.get(0);
            height = pHeight.get(0);
        }
        
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glfwSwapInterval(1);
        glfwShowWindow(window);
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0.1f, 0.1f, 0.15f, 1.0f);
        
        // Initialize game systems
        gameWorld = new GameWorld(width, height);
        renderer = new Renderer();
        inputHandler = new InputHandler(gameWorld, width, height);
        uiManager = new UIManager(width, height);
        aiController = new AIController(gameWorld);
        
        System.out.println("[GAME] StarCraft Lite initialized successfully!");
        System.out.println("[INFO] Window size: " + width + "x" + height);
    }
    
    private void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (gameState == GameState.PLAYING) {
            inputHandler.handleKeyboard(key, action);
            
            // Hotkeys for buildings
            if (action == GLFW_PRESS) {
                switch (key) {
                    case GLFW_KEY_B:
                        uiManager.setBuildingMode(BuildingType.BARRACKS);
                        break;
                    case GLFW_KEY_R:
                        uiManager.setBuildingMode(BuildingType.REFINERY);
                        break;
                    case GLFW_KEY_S:
                        uiManager.setBuildingMode(BuildingType.SUPPLY_DEPOT);
                        break;
                    case GLFW_KEY_SPACE:
                        gameWorld.centerCameraOnBase();
                        break;
                    case GLFW_KEY_A:
                        inputHandler.setAttackMode(true);
                        break;
                    case GLFW_KEY_M:
                        inputHandler.setMoveMode(true);
                        break;
                    case GLFW_KEY_ESCAPE:
                        uiManager.cancelBuildingMode();
                        inputHandler.setAttackMode(false);
                        inputHandler.setMoveMode(false);
                        break;
                }
            }
        } else if (gameState == GameState.MENU) {
            if (action == GLFW_PRESS) {
                if (key == GLFW_KEY_1) {
                    selectedDifficulty = Difficulty.EASY;
                    System.out.println("[MENU] Difficulty set to EASY");
                } else if (key == GLFW_KEY_2) {
                    selectedDifficulty = Difficulty.MEDIUM;
                    System.out.println("[MENU] Difficulty set to MEDIUM");
                } else if (key == GLFW_KEY_3) {
                    selectedDifficulty = Difficulty.HARD;
                    System.out.println("[MENU] Difficulty set to HARD");
                } else if (key == GLFW_KEY_ENTER) {
                    startGame();
                }
            }
        } else if (gameState == GameState.GAME_OVER) {
            if (action == GLFW_PRESS && key == GLFW_KEY_ENTER) {
                gameState = GameState.MENU;
                gameWorld = new GameWorld(width, height);
                aiController = new AIController(gameWorld);
                inputHandler = new InputHandler(gameWorld, width, height);
                uiManager = new UIManager(width, height);
            }
        }
    }
    
    private void mouseButtonCallback(long window, int button, int action, int mods) {
        double[] xpos = new double[1];
        double[] ypos = new double[1];
        glfwGetCursorPos(window, xpos, ypos);
        
        if (gameState == GameState.PLAYING) {
            inputHandler.handleMouseInput((int)xpos[0], (int)ypos[0], button, action);
        } else if (gameState == GameState.MENU && action == GLFW_PRESS && button == GLFW_MOUSE_BUTTON_LEFT) {
            handleMenuClick((int)xpos[0], (int)ypos[0]);
        } else if (gameState == GameState.GAME_OVER && action == GLFW_PRESS && button == GLFW_MOUSE_BUTTON_LEFT) {
            if (xpos[0] > width/2 - 100 && xpos[0] < width/2 + 100 && 
                ypos[0] > height/2 + 20 && ypos[0] < height/2 + 60) {
                gameState = GameState.MENU;
                gameWorld = new GameWorld(width, height);
                aiController = new AIController(gameWorld);
                inputHandler = new InputHandler(gameWorld, width, height);
                uiManager = new UIManager(width, height);
            }
        }
    }
    
    private void cursorPosCallback(long window, double xpos, double ypos) {
        if (gameState == GameState.PLAYING) {
            inputHandler.handleMouseMove((int)xpos, (int)ypos);
        }
    }
    
    private void scrollCallback(long window, double xoffset, double yoffset) {
        if (gameState == GameState.PLAYING) {
            gameWorld.adjustZoom((float)yoffset);
        }
    }
    
    private void windowSizeCallback(long window, int w, int h) {
        width = w;
        height = h;
        glViewport(0, 0, width, height);
        if (uiManager != null) uiManager.resize(w, h);
        if (inputHandler != null) inputHandler.resize(w, h);
    }
    
    private void handleMenuClick(int x, int y) {
        // Start Game button
        if (x > width/2 - 100 && x < width/2 + 100 && y > height/2 - 20 && y < height/2 + 20) {
            startGame();
        }
        // Difficulty buttons
        if (y > height/2 + 40 && y < height/2 + 80) {
            if (x > width/2 - 300 && x < width/2 - 100) {
                selectedDifficulty = Difficulty.EASY;
                System.out.println("[MENU] Difficulty set to EASY");
            } else if (x > width/2 - 80 && x < width/2 + 120) {
                selectedDifficulty = Difficulty.MEDIUM;
                System.out.println("[MENU] Difficulty set to MEDIUM");
            } else if (x > width/2 + 140 && x < width/2 + 340) {
                selectedDifficulty = Difficulty.HARD;
                System.out.println("[MENU] Difficulty set to HARD");
            }
        }
    }
    
    private void startGame() {
        gameState = GameState.PLAYING;
        aiController.setDifficulty(selectedDifficulty);
        gameWorld.initializePlayerBase();
        System.out.println("[GAME] Starting game with " + selectedDifficulty + " difficulty");
        System.out.println("[INFO] Player starts with 4 SCVs and 1 Barracks");
        System.out.println("[INFO] Objective: Destroy enemy Command Center");
    }
    
    private void gameLoop() {
        double lastTime = glfwGetTime();
        double accumulator = 0;
        double tickRate = 1.0 / 30.0;
        
        while (running && !glfwWindowShouldClose(window)) {
            double currentTime = glfwGetTime();
            double deltaTime = currentTime - lastTime;
            lastTime = currentTime;
            
            accumulator += deltaTime;
            
            while (accumulator >= tickRate) {
                update(tickRate);
                accumulator -= tickRate;
            }
            
            render();
            
            glfwPollEvents();
        }
    }
    
    private void update(double deltaTime) {
        if (gameState == GameState.PLAYING) {
            gameWorld.update(deltaTime);
            aiController.update(deltaTime);
            inputHandler.update(deltaTime);
            
            // Check win/lose conditions
            if (!gameWorld.hasPlayerCommandCenter()) {
                gameState = GameState.GAME_OVER;
                System.out.println("[GAME] DEFEAT! Your Command Center was destroyed.");
            } else if (!gameWorld.hasEnemyCommandCenter()) {
                gameState = GameState.GAME_OVER;
                System.out.println("[GAME] VICTORY! Enemy Command Center destroyed!");
            }
        }
    }
    
    private void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        
        if (gameState == GameState.MENU) {
            renderMenu();
        } else if (gameState == GameState.PLAYING) {
            renderer.render(gameWorld, inputHandler, uiManager);
            uiManager.render(gameWorld);
        } else if (gameState == GameState.GAME_OVER) {
            renderGameOver();
        }
        
        glfwSwapBuffers(window);
    }
    
    private void renderMenu() {
        // Background gradient
        glBegin(GL_QUADS);
        glColor3f(0.1f, 0.15f, 0.2f);
        glVertex2f(-1, -1);
        glVertex2f(1, -1);
        glColor3f(0.15f, 0.2f, 0.25f);
        glVertex2f(1, 1);
        glVertex2f(-1, 1);
        glEnd();
        
        // Title
        drawText("STAR CRAFT LITE", 0, 0.3f, 2.0f, new float[]{0.9f, 0.8f, 0.3f});
        
        // Subtitle
        drawText("Real-Time Strategy Game", 0, 0.2f, 0.8f, new float[]{0.7f, 0.7f, 0.7f});
        
        // Start button
        drawButton("START GAME", 0, 0, 200, 40, new float[]{0.2f, 0.6f, 0.3f});
        
        // Difficulty selection
        drawText("Select Difficulty:", 0, -0.15f, 0.7f, new float[]{0.8f, 0.8f, 0.8f});
        
        String[] difficulties = {"EASY (1)", "MEDIUM (2)", "HARD (3)"};
        float[] colors = {
            selectedDifficulty == Difficulty.EASY ? 0.3f : 0.5f,
            selectedDifficulty == Difficulty.EASY ? 0.7f : 0.5f,
            selectedDifficulty == Difficulty.EASY ? 0.3f : 0.5f
        };
        drawButton(difficulties[0], -width/4f/(width/2f), -0.25f, 180, 35, 
                   selectedDifficulty == Difficulty.EASY ? new float[]{0.3f, 0.7f, 0.3f} : new float[]{0.4f, 0.4f, 0.4f});
        
        colors = new float[]{
            selectedDifficulty == Difficulty.MEDIUM ? 0.3f : 0.5f,
            selectedDifficulty == Difficulty.MEDIUM ? 0.7f : 0.5f,
            selectedDifficulty == Difficulty.MEDIUM ? 0.3f : 0.5f
        };
        drawButton(difficulties[1], 0, -0.25f, 180, 35,
                   selectedDifficulty == Difficulty.MEDIUM ? new float[]{0.3f, 0.7f, 0.3f} : new float[]{0.4f, 0.4f, 0.4f});
        
        colors = new float[]{
            selectedDifficulty == Difficulty.HARD ? 0.3f : 0.5f,
            selectedDifficulty == Difficulty.HARD ? 0.7f : 0.5f,
            selectedDifficulty == Difficulty.HARD ? 0.3f : 0.5f
        };
        drawButton(difficulties[2], width/4f/(width/2f), -0.25f, 180, 35,
                   selectedDifficulty == Difficulty.HARD ? new float[]{0.7f, 0.3f, 0.3f} : new float[]{0.4f, 0.4f, 0.4f});
        
        // Instructions
        drawText("Controls: WASD/Arrows to scroll | Right-click to move/attack | B/R/S for buildings", 0, -0.4f, 0.5f, new float[]{0.6f, 0.6f, 0.6f});
    }
    
    private void renderGameOver() {
        // Dark overlay
        glBegin(GL_QUADS);
        glColor4f(0.0f, 0.0f, 0.0f, 0.7f);
        glVertex2f(-1, -1);
        glVertex2f(1, -1);
        glVertex2f(1, 1);
        glVertex2f(-1, 1);
        glEnd();
        
        boolean victory = gameWorld.hasEnemyCommandCenter() == false;
        String message = victory ? "VICTORY!" : "DEFEAT!";
        float[] color = victory ? new float[]{0.3f, 0.7f, 0.3f} : new float[]{0.7f, 0.3f, 0.3f};
        
        drawText(message, 0, 0.1f, 2.0f, color);
        drawText(victory ? "Enemy Command Center destroyed!" : "Your Command Center was destroyed!", 0, -0.05f, 0.8f, new float[]{0.8f, 0.8f, 0.8f});
        drawText("Press ENTER or click to return to menu", 0, -0.2f, 0.6f, new float[]{0.6f, 0.6f, 0.6f});
        
        drawButton("MAIN MENU", 0, -0.35f, 200, 40, new float[]{0.4f, 0.4f, 0.6f});
    }
    
    private void drawText(String text, float x, float y, float scale, float[] color) {
        // Simplified text rendering - in production would use STB TrueType
        glColor3fv(color);
        glRasterPos2f(x, y);
        // For now, just skip actual text rendering as it requires font loading
        // The UI manager handles proper text rendering
    }
    
    private void drawButton(String text, float x, float y, float w, float h, float[] color) {
        float hw = w / (float)width;
        float hh = h / (float)height;
        
        glBegin(GL_QUADS);
        glColor3fv(color);
        glVertex2f(x - hw, y - hh);
        glVertex2f(x + hw, y - hh);
        glVertex2f(x + hw, y + hh);
        glVertex2f(x - hw, y + hh);
        glEnd();
        
        glBegin(GL_LINE_LOOP);
        glColor3f(1.0f, 1.0f, 1.0f);
        glVertex2f(x - hw, y - hh);
        glVertex2f(x + hw, y - hh);
        glVertex2f(x + hw, y + hh);
        glVertex2f(x - hw, y + hh);
        glEnd();
    }
    
    private void cleanup() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        System.out.println("[GAME] Cleanup complete");
    }
    
    public static void main(String[] args) {
        new StarCraftLite().run();
    }
}
