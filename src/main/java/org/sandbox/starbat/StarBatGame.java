package org.sandbox.starbat;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class StarBatGame {
    
    private final double WIDTH = 1024.0;
    private final double HEIGHT = 768.0;
    
    // Game state
    private final java.util.List<Mineral> minerals = new java.util.ArrayList<>();
    private final java.util.List<Building> buildings = new java.util.ArrayList<>();
    private final java.util.List<Worker> workers = new java.util.ArrayList<>();
    private final java.util.List<Marine> marines = new java.util.ArrayList<>();
    private final java.util.List<Unit> selectedUnits = new java.util.ArrayList<>();
    
    // Resources
    private int mineralCount = 50;
    
    // UI elements
    private Canvas canvas;
    private GraphicsContext gc;
    private BuildingType buildMode = null;
    
    private final Stage primaryStage;
    
    public StarBatGame(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    public void start() {
        Pane root = new Pane();
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setTitle("StarBat");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        
        initializeGame();
        setupInputHandlers(scene);
        
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        };
        gameLoop.start();
    }
    
    private void initializeGame() {
        // Create main base (Command Center equivalent)
        Building base = new Building(400, 350, 100, 80, BuildingType.BASE, Color.BLUE);
        buildings.add(base);
        
        // Create mineral fields
        for (int i = 0; i < 8; i++) {
            double angle = i * (Math.PI / 4);
            double x = 200 + 150 * Math.cos(angle);
            double y = 200 + 150 * Math.sin(angle);
            minerals.add(new Mineral(x, y, 50));
        }
        
        // Create 3 starting workers
        for (int i = 0; i < 3; i++) {
            Worker worker = new Worker(400 + i * 40, 450, this);
            workers.add(worker);
        }
    }
    
    private void setupInputHandlers(Scene scene) {
        scene.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                handleLeftClick(event.getX(), event.getY());
            } else if (event.getButton() == MouseButton.SECONDARY) {
                handleRightClick(event.getX(), event.getY());
            }
        });
    }
    
    private void handleLeftClick(double x, double y) {
        // Check if clicking on UI buttons first
        // Barracks button area: 150, HEIGHT-90, 120, 30
        if (x >= 150 && x <= 270 && y >= HEIGHT - 90 && y <= HEIGHT - 60) {
            buildMode = BuildingType.BARRACKS;
            selectedUnits.clear();
            return;
        }
        
        // Clear previous selection
        selectedUnits.clear();
        buildMode = null;
        
        // Check if clicking on a building for production
        for (Building building : buildings) {
            if (building.contains(x, y)) {
                if (building.getType() == BuildingType.BASE) {
                    // Train worker
                    if (mineralCount >= 50) {
                        mineralCount -= 50;
                        Worker newWorker = new Worker(building.getX() + building.getWidth()/2, 
                                                       building.getY() + building.getHeight(), this);
                        workers.add(newWorker);
                    }
                } else if (building.getType() == BuildingType.BARRACKS) {
                    // Train marine
                    if (mineralCount >= 50) {
                        mineralCount -= 50;
                        Marine newMarine = new Marine(building.getX() + building.getWidth()/2, 
                                                       building.getY() + building.getHeight(), this);
                        marines.add(newMarine);
                    }
                }
                return;
            }
        }
        
        // Check if clicking on a worker
        for (Worker worker : workers) {
            if (worker.contains(x, y)) {
                selectedUnits.add(worker);
                worker.setSelected(true);
            } else {
                worker.setSelected(false);
            }
        }
        
        // Check if clicking on a marine
        for (Marine marine : marines) {
            if (marine.contains(x, y)) {
                selectedUnits.add(marine);
                marine.setSelected(true);
            } else {
                marine.setSelected(false);
            }
        }
        
        // Check if clicking on a mineral
        for (Mineral mineral : minerals) {
            if (mineral.contains(x, y)) {
                // Select all workers to mine this mineral
                for (Worker worker : workers) {
                    worker.setTargetMineral(mineral);
                }
                return;
            }
        }
    }
    
    private void handleRightClick(double x, double y) {
        // Check if we're in build mode
        if (buildMode != null) {
            BuildingType type = buildMode;
            Building newBuilding = new Building(x - 50, y - 40, 100, 80, type, Color.GREEN);
            
            // Check cost
            int cost = (type == BuildingType.BARRACKS) ? 150 : 0;
            
            if (mineralCount >= cost) {
                mineralCount -= cost;
                buildings.add(newBuilding);
                
                // Assign workers to build
                for (Unit unit : selectedUnits) {
                    if (unit instanceof Worker) {
                        ((Worker) unit).setTargetBuilding(newBuilding);
                    }
                }
                buildMode = null;
            }
            return;
        }
        
        // Move selected units
        for (Unit unit : selectedUnits) {
            unit.setTarget(x, y);
        }
    }
    
    public void enterBuildMode(BuildingType type) {
        buildMode = type;
    }
    
    private void update() {
        // Update all units
        for (Worker worker : workers) {
            worker.update();
        }
        for (Marine marine : marines) {
            marine.update();
        }
        
        // Check for completed buildings
        buildings.removeIf(b -> b.isMarkedForRemoval());
    }
    
    private void render() {
        gc.setFill(Color.rgb(30, 30, 50));
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Draw minerals
        for (Mineral mineral : minerals) {
            mineral.render(gc);
        }
        
        // Draw buildings
        for (Building building : buildings) {
            building.render(gc);
        }
        
        // Draw workers
        for (Worker worker : workers) {
            worker.render(gc);
        }
        
        // Draw marines
        for (Marine marine : marines) {
            marine.render(gc);
        }
        
        // Draw selection circles
        for (Unit unit : selectedUnits) {
            gc.setStroke(Color.LIME);
            gc.setLineWidth(2.0);
            gc.strokeOval(unit.getX() - 20, unit.getY() - 20, 40, 40);
        }
        
        // Draw UI
        renderUI();
        
        // Draw build mode indicator
        if (buildMode != null) {
            gc.setFill(Color.YELLOW);
            gc.fillText("Build Mode: " + buildMode.name() + " - Right-click to place", 10, HEIGHT - 80);
        }
    }
    
    private void renderUI() {
        // Resource display
        gc.setFill(Color.WHITE);
        gc.fillText("Minerals: " + mineralCount, 10, 20);
        
        // Build buttons area
        gc.setFill(Color.rgb(50, 50, 70));
        gc.fillRect(0, HEIGHT - 100, WIDTH, 100);
        
        gc.setFill(Color.WHITE);
        gc.fillText("Build Commands:", 10, HEIGHT - 70);
        
        // Barracks button
        gc.setFill((buildMode == BuildingType.BARRACKS) ? Color.LIME : Color.GRAY);
        gc.fillRect(150, HEIGHT - 90, 120, 30);
        gc.setFill(Color.WHITE);
        gc.fillText("Barracks (150)", 155, HEIGHT - 70);
        
        // Check if barracks button clicked (via left click)
    }
    
    public int getMineralCount() { return mineralCount; }
    public void addMinerals(int amount) { mineralCount += amount; }
    public java.util.List<Building> getBuildings() { return buildings; }
}
