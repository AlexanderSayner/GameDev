# StarCraft Lite - RTS Game

A production-ready real-time strategy (RTS) game inspired by StarCraft, built with LWJGL3 and Gradle.

## Features

### Core Gameplay
- **Base Building**: Construct Command Centers, Barracks, Refineries, and Supply Depots
- **Unit Production**: Train SCVs (workers), Marines, and Tanks
- **Resource Management**: Gather minerals and vespene gas
- **Combat System**: Attack enemy units and structures
- **Win Conditions**: Destroy the enemy Command Center to win

### AI Opponent
Three difficulty levels with intelligent behavior:
- **Easy**: Slower build times, smaller army, less aggressive
- **Medium**: Balanced gameplay experience
- **Hard**: Fast builds, large armies, highly aggressive

### Controls
- **WASD/Arrow Keys**: Scroll camera around the map
- **Mouse Drag**: Select multiple units (box selection)
- **Left Click**: Select single unit
- **Right Click**: Move units, attack enemies, gather resources, or place buildings
- **B**: Queue Barracks placement
- **R**: Queue Refinery placement  
- **S**: Queue Supply Depot placement
- **Space**: Center camera on your base
- **Escape**: Cancel selection/building mode

### Units & Buildings

#### Buildings
| Building | Minerals | Gas | Health | Description |
|----------|----------|-----|--------|-------------|
| Command Center | 400 | 0 | 1500 | Main base, trains SCVs |
| Barracks | 150 | 0 | 1000 | Trains Marines and Tanks |
| Refinery | 100 | 0 | 750 | Collects vespene gas |
| Supply Depot | 100 | 0 | 500 | Increases supply cap by 8 |

#### Units
| Unit | Minerals | Gas | Health | Damage | Range | Speed |
|------|----------|-----|--------|--------|-------|-------|
| SCV | 50 | 0 | 60 | 5 | 50 | 2.5 |
| Marine | 50 | 0 | 40 | 6 | 80 | 3.0 |
| Tank | 150 | 100 | 150 | 15 | 120 | 1.5 |

## Requirements
- Java 17 or higher
- OpenGL 3.3 compatible graphics card
- Linux/Windows/MacOS with X11 display server

## Building the Game

```bash
cd starcraft-lite
gradle build
```

## Running the Game

### Using Gradle
```bash
gradle run
```

### Using the JAR
```bash
java -jar build/libs/starcraft-lite-1.0.0.jar
```

### Using the Distribution Scripts
```bash
# Linux/Mac
./build/scripts/starcraft-lite

# Windows
.\build\scripts\starcraft-lite.bat
```

## Game Mechanics

### Economy
- Start with 50 minerals and 10 supply
- SCVs automatically gather minerals when assigned to mineral patches
- Resources are collected continuously while gathering
- Build Supply Depots to increase your supply cap

### Combat
- Select units and right-click on enemies to attack
- Units will automatically engage enemies in range
- Different units have different ranges and damage values
- Focus fire on high-value targets like Command Centers

### Victory Conditions
- **Victory**: Destroy all enemy Command Centers
- **Defeat**: Lose all your Command Centers
- **Draw**: Both players lose all Command Centers simultaneously

## Technical Details

### Architecture
- **Entity-Component-like system** for game objects
- **Modern OpenGL 3.3** for rendering
- **Fixed timestep** game loop for consistent physics
- **State-based AI** with difficulty modifiers

### Code Structure
```
src/main/java/com/rts/game/
├── core/
│   ├── Game.java          # Main game class
│   └── Renderer.java      # OpenGL rendering
├── entities/
│   ├── Entity.java        # Game entity class
│   ├── EntityType.java    # Entity types enum
│   └── EntityOwner.java   # Owner enum
├── systems/
│   ├── GameMap.java       # Map and entity management
│   ├── GameState.java     # Game state and resources
│   ├── SelectionSystem.java # Unit selection
│   ├── CommandSystem.java   # Command handling
│   └── InputHandler.java    # Input processing
└── ai/
    └── AISystem.java      # AI logic
```

## Balance Notes

The game features fair balance between player and AI:
- Both sides follow the same rules and costs
- AI difficulty affects timing and aggression, not resources
- Rock-paper-scissors unit dynamics (Tanks beat Marines, Marines swarm Tanks)
- Supply system prevents unlimited unit spam

## Troubleshooting

### Display Issues
- Ensure your graphics drivers support OpenGL 3.3
- Try updating your GPU drivers

### Performance Issues
- The game runs at 60 FPS fixed timestep
- Large numbers of units may impact performance

### Controls Not Working
- Make sure the game window has focus
- Check that no other application is intercepting key inputs

## License

This is a fan project created for educational purposes. StarCraft is a trademark of Blizzard Entertainment.
